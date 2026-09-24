package org.jabref.logic.lint.rule;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.jabref.logic.cleanup.FieldFormatterCleanup;
import org.jabref.model.FieldChange;
import org.jabref.model.entry.BibEntry;

import org.jspecify.annotations.NullMarked;

/// Runs one of JabRef's Save Actions as a [Rule].
///
/// Everything the Save Action already decides is left to it: which fields it applies to, and what
/// their values become. It is run on a copy of the entry, since [#scan] must not change the entry,
/// and each [FieldChange] it reports becomes a [Finding]. The finding's fix sets just that one field,
/// so a Save Action on all fields still yields one repair per field. Idempotency, which [Rule]
/// requires, is up to the wrapped formatter.
///
/// @param saveAction a formatter applied to a field, as configured in JabRef's Save Actions
@NullMarked
public record SaveActionRule(FieldFormatterCleanup saveAction) implements Rule {

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
    public List<Finding> scan(BibEntry entry) {
        return saveAction.cleanup(new BibEntry(entry)).stream()
                         .map(change -> Finding.of(this, entry, change.field(), saveAction.getFormatter().getName(),
                                 target -> apply(change, target)))
                         .toList();
    }

    /// A Save Action removes a field whose value it formats to nothing, reported as a `null` new value.
    private static Optional<FieldChange> apply(FieldChange change, BibEntry target) {
        return Optional.ofNullable(change.newValue())
                       .map(newValue -> target.setField(change.field(), newValue))
                       .orElseGet(() -> target.clearField(change.field()));
    }
}
