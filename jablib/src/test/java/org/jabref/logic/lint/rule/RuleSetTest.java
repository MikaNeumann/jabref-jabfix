package org.jabref.logic.lint.rule;

import java.util.List;

import org.jabref.logic.bibtex.FieldPreferences;
import org.jabref.model.entry.BibEntry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RuleSetTest {

    private static final FieldPreferences FIELD_PREFERENCES = new FieldPreferences(true, List.of(), List.of());

    /// Stands in for a rule implemented outside this module.
    private static final Rule EXTERNAL = new Rule() {
        @Override
        public String id() {
            return "external";
        }

        @Override
        public String description() {
            return "A rule that does not ship with JabFix.";
        }

        @Override
        public List<Finding> scan(BibEntry entry) {
            return List.of();
        }
    };

    @Test
    void allContainsEveryBuiltInRule() {
        assertEquals(List.of("surrounding-whitespace", "repeated-whitespace"), RuleSet.all(FIELD_PREFERENCES).ids());
    }

    @Test
    void withoutSwitchesTheNamedRuleOff() throws UnknownRuleException {
        assertEquals(List.of("repeated-whitespace"),
                RuleSet.all(FIELD_PREFERENCES).without(List.of("surrounding-whitespace")).ids());
    }

    @Test
    void withoutNothingChangesNothing() throws UnknownRuleException {
        assertEquals(RuleSet.all(FIELD_PREFERENCES).ids(), RuleSet.all(FIELD_PREFERENCES).without(List.of()).ids());
    }

    /// A misspelled id must not pass silently -- otherwise a user believes a rule is off while it
    /// goes on running.
    @Test
    void withoutRejectsAnIdThatNamesNoRule() {
        UnknownRuleException exception = assertThrows(UnknownRuleException.class,
                () -> RuleSet.all(FIELD_PREFERENCES).without(List.of("surounding-whitespace", "surrounding-whitespace")));

        assertEquals(List.of("surounding-whitespace"), exception.getUnknownIds());
        assertEquals(List.of("surrounding-whitespace", "repeated-whitespace"), exception.getKnownIds());
    }

    @Test
    void withAppendsARuleFromOutsideThisModule() {
        assertEquals(List.of("surrounding-whitespace", "repeated-whitespace", "external"),
                RuleSet.all(FIELD_PREFERENCES).with(EXTERNAL).ids());
    }

    @Test
    void anExternalRuleCanBeSwitchedOffByIdLikeAnyOther() throws UnknownRuleException {
        assertEquals(List.of("surrounding-whitespace", "repeated-whitespace"),
                RuleSet.all(FIELD_PREFERENCES).with(EXTERNAL).without(List.of("external")).ids());
    }

    @Test
    void ofTakesExactlyTheRulesGiven() {
        assertEquals(List.of("external"), RuleSet.of(EXTERNAL).ids());
    }
}
