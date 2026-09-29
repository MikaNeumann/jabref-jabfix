package org.jabref.model.metadata;

import java.util.Map;

import org.jspecify.annotations.NullMarked;

/// What a library asks JabFix to do when it is saved.
///
/// This is the configuration, not the rules themselves: which rules a run ends up applying is
/// derived from it, together with the library's Save Actions and the rules JabFix ships with.
///
/// A rule runs only where the library asks for it. Carrying these settings at all is what asks for
/// anything to be done, and within them a rule that is not named does not run -- naming one
/// [RuleMode#OFF] says the same thing out loud, where a library wants to record that it is
/// deliberately not run. A library that carries no settings is saved the way it always was.
///
/// One entry per rule is also where the values a rule takes will go, once rules take any: they
/// belong to the rule they configure, not to a list beside it.
///
/// @param ruleModes how far each named rule goes, by [org.jabref.logic.lint.rule.Rule#id]. An id
///                  naming no rule of this JabFix is passed over rather than rejected, so that a
///                  library configured by a newer JabFix still saves with an older one.
@NullMarked
public record LintSettings(Map<String, RuleMode> ruleModes) {
    public LintSettings {
        ruleModes = Map.copyOf(ruleModes);
    }

    /// How far the rule with this id goes. A rule the library does not name does not run.
    public RuleMode modeOf(String ruleId) {
        return ruleModes.getOrDefault(ruleId, RuleMode.OFF);
    }
}
