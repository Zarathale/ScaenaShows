package com.scaena.shows.model;

import java.util.Map;

/** Authorship block present on every Cue and Show. */
public record CueMeta(
    String createdBy,
    String createdAt,
    String lastEditedBy,
    String lastEditedAt
) {
    @SuppressWarnings("unchecked")
    public static CueMeta from(Object raw) {
        if (!(raw instanceof Map<?, ?> mRaw)) return empty();
        Map<String, Object> m = (Map<String, Object>) mRaw;
        return new CueMeta(
            str(m, "created_by", ""),
            str(m, "created_at", ""),
            str(m, "last_edited_by", ""),
            str(m, "last_edited_at", "")
        );
    }

    public static CueMeta empty() {
        return new CueMeta("", "", "", "");
    }

    private static String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return v != null ? v.toString() : def;
    }
}
