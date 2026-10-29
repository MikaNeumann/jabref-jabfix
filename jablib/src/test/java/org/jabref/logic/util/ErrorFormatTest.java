package org.jabref.logic.util;

import java.nio.file.Path;
import java.util.Optional;

import org.jabref.logic.importer.ParserResult;
import org.jabref.model.entry.field.StandardField;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorFormatTest {

    private static final Path LIBRARY = Path.of("library.bib");

    @Test
    void aFindingAboutAFieldNamesTheFieldBehindTheCitationKey() {
        assertEquals("library.bib:12:3:knuth1984:author: value has leading or trailing whitespace",
                ErrorFormat.line(LIBRARY, new ParserResult.Range(12, 3), "knuth1984",
                        Optional.of(StandardField.AUTHOR), "value has leading or trailing whitespace"));
    }

    @Test
    void aFindingAboutAnEntryNamesOnlyTheCitationKey() {
        assertEquals("library.bib:12:0:knuth1984: the entry has no year",
                ErrorFormat.line(LIBRARY, new ParserResult.Range(12, 0), "knuth1984",
                        Optional.empty(), "the entry has no year"));
    }

    /// An entry that was not read from a file has no place in one.
    @Test
    void anEntryWithoutARangeIsReportedAtTheStart() {
        assertEquals("library.bib:0:0:key:title: message",
                ErrorFormat.line(LIBRARY, ParserResult.Range.NULL_RANGE, "key",
                        Optional.of(StandardField.TITLE), "message"));
    }
}
