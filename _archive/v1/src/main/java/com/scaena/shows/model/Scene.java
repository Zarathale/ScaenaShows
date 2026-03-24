package com.scaena.shows.model;

import java.util.List;

public record Scene(
        String id,
        String name,
        /**
         * Optional scene-wide title overlay text (MiniMessage). If provided, it is shown for the scene duration
         * in the Minecraft title area (center screen).
         */
        String sceneTextMiniMessage,
        int durationTicks,
        List<SceneEvent> events
) {}
