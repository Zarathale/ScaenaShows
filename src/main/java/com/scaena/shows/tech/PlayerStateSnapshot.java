package com.scaena.shows.tech;

import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Snapshot of a player's state taken at TechSession entry.
 * Restored in full on DISMISS so tech mode leaves no trace.
 *
 * hotbarItems: copies of slots 4–8 (the 5 slots tech mode occupies).
 * heldItemSlot: the slot the player had selected before entering tech.
 */
public record PlayerStateSnapshot(
    GameMode    gameMode,
    boolean     allowFlight,
    boolean     flying,
    ItemStack[] hotbarItems,    // copies of slots 4–8 (indices 4–8 in player inventory)
    int         heldItemSlot,
    Scoreboard  scoreboard
) {}
