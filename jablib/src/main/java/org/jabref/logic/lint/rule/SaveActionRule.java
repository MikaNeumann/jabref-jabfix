package org.jabref.logic.lint.rule;

import java.util.Locale;

import org.jabref.logic.cleanup.CleanupJob;
import org.jabref.logic.cleanup.FieldFormatterCleanup;
import org.jabref.model.FieldChange;

import org.jspecify.annotations.NullMarked;

/// Runs one of JabRef's Save Actions as a [Rule].
///
/// A Save Action is a formatter applied to a field, which is a [CleanupJob] like any other -- see
/// [CleanupRule] for how it is scanned and repaired. What this class adds is the identity of the
/// rule: the field and the key the formatter is configured under.
@NullMarked
public class SaveActionRule extends CleanupRule {

    private final FieldFormatterCleanup saveAction;

    /// @param saveAction a formatter applied to a field, as configured in JabRef's Save Actions
    public SaveActionRule(FieldFormatterCleanup saveAction) {
        this.saveAction = saveAction;
    }

    /// The field and the formatter's key, e.g. `pages-normalize-page-numbers`.
    @Override
    public String id() {
        return (saveAction.getField().getName() + "-" + saveAction.getFormatter().getKey())
                .replace('_', '-')
                .toLowerCase(Locale.ROOT);
    }

    @Override
    public String description() {
        return saveAction.getFormatter().getDescription();
    }

    @Override
    protected CleanupJob cleanupJob() {
        return saveAction;
    }

    @Override
    protected String message(FieldChange change) {
        return saveAction.getFormatter().getName();
    }
}
