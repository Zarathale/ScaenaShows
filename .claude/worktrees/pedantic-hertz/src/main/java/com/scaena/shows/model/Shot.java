package com.scaena.shows.model;

public record Shot(
        int atTick,
        String presetOrSequenceId,
        int count,
        Double spreadOverride,
        SoundPayload sound
) {}
