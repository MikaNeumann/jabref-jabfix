package org.jabref.logic.lint;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jabref.logic.bibtex.FieldPreferences;
import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.logic.importer.fileformat.BibtexParser;
import org.jabref.logic.lint.rule.Finding;
import org.jabref.logic.lint.rule.RuleSet;
import org.jabref.model.FieldChange;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.StandardField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JabFixTest {

    private static final String SLOPPY = """
            @ARTICLE{key,
            author = " Doe, Jane ",
                YEAR="2024"
            }
            """;

    private static final FieldPreferences FIELD_PREFERENCES = new FieldPreferences(true, List.of(), List.of());

    private ImportFormatPreferences importFormatPreferences;

    @BeforeEach
    void setUp() {
        importFormatPreferences = mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS);
        when(importFormatPreferences.fieldPreferences()).thenReturn(FIELD_PREFERENCES);
    }

    @Test
    void repairsWhatTheRulesReport() throws IOException {
        List<BibEntry> entries = parse(SLOPPY);

        new JabFix(RuleSet.all(FIELD_PREFERENCES)).apply(entries);

        assertEquals("Doe, Jane", entries.getFirst().getField(StandardField.AUTHOR).orElseThrow());
    }

    @Test
    void reportsWhatItRepaired() throws IOException {
        JabFixResult result = new JabFix(RuleSet.all(FIELD_PREFERENCES)).apply(parse(SLOPPY));

        assertEquals(List.of("surrounding-whitespace"),
                result.findings().stream().map(finding -> finding.rule().id()).toList());
    }

    @Test
    void everyFindingCarriesTheEntryAndFieldItConcerns() throws IOException {
        Finding finding = new JabFix(RuleSet.all(FIELD_PREFERENCES)).apply(parse(SLOPPY)).findings().getFirst();

        assertEquals("key", finding.citationKey());
        assertEquals("author", finding.field().orElseThrow().getName());
        assertTrue(finding.isFixable());
    }

    /// What a repair changed is what an undo manager collects.
    @Test
    void reportsWhatEveryRepairChanged() throws IOException {
        JabFixResult result = new JabFix(RuleSet.all(FIELD_PREFERENCES)).apply(parse(SLOPPY));

        assertEquals(List.of(" Doe, Jane "), result.changes().stream().map(FieldChange::oldValue).toList());
        assertEquals(List.of("Doe, Jane"), result.changes().stream().map(FieldChange::newValue).toList());
    }

    @Test
    void anEmptyRuleSetChangesNothing() throws IOException {
        List<BibEntry> entries = parse(SLOPPY);

        JabFixResult result = new JabFix(RuleSet.of()).apply(entries);

        assertEquals(" Doe, Jane ", entries.getFirst().getField(StandardField.AUTHOR).orElseThrow());
        assertEquals(List.of(), result.changes());
    }

    /// Scanning stays on the calling thread; only the repairs are handed to the scheduler, which a
    /// GUI uses to keep entry mutations on the JavaFX thread.
    @Test
    void everyRepairGoesThroughTheMutationScheduler() throws IOException {
        List<BibEntry> entries = parse(SLOPPY);
        AtomicInteger scheduled = new AtomicInteger();

        new JabFix(RuleSet.all(FIELD_PREFERENCES)).apply(entries, mutation -> {
            scheduled.incrementAndGet();
            mutation.run();
        });

        assertEquals(1, scheduled.get());
        assertEquals("Doe, Jane", entries.getFirst().getField(StandardField.AUTHOR).orElseThrow());
    }

    private List<BibEntry> parse(String bibtex) throws IOException {
        return new BibtexParser(importFormatPreferences)
                .parse(Reader.of(bibtex))
                .getDatabaseContext()
                .getEntries();
    }
}
