package com.scaena.shows.registry;

import com.scaena.shows.model.*;
import com.scaena.shows.util.IdValidator;

import java.util.*;

final class SceneEventParser {

    private SceneEventParser() {}

    static SceneEvent parse(int at, String type, Map<?, ?> m, String ctx, List<String> errors) {
        switch (type.toUpperCase(Locale.ROOT)) {
            case "SOUND" -> {
                Map<?, ?> sound = map(m.get("sound"));
                if (sound == null) { errors.add(ctx + ": missing sound"); return null; }
                SoundPayload s = parseSound(sound, ctx + ".sound", errors);
                return s == null ? null : new SceneEvent.SoundEvent(at, s);
            }
            case "PARTICLE" -> {
                Map<?, ?> p = map(m.get("particle"));
                if (p == null) { errors.add(ctx + ": missing particle"); return null; }
                String id = str(p.get("id"));
                int count = intOr(p.get("count"), 1);
                List<?> off = p.get("offset") instanceof List<?> l ? l : List.of(0,0,0);
                double ox = off.size() > 0 ? dblOr(off.get(0),0) : 0;
                double oy = off.size() > 1 ? dblOr(off.get(1),0) : 0;
                double oz = off.size() > 2 ? dblOr(off.get(2),0) : 0;
                double extra = dblOr(p.get("extra"), 0);
                boolean force = boolOr(p.get("force"), false);
                if (id == null || !IdValidator.validParticle(id)) errors.add(ctx + ": invalid particle id '" + id + "'");
                return new SceneEvent.ParticleEvent(at, new ParticlePayload(id, count, ox, oy, oz, extra, force));
            }
            case "MESSAGE" -> {
                AudienceMode a = AudienceMode.fromString(str(m.get("audience")), AudienceMode.BROADCAST);
                String msg = str(m.get("message"));
                if (msg == null) { errors.add(ctx + ": missing message"); return null; }
                return new SceneEvent.MessageEvent(at, new MessagePayload(a, msg));
            }
            case "TITLE" -> {
                AudienceMode a = AudienceMode.fromString(str(m.get("audience")), AudienceMode.BROADCAST);
                Map<?, ?> t = map(m.get("title"));
                if (t == null) { errors.add(ctx + ": missing title payload"); return null; }
                String title = str(t.get("title"));
                String sub = str(t.get("subtitle"));
                int fi = intOr(t.get("fade_in"), 10);
                int st = intOr(t.get("stay"), 40);
                int fo = intOr(t.get("fade_out"), 10);
                return new SceneEvent.TitleEvent(at, new TitlePayload(a, title == null ? "" : title, sub == null ? "" : sub, fi, st, fo));
            }
            case "BOSSBAR" -> {
                AudienceMode a = AudienceMode.fromString(str(m.get("audience")), AudienceMode.BROADCAST);
                Map<?, ?> b = map(m.get("bossbar"));
                if (b == null) { errors.add(ctx + ": missing bossbar payload"); return null; }
                String action = str(b.get("action"));
                BossbarPayload.Action act = "hide".equalsIgnoreCase(action) ? BossbarPayload.Action.HIDE : BossbarPayload.Action.SHOW;
                String title = str(b.get("title"));
                String color = str(b.get("color"));
                String overlay = str(b.get("overlay"));
                return new SceneEvent.BossbarEvent(at, new BossbarPayload(a, act, title, color, overlay));
            }
            case "EFFECT" -> {
                Map<?, ?> e = map(m.get("effect"));
                if (e == null) { errors.add(ctx + ": missing effect"); return null; }
                String id = str(e.get("id"));
                int dur = intOr(e.get("duration_ticks"), 40);
                int amp = intOr(e.get("amplifier"), 0);
                boolean hide = boolOr(e.get("hide_particles"), false);
                if (id == null || !IdValidator.validPotion(id)) errors.add(ctx + ": invalid potion effect '" + id + "'");
                return new SceneEvent.EffectEvent(at, new EffectPayload(id, dur, amp, hide));
            }
            case "SEQUENCE" -> {
                String sid = str(m.get("sequence"));
                if (sid == null) { errors.add(ctx + ": missing sequence id"); return null; }
                return new SceneEvent.SequenceEvent(at, new SequenceRefPayload(sid));
            }
            case "ITEM" -> {
                Map<?, ?> itemMap = map(m.get("item"));
                if (itemMap == null) { errors.add(ctx + ": missing item"); return null; }
                String mat = str(itemMap.get("material"));
                int amt = intOr(itemMap.get("amount"), 1);
                String name = str(itemMap.get("name"));
                List<String> lore = listStr(itemMap.get("lore"));
                if (mat == null || !IdValidator.validMaterial(mat)) errors.add(ctx + ": invalid material '" + mat + "'");

                Map<?, ?> deliveryMap = map(m.get("delivery"));
                ItemDelivery delivery = ItemDelivery.fromString(str(deliveryMap != null ? deliveryMap.get("method") : m.get("delivery")));
                Map<?, ?> notifyMap = map(m.get("notify"));
                if (notifyMap == null) { errors.add(ctx + ": ITEM requires notify"); return null; }
                String nmsg = str(notifyMap.get("message"));
                Map<?, ?> nsound = map(notifyMap.get("sound"));
                SoundPayload snd = nsound == null ? null : parseSound(nsound, ctx + ".notify.sound", errors);
                if (nmsg == null || nmsg.isBlank()) errors.add(ctx + ": ITEM notify.message required");
                if (snd == null) errors.add(ctx + ": ITEM notify.sound required");

                return new SceneEvent.ItemEvent(at, new ItemEventPayload(
                        new ItemPayload(mat, amt, name, lore),
                        delivery,
                        new ItemNotifyPayload(nmsg, snd)
                ));
            }
            default -> {
                errors.add(ctx + ": unknown type '" + type + "'");
                return null;
            }
        }
    }

    private static SoundPayload parseSound(Map<?, ?> sound, String ctx, List<String> errors) {
        String id = str(sound.get("id"));
        Object catObj = sound.containsKey("category") ? sound.get("category") : "master";
        Object volObj = sound.containsKey("volume") ? sound.get("volume") : 1.0;
        Object pitObj = sound.containsKey("pitch") ? sound.get("pitch") : 1.0;
        String cat = str(catObj);
        float vol = (float) dblOr(volObj, 1.0);
        float pit = (float) dblOr(pitObj, 1.0);
        if (id == null || !IdValidator.validSoundId(id)) {
            errors.add(ctx + ": invalid sound id '" + id + "'");
            return null;
        }
        return new SoundPayload(id, cat == null ? "master" : cat, vol, pit);
    }

    private static Map<?, ?> map(Object o) { return (o instanceof Map<?, ?> m) ? m : null; }
    private static String str(Object o) { return o == null ? null : String.valueOf(o); }

    private static Integer intObj(Object o) {
        if (o instanceof Number n) return n.intValue();
        try { return o == null ? null : Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private static int intOr(Object o, int def) {
        Integer v = intObj(o);
        return v == null ? def : v;
    }

    private static double dblOr(Object o, double def) {
        if (o instanceof Number n) return n.doubleValue();
        try { return o == null ? def : Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return def; }
    }

    private static boolean boolOr(Object o, boolean def) {
        if (o instanceof Boolean b) return b;
        if (o == null) return def;
        return Boolean.parseBoolean(String.valueOf(o));
    }

    @SuppressWarnings("unchecked")
    private static List<String> listStr(Object o) {
        if (o instanceof List<?> l) {
            List<String> out = new ArrayList<>();
            for (Object x : l) out.add(String.valueOf(x));
            return out;
        }
        return List.of();
    }
}
