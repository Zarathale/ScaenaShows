package com.scaena.shows.model.event;

import java.util.Map;

/** §6.2 — SOUND */
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
}
