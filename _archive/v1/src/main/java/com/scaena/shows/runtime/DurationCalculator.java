package com.scaena.shows.runtime;

import com.scaena.shows.model.*;
import com.scaena.shows.registry.SequenceRegistry;

import java.util.Comparator;

final class DurationCalculator {

    private DurationCalculator() {}

    static int calculate(ShowManager mgr, ShowDefinition show) {
        int max = 0;

        for (ShowTimelineEntry te : show.timeline()) {
            int at = te.atTick();

            if (te instanceof ShowTimelineEntry.SceneCall sc) {
                Scene scene = mgr.scenes().get(sc.sceneId());
                int dur = scene != null ? scene.durationTicks() : 0;
                if (dur <= 0 && scene != null && !scene.events().isEmpty()) {
                    dur = scene.events().stream().map(SceneEvent::atTick).max(Comparator.naturalOrder()).orElse(0);
                }
                max = Math.max(max, at + dur);
            } else if (te instanceof ShowTimelineEntry.EventEntry ee) {
                SceneEvent ev = ee.event();
                if (ev instanceof SceneEvent.SequenceEvent se) {
                    max = Math.max(max, at + sequenceDuration(mgr.sequences(), se.sequence().sequenceId()));
                } else {
                    // For other events, treat as instantaneous at 'at'
                    max = Math.max(max, at);
                }
            }
        }

        // small cushion so last-tick events still display bossbar at 100%
        return Math.max(1, max + 1);
    }

    private static int sequenceDuration(SequenceRegistry sequences, String seqId) {
        Sequence s = sequences.get(seqId);
        if (s == null) return 0;
        if (s.durationTicks() > 0) return s.durationTicks();
        if (s.shots().isEmpty()) return 0;
        int last = s.shots().stream().map(Shot::atTick).max(Comparator.naturalOrder()).orElse(0);
        return last + 1;
    }
}
