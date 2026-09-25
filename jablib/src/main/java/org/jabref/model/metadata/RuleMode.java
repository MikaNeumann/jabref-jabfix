package org.jabref.model.metadata;

import org.jspecify.annotations.NullMarked;

/// How far a library lets one JabFix rule go when it is saved.
@NullMarked
public enum RuleMode {
    /// The rule does not run: nothing is reported and nothing is changed.
    OFF,

    /// The rule reports what it finds and changes nothing, even where it knows the repair.
    CHECK,

    /// The rule reports what it finds and repairs it, which is what a rule does unless the library
    /// says otherwise.
    FIX
}
