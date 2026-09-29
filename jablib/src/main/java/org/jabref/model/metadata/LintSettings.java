package org.jabref.model.metadata;

import java.util.Map;

import org.jspecify.annotations.NullMarked;

/// What a library asks JabFix to do when it is saved.
///
/// This is the configuration, not the rules themselves: which rules a run ends up applying is
/// derived from it, together with the library's Save Actions and the rules JabFix ships with.
/// A library that carries no settings at all is saved the way it always was.
///
/// A rule the library says nothing about is repaired, so a library only has to name what it wants
/// differently. One entry per rule is also where the values a rule takes will go, once rules take
/// any: they belong to the rule they configure, not to a list beside it.
///
/// @param enabled   whether the rules are applied at all. A library can keep its settings and still
///                  switch the whole of JabFix off, as it can for its Save Actions.
/// @param ruleModes how far each named rule goes, by [org.jabref.logic.lint.rule.Rule#id]. An id
///                  naming no rule of this JabFix is passed over rather than rejected, so that a
///                  library configured by a newer JabFix still saves with an older one.
@NullMarked
public record LintSettings(boolean enabled, Map<String, RuleMode> ruleModes) {
    /// Every rule of the run, each repairing what it finds.
    public static final LintSettings ENABLED = new LintSettings(true, Map.of());

    public LintSettings {
        ruleModes = Map.copyOf(ruleModes);
    }

    /// How far the rule with this id goes. A rule the library does not name repairs what it finds.
    public RuleMode modeOf(String ruleId) {
        return ruleModes.getOrDefault(ruleId, RuleMode.FIX);
    }
}
