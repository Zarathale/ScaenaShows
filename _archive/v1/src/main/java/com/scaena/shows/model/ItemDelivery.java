package com.scaena.shows.model;

public enum ItemDelivery {
    INVENTORY_ONLY,
    INVENTORY_THEN_DROP,
    DROP_ONLY;

    public static ItemDelivery fromString(String s) {
        if (s == null) return INVENTORY_ONLY;
        return switch (s.toLowerCase()) {
            case "inventory_only" -> INVENTORY_ONLY;
            case "inventory_then_drop" -> INVENTORY_THEN_DROP;
            case "drop_only" -> DROP_ONLY;
            default -> INVENTORY_ONLY;
        };
    }
}
