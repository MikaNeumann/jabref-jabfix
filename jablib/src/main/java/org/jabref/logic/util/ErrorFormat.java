package org.jabref.logic.util;

import java.nio.file.Path;
import java.util.Optional;

import org.jabref.logic.importer.ParserResult;
import org.jabref.model.entry.field.Field;

import org.jspecify.annotations.NullMarked;

/// The line-oriented `file:line:column:citationKey[:field]: message` format the checking commands
/// report their findings in, which editors and CI tooling parse to jump to the place at fault.
///
/// Where that place is comes from the [ParserResult] of the file the entries were read from:
/// [ParserResult#getFieldRange] for a finding about one field, and
/// [ParserResult#getCompleteEntryIndicator] for one about an entry as a whole.
@NullMarked
public class ErrorFormat {

    private ErrorFormat() {
    }

    /// One finding as a line, without the line break.
    ///
    /// @param file    the file the entries were read from
    /// @param range   where in it the finding is; [ParserResult.Range#NULL_RANGE] reports line and
    ///                column 0, which is what an entry that was not read from a file has
    /// @param entry   what names the offending entry, usually its citation key
    /// @param field   the field at fault, empty when the finding concerns the entry as a whole
    /// @param message what is wrong, phrased for a person reading a report
    public static String line(Path file, ParserResult.Range range, String entry, Optional<Field> field, String message) {
        String location = field.map(atFault -> entry + ":" + atFault.getName())
                               .orElse(entry);
        return "%s:%d:%d:%s: %s".formatted(file, range.startLine(), range.startColumn(), location, message);
    }
}
