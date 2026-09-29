package org.jabref.model.metadata;

import org.jspecify.annotations.NullMarked;

/// How far a library lets one JabFix rule go when it is saved.
@NullMarked
public enum RuleMode {
    /// The rule does not run: nothing is reported and nothing is changed. This is what a rule the
    /// library does not name does, so naming one here says it out loud.
    OFF,

    /// The rule reports what it finds and changes nothing, even where it knows the repair.
    CHECK,

    /// The rule reports what it finds and repairs it.
    FIX
}
