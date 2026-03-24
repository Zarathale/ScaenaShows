package com.scaena.shows.model;

public record FireworkLaunch(Mode mode, double yOffset, double spread) {
    public enum Mode { FEET, ABOVE, RANDOM }
}
