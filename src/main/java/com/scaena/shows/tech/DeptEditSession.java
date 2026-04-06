package com.scaena.shows.tech;

/**
 * Interface for per-department Phase 2 edit sessions.
 *
 * Each department that supports in-panel editing implements this interface.
 * The TechCueSession holds at most one active DeptEditSession at a time.
 * Entry: player clicks [Edit ▸] in the Phase 2 cue panel.
 * Exit: player clicks [Save], [Save as Preset], or [Cancel].
 *
 * The universal edit shell (boss bar, periodic save/cancel button re-send,
 * hotbar suspension) is managed by CuePanelBuilder / TechManager, not here.
 */
public interface DeptEditSession {

    /** The fully-qualified cue ID being edited (e.g. "casting.zombie.warrior_enter"). */
    String cueId();

    /** The department slug owning this session (e.g. "casting", "sound"). */
    String department();

    /**
     * Commit changes to the show YAML via the session's ShowYamlEditor.
     * Called when the player clicks [Save].
     */
    void onSave();

    /**
     * Commit changes and promote the cue content to the preset library
     * (cues/*.yml) with an auto-generated ID.
     * Called when the player clicks [Save as Preset].
     */
    void onSaveAsPreset();

    /**
     * Discard all in-session edits and restore the entry state.
     * Called when the player clicks [Cancel].
     */
    void onCancel();
}
