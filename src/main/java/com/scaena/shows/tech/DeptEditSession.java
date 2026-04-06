package com.scaena.shows.tech;

/**
 * Interface for per-department edit sessions in Phase 2.
 *
 * Each department implements its own session (Casting, Wardrobe, Sound, ...).
 * The interface provides the save/cancel contract shared by all departments.
 *
 * Group 5 implements concrete sessions, one per department, in priority order
 * (simplest → most complex). This interface is the only coupling between
 * TechCueSession and the department-specific edit logic.
 */
public interface DeptEditSession {
    /** The cue ID being edited (e.g. "casting.zombie.warrior_enter"). */
    String cueId();

    /** The department this session belongs to (e.g. "casting"). */
    String department();

    /** Player clicked [Save] — apply changes, close edit session. */
    void onSave();

    /** Player clicked [Save as Preset] — apply changes and promote to cues/*.yml. */
    void onSaveAsPreset();

    /** Player clicked [Cancel] — discard all edits, restore entry state. */
    void onCancel();
}
