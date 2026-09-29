package org.jabref.logic.lint.rule;

import java.util.List;

import org.jabref.model.entry.BibEntry;

import org.jspecify.annotations.NullMarked;

/// Reports what another rule finds, without repairing it.
///
/// A rule keeps deciding what is wrong; this only takes the repair away, so a library can have a
/// rule tell it about something without having the value rewritten under it. The findings then look
/// like those of a rule that knows no repair, which callers already have to handle -- `jabkit fix`
/// says them out loud instead of letting them pass unseen in the output.
///
/// The findings keep naming the rule that reported them, so a report reads the same either way.
@NullMarked
record CheckOnlyRule(Rule rule) implements Rule {

    @Override
    public String id() {
        return rule.id();
    }

    @Override
    public String description() {
        return rule.description();
    }

    @Override
    public List<Finding> scan(BibEntry entry) {
        return rule.scan(entry).stream()
                   .map(Finding::withoutFix)
                   .toList();
    }
}
