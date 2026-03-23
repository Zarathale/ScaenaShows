package com.scaena.shows.model;

public sealed interface ShowTimelineEntry permits ShowTimelineEntry.SceneCall, ShowTimelineEntry.EventEntry {

    int atTick();

    record SceneCall(int atTick, String sceneId) implements ShowTimelineEntry {}

    // event is one of SceneEvent types (SOUND/PARTICLE/MESSAGE/etc) but with a top-level offset
    record EventEntry(int atTick, SceneEvent event) implements ShowTimelineEntry {}
}
