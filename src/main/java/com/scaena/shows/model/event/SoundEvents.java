package com.scaena.shows.model.event;

import java.util.Map;

/** §6.2 — SOUND and STOP_SOUND */
public final class SoundEvents {

    private SoundEvents() {}

    public static final class SoundEvent extends ShowEvent {
        public final String soundId;
        public final String category;
        public final float volume;
        public final float pitch;
        public final int maxDurationTicks; // -1 = no truncation

        public SoundEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.soundId          = str(m, "sound_id", "minecraft:block.note_block.harp");
            this.category         = str(m, "category", "master");
            this.volume           = fltVal(m, "volume", 1.0f);
            this.pitch            = fltVal(m, "pitch", 1.0f);
            this.maxDurationTicks = intVal(m, "max_duration_ticks", -1);
        }

        @Override public EventType type() { return EventType.SOUND; }
    }

    /**
     * STOP_SOUND — immediately cuts all sounds from the specified source for each participant.
     *
     * The stop is abrupt (no fade); pair with a transitional sound (chime, boom, thunder)
     * at the same tick so the cut feels intentional rather than broken.
     *
     * source values: music | ambient | neutral | player | block | hostile | master | all
     *   "music"   — stops the background music track (most common use)
     *   "ambient"  — stops ambient/cave sounds
     *   "neutral"  — stops neutral mob sounds (chickens, etc.)
     *   "all"      — stops every sound source simultaneously
     */
    public static final class StopSoundEvent extends ShowEvent {
        public final String source; // Adventure Sound.Source name, or "all"

        public StopSoundEvent(Map<String, Object> m) {
            super(intVal(m, "at", 0));
            this.source = str(m, "source", "music");
        }

        @Override public EventType type() { return EventType.STOP_SOUND; }
    }
}
