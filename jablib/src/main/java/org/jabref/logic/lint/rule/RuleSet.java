package org.jabref.logic.lint.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jabref.logic.bibtex.FieldPreferences;
import org.jabref.logic.lint.rules.RepeatedWhitespaceRule;
import org.jabref.logic.lint.rules.SurroundingWhitespaceRule;

import org.jspecify.annotations.NullMarked;

/// The rules a JabFix run applies, in the order it applies them.
///
/// This is the seam configuration hangs off: [#all] is the built-in default, [#without] switches
/// individual rules off by id, and [#with] adds a rule that does not ship with JabFix at all.
///
/// Order is part of the contract, not an implementation detail. Each rule sees what the rules
/// before it left behind, so rules that tidy a value up belong before rules that pattern-match it:
/// otherwise the latter are defeated by noise the former would have removed.
@NullMarked
public class RuleSet {

    private final List<Rule> rules;

    private RuleSet(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    /// The default: every rule JabFix ships with, in the order a save has always applied them.
    ///
    /// @param fieldPreferences tells the whitespace rules which fields hold text that may be wrapped
    public static RuleSet all(FieldPreferences fieldPreferences) {
        return new RuleSet(List.of(
                new SurroundingWhitespaceRule(),
                new RepeatedWhitespaceRule(fieldPreferences)));
    }

    /// Exactly the given rules, in the given order. An empty set reformats without applying any
    /// rule at all.
    public static RuleSet of(Rule... rules) {
        return new RuleSet(List.of(rules));
    }

    /// The same rules, minus the ones named.
    ///
    /// @param ruleIds the [Rule#id]s to switch off
    /// @throws UnknownRuleException if any id names no rule in this set, rather than passing over
    ///                              it and leaving the user to wonder why nothing changed
    public RuleSet without(Collection<String> ruleIds) throws UnknownRuleException {
        List<String> unknown = ruleIds.stream()
                                      .distinct()
                                      .filter(ruleId -> !ids().contains(ruleId))
                                      .toList();
        if (!unknown.isEmpty()) {
            throw new UnknownRuleException(unknown, ids());
        }
        return new RuleSet(rules.stream()
                                .filter(rule -> !ruleIds.contains(rule.id()))
                                .toList());
    }

    /// Appends a rule, which is how a rule implemented outside this module joins a run.
    public RuleSet with(Rule rule) {
        List<Rule> extended = new ArrayList<>(rules);
        extended.add(rule);
        return new RuleSet(extended);
    }

    /// @return the rules, in application order
    public List<Rule> rules() {
        return rules;
    }

    /// @return the [Rule#id] of every rule in this set, in application order
    public List<String> ids() {
        return rules.stream().map(Rule::id).toList();
    }
}
