package com.scaena.shows.model;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;

import java.util.List;

public record FireworkPreset(
        String id,
        String displayName,
        int power,
        FireworkEffect.Type type,
        List<Color> colors,
        List<Color> fades,
        boolean trail,
        boolean flicker,
        FireworkLaunch launch
) {}
