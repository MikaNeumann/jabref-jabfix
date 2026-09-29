package org.jabref.logic.lint.rule;

import java.util.List;

import org.jabref.model.entry.BibEntry;

import org.jspecify.annotations.NullMarked;

/// One independently configurable thing JabFix has an opinion about.
///
/// A rule only ever *reports*: [#scan] inspects an entry and returns a [Finding] for everything it
/// objects to, leaving the entry untouched. Repairing is not a second method but a [Fix] attached
/// to the finding, so a rule cannot silently change something it did not report, and `--check` and
/// the actual formatting can never drift apart. A rule that can detect a problem but not repair it
/// simply attaches no fix.
///
/// Implementations must be stateless: one instance is shared across every entry of every library.
///
/// Rules run as a pipeline, in the order of the [RuleSet], each seeing what the rules before it
/// left behind. A rule must be idempotent with respect to its own output -- scanning a value it has
/// already repaired must report nothing -- which is what lets [org.jabref.logic.lint.JabFix] run every
/// rule exactly once instead of iterating to a fixed point.
@NullMarked
public interface Rule {

    /// Stable identifier, in kebab-case. Used to switch the rule on and off in configuration and to
    /// name it in reports, so it must not change once the rule has been released.
    String id();

    /// One sentence on what the rule enforces, for `--help`-style listings.
    String description();

    /// Reports everything this rule objects to in `entry`, without modifying it.
    ///
    /// @return the findings, in the order they occur in the entry; empty when the entry is clean
    List<Finding> scan(BibEntry entry);
}
