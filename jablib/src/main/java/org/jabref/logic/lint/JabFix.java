package org.jabref.logic.lint;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jabref.logic.lint.rule.Finding;
import org.jabref.logic.lint.rule.Rule;
import org.jabref.logic.lint.rule.RuleSet;
import org.jabref.model.FieldChange;
import org.jabref.model.entry.BibEntry;

import org.jspecify.annotations.NullMarked;

/// Engine behind the `jabkit fix` command: applies a [RuleSet] to the entries of a library.
///
/// The rules decide the questions the `.bib` format leaves open but that survive parsing -- such as
/// whether a value may be padded with whitespace. Serialization is not this class's business: it is
/// left to JabRef's own [org.jabref.logic.exporter.BibDatabaseWriter], which settles everything that
/// does *not* survive parsing -- entry type capitalization, whether values are braced or quoted,
/// field order, indentation -- and which runs this engine for a library that asks for it.
@NullMarked
public class JabFix {

    private final RuleSet ruleSet;

    public JabFix(RuleSet ruleSet) {
        this.ruleSet = ruleSet;
    }

    /// Runs every rule over `entries` and applies the repairs they offer.
    public JabFixResult apply(List<BibEntry> entries) {
        return apply(entries, Runnable::run);
    }

    /// Runs every rule over `entries`, in the order of the [RuleSet], and applies the repairs they
    /// offer.
    ///
    /// The entries are modified in place. Rules are applied as a pipeline: each rule sees what the
    /// rules before it left behind. A rule is therefore only ever run once over an entry -- see
    /// [Rule] for the contract that makes a second pass unnecessary.
    ///
    /// @param mutationScheduler routes the [BibEntry] mutations the repairs make to the correct
    ///                          thread; it has to run each of them synchronously
    public JabFixResult apply(List<BibEntry> entries, Consumer<Runnable> mutationScheduler) {
        List<Finding> findings = new ArrayList<>();
        List<FieldChange> changes = new ArrayList<>();
        for (BibEntry entry : entries) {
            for (Rule rule : ruleSet.rules()) {
                List<Finding> reported = rule.scan(entry);
                for (Finding finding : reported) {
                    // Only the mutation is scheduled; scanning stays on the calling thread, as the
                    // cleanup jobs of JabRef do it.
                    finding.fix().ifPresent(fix -> mutationScheduler.accept(() -> fix.applyTo(entry).ifPresent(changes::add)));
                }
                findings.addAll(reported);
            }
        }
        return new JabFixResult(findings, changes);
    }
}
