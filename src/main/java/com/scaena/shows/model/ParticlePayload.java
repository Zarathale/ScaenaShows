package com.scaena.shows.model;

public record ParticlePayload(String id, int count, double ox, double oy, double oz, double extra, boolean force) {}
