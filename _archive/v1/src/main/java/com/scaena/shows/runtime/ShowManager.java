package com.scaena.shows.runtime;

import com.scaena.shows.ScaenaShowsPlugin;
import com.scaena.shows.config.ConfigLoader;
import com.scaena.shows.config.ScaenaShowsConfig;
import com.scaena.shows.config.SoundSpec;
import com.scaena.shows.model.*;
import com.scaena.shows.registry.FireworkPresetRegistry;
import com.scaena.shows.registry.SceneRegistry;
import com.scaena.shows.registry.SequenceRegistry;
import com.scaena.shows.registry.ShowRegistry;
import com.scaena.shows.util.IdValidator;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class ShowManager {

    private final ScaenaShowsPlugin plugin;
    private final ConfigLoader configLoader;
    private final FireworkPresetRegistry fireworkPresets;
    private final SequenceRegistry sequences;
    private final SceneRegistry scenes;
    private final ShowRegistry shows;

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();

    // One active show per target player
    private final Map<UUID, RunningShow> running = new ConcurrentHashMap<>();

    // Cooldown per initiator
    private final Map<UUID, Long> lastPlayMillis = new ConcurrentHashMap<>();

    // Fireworks rate limiting
    private volatile long currentSecond = -1L;
    private volatile int fireworksThisSecond = 0;

    public ShowManager(ScaenaShowsPlugin plugin,
                       ConfigLoader configLoader,
                       FireworkPresetRegistry fireworkPresets,
                       SequenceRegistry sequences,
                       SceneRegistry scenes,
                       ShowRegistry shows) {
        this.plugin = plugin;
        this.configLoader = configLoader;
        this.fireworkPresets = fireworkPresets;
        this.sequences = sequences;
        this.scenes = scenes;
        this.shows = shows;
    }

    public Collection<String> listShowIds() {
        return new TreeSet<>(shows.all().keySet());
    }

    public boolean isRunning(UUID playerId) {
        return running.containsKey(playerId);
    }

    public RunningShow getRunning(UUID playerId) {
        return running.get(playerId);
    }

    public void tickAll() {
        for (Iterator<Map.Entry<UUID, RunningShow>> it = running.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, RunningShow> e = it.next();
            RunningShow rs = e.getValue();
            if (rs.isPaused()) {
                // Expire paused shows if outside window
                if (isPauseExpired(rs)) {
                    stopInternal(rs, "Show expired while you were offline.");
                    it.remove();
                }
                continue;
            }

            boolean done = rs.tick(this);
            if (done) {
                it.remove();
            }
        }
    }

    public void pause(Player player) {
        RunningShow rs = running.get(player.getUniqueId());
        if (rs == null) return;
        rs.pause();
    }

    public void resume(Player player) {
        RunningShow rs = running.get(player.getUniqueId());
        if (rs == null) return;

        if (isPauseExpired(rs)) {
            running.remove(player.getUniqueId());
            stopInternal(rs, "Show expired while you were offline.");
            return;
        }
        rs.resume();
        // Ensure bossbar re-shown to this player if private
        rs.ensureBossbarShownToRejoined(player);
    }

    public void showBossbarsToJoiner(Player joiner) {
        // For broadcast bossbars, show to newly joined player.
        for (RunningShow rs : running.values()) {
            rs.showBossbarToJoinerIfBroadcast(joiner);
        }
    }

    private boolean isPauseExpired(RunningShow rs) {
        ScaenaShowsConfig c = configLoader.get();
        long windowMillis = c.resumeWindowSeconds() * 1000L;
        return rs.getPausedAtMillis() > 0 && (System.currentTimeMillis() - rs.getPausedAtMillis()) > windowMillis;
    }

    public void reloadAll(CommandSenderLike sender) {
        plugin.reloadAll();
        validateAll(sender);
    }

    public void validateAll(CommandSenderLike sender) {
        // Cross-reference validation after registries load
        List<String> errs = new ArrayList<>();

        // Validate sequences: shot preset references presetId or sequenceId
        for (Sequence seq : sequences.all().values()) {
            for (Shot shot : seq.shots()) {
                if (!fireworkPresets.all().containsKey(shot.presetOrSequenceId())
                        && !sequences.all().containsKey(shot.presetOrSequenceId())) {
                    errs.add("Sequence '" + seq.id() + "' shot at " + shot.atTick() + " references unknown preset/sequence '" + shot.presetOrSequenceId() + "'");
                }
            }
        }

        // Validate scenes: SEQUENCE id exists; ITEM notify enforced at parse stage already.
        for (Scene scene : scenes.all().values()) {
            for (SceneEvent ev : scene.events()) {
                if (ev instanceof SceneEvent.SequenceEvent se) {
                    if (!sequences.all().containsKey(se.sequence().sequenceId())) {
                        errs.add("Scene '" + scene.id() + "' references unknown sequence '" + se.sequence().sequenceId() + "'");
                    }
                }
            }
        }

        // Validate shows: scene and sequence references exist
        for (ShowDefinition show : shows.all().values()) {
            for (ShowTimelineEntry te : show.timeline()) {
                if (te instanceof ShowTimelineEntry.SceneCall sc) {
                    if (!scenes.all().containsKey(sc.sceneId())) {
                        errs.add("Show '" + show.id() + "' references unknown scene '" + sc.sceneId() + "'");
                    }
                } else if (te instanceof ShowTimelineEntry.EventEntry ee) {
                    if (ee.event() instanceof SceneEvent.SequenceEvent se) {
                        if (!sequences.all().containsKey(se.sequence().sequenceId())) {
                            errs.add("Show '" + show.id() + "' references unknown sequence '" + se.sequence().sequenceId() + "'");
                        }
                    }
                }
            }
        }

        if (errs.isEmpty()) {
            sender.sendPlain("ScaenaShows reload OK. Loaded: " +
                    shows.all().size() + " shows, " +
                    scenes.all().size() + " scenes, " +
                    sequences.all().size() + " sequences, " +
                    fireworkPresets.all().size() + " presets.");
        } else {
            sender.sendPlain("ScaenaShows reload loaded with " + errs.size() + " validation issue(s):");
            for (String e : errs) sender.sendPlain(" - " + e);
        }
    }

    public void play(CommandSenderLike sender, Player initiatorOrNull, String showId, Collection<Player> targets, ShowMode mode, boolean privateFlag, boolean directorScenes) {
        ShowDefinition def = shows.get(showId);
        if (def == null) {
            sender.sendPlain("Unknown show '" + showId + "'. Try /show list.");
            return;
        }

        ScaenaShowsConfig c = configLoader.get();

        // Cooldown applies per initiator (the command runner)
        if (initiatorOrNull != null && !hasBypassCooldown(initiatorOrNull, c)) {
            long now = System.currentTimeMillis();
            long last = lastPlayMillis.getOrDefault(initiatorOrNull.getUniqueId(), 0L);
            long cdMillis = c.defaultCooldownSeconds() * 1000L;
            long remaining = (last + cdMillis) - now;
            if (remaining > 0) {
                sender.sendPlain("Cooldown: wait " + (remaining / 1000L) + "s before playing another show.");
                return;
            }
            lastPlayMillis.put(initiatorOrNull.getUniqueId(), now);
        }

        // Start show per target (one active show per player)
        int started = 0;
        for (Player target : targets) {
            UUID tid = target.getUniqueId();
            if (running.containsKey(tid)) {
                sender.sendPlain("Show already running for " + target.getName() + ". Use /show stop " + target.getName() + ".");
                continue;
            }

            ShowMode effectiveMode = mode != null ? mode : def.defaultMode();
            AudienceMode showAudience = privateFlag ? AudienceMode.PRIVATE : def.defaultAudience();

            RunningShow rs = RunningShow.create(this, def, target, effectiveMode, showAudience, directorScenes);
            running.put(tid, rs);
            started++;
        }

        if (started > 0) {
            sender.sendPlain("Started show '" + showId + "' for " + started + " target(s).");
        }
    }

    private boolean hasBypassCooldown(Player p, ScaenaShowsConfig c) {
        return p.hasPermission(c.adminPermission()) || p.hasPermission(c.cooldownBypassPermission());
    }

    public void stop(Player target) {
        RunningShow rs = running.remove(target.getUniqueId());
        if (rs == null) return;
        stopInternal(rs, null);
    }

    public void stopAll(String reason) {
        for (RunningShow rs : running.values()) {
            stopInternal(rs, reason);
        }
        running.clear();
    }

    public void stopTargets(CommandSenderLike sender, Collection<Player> targets) {
        int stopped = 0;
        for (Player t : targets) {
            RunningShow rs = running.remove(t.getUniqueId());
            if (rs != null) {
                stopInternal(rs, null);
                stopped++;
            }
        }
        sender.sendPlain("Stopped shows for " + stopped + " target(s).");
    }

    private void stopInternal(RunningShow rs, String reason) {
        rs.stopAndCleanup(this, true, reason);
    }

    // ------------------------------------------------------------------------
    // Event execution helpers (called by RunningShow)
    // ------------------------------------------------------------------------

    public Location resolveLocation(RunningShow rs) {
        if (rs.mode() == ShowMode.STATIC) return rs.origin().clone();
        Player p = Bukkit.getPlayer(rs.targetId());
        if (p == null) return rs.origin().clone();
        return p.getLocation().clone();
    }

    public Collection<Player> resolveAudience(RunningShow rs, AudienceMode requested) {
        AudienceMode effective = (rs.audienceMode() == AudienceMode.PRIVATE) ? AudienceMode.PRIVATE : requested;
        if (effective == AudienceMode.PRIVATE) {
            Player p = Bukkit.getPlayer(rs.targetId());
            return p != null ? List.of(p) : List.of();
        }
        // Bukkit returns Collection<? extends Player>
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public void sendMessage(RunningShow rs, AudienceMode audience, String miniMsg) {
        Component c = mm.deserialize(miniMsg == null ? "" : miniMsg);
        for (Player p : resolveAudience(rs, audience)) {
            p.sendMessage(c);
        }
    }

    public void sendTitle(RunningShow rs, TitlePayload payload) {
        Title title = Title.title(
                mm.deserialize(payload.titleMiniMessage() == null ? "" : payload.titleMiniMessage()),
                mm.deserialize(payload.subtitleMiniMessage() == null ? "" : payload.subtitleMiniMessage()),
                Title.Times.times(Duration.ofMillis(payload.fadeInTicks() * 50L), Duration.ofMillis(payload.stayTicks() * 50L), Duration.ofMillis(payload.fadeOutTicks() * 50L))
        );
        for (Player p : resolveAudience(rs, payload.audience())) {
            p.showTitle(title);
        }
    }

    /**
     * Scene-level title overlay shown for the remaining duration of the scene.
     * This is used for optional `scene_text` in scenes/*.yml.
     */
    public void showSceneTextOverlay(RunningShow rs, String sceneTextMiniMessage, int stayTicks) {
        if (sceneTextMiniMessage == null || sceneTextMiniMessage.isBlank()) return;
        int max = Math.max(1, config().sceneTextMaxChars());

        Component comp = mm.deserialize(sceneTextMiniMessage);
        String plainText = plain.serialize(comp);
        if (plainText.length() > max) {
            // Keep it safe: truncate the *visible* text and drop formatting.
            plainText = plainText.substring(0, max);
            comp = Component.text(plainText);
        }

        Title title = Title.title(
                comp,
                Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(Math.max(1, stayTicks) * 50L), Duration.ofMillis(10 * 50L))
        );
        for (Player p : resolveAudience(rs, AudienceMode.BROADCAST)) {
            p.showTitle(title);
        }
    }

    /**
     * Director mode label displayed at the start of each scene (action bar). Enabled by `--scenes`.
     */
    public void showSceneDirectorLabel(RunningShow rs, Scene scene) {
        if (scene == null) return;
        String msg = "<gray><i>[Scene]</i></gray> <white>" + escapeMini(scene.name()) + "</white> <dark_gray>(" + escapeMini(scene.id()) + ")</dark_gray>";
        Component comp = mm.deserialize(msg);
        for (Player p : resolveAudience(rs, AudienceMode.BROADCAST)) {
            p.sendActionBar(comp);
        }
        // Action bar fades quickly; re-assert once a second later for readability.
        enqueueLater(rs, rs.currentTick() + 20, () -> {
            for (Player p : resolveAudience(rs, AudienceMode.BROADCAST)) {
                p.sendActionBar(comp);
            }
        });
    }

    private String escapeMini(String s) {
        if (s == null) return "";
        return s.replace("<", "&lt;").replace(">", "&gt;");
    }

    private void enqueueLater(RunningShow rs, int atTick, Runnable r) {
        rs.enqueue(atTick, r);
    }

    public void playSound(RunningShow rs, SoundPayload payload) {
        if (payload == null) return;
        Location loc = resolveLocation(rs);
        SoundCategory cat;
        try {
            cat = SoundCategory.valueOf(payload.category().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            cat = SoundCategory.MASTER;
        }
        for (Player p : resolveAudience(rs, AudienceMode.BROADCAST)) {
            // sounds are inherently positional; but for private shows, we still want only the target to hear it.
            // So resolve audience using BROADCAST and show audience mode will clamp if needed.
            p.playSound(loc, payload.id(), cat, payload.volume(), payload.pitch());
        }
    }

    public void spawnParticle(RunningShow rs, ParticlePayload payload) {
        if (payload == null) return;
        Location loc = resolveLocation(rs);
        if (payload.id() == null) return;

        String raw = payload.id().contains(":") ? payload.id().substring(payload.id().indexOf(':') + 1) : payload.id();
        Particle particle;
        try {
            particle = Particle.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Invalid particle id: " + payload.id());
            return;
        }

        int count = Math.min(payload.count(), configLoader.get().maxParticlesPerEvent());
        loc.getWorld().spawnParticle(particle, loc, count, payload.ox(), payload.oy(), payload.oz(), payload.extra(), null, payload.force());
    }

    public void applyEffect(RunningShow rs, EffectPayload payload) {
        if (payload == null) return;
        Player p = Bukkit.getPlayer(rs.targetId());
        if (p == null) return;

        PotionEffectType type = IdValidator.parseKey(payload.id())
                .map(PotionEffectType::getByKey)
                .orElse(null);
        if (type == null) return;

        PotionEffect pe = new PotionEffect(type, payload.durationTicks(), payload.amplifier(), false, !payload.hideParticles(), !payload.hideParticles());
        p.addPotionEffect(pe);

        rs.trackEffect(type);
    }

    public void runSequence(RunningShow rs, int baseTick, String sequenceId, int depth) {
        if (depth > configLoader.get().maxSequenceNestingDepth()) {
            plugin.getLogger().warning("Sequence nesting exceeded max depth for '" + sequenceId + "'");
            return;
        }
        Sequence seq = sequences.get(sequenceId);
        if (seq == null) {
            plugin.getLogger().warning("Unknown sequence: " + sequenceId);
            return;
        }

        for (Shot shot : seq.shots()) {
            int at = baseTick + shot.atTick();
            rs.enqueue(at, () -> {
                // optional shot sound
                if (shot.sound() != null) playSound(rs, shot.sound());

                String ref = shot.presetOrSequenceId();
                if (sequences.get(ref) != null) {
                    runSequence(rs, baseTick + shot.atTick(), ref, depth + 1);
                    return;
                }

                FireworkPreset preset = fireworkPresets.get(ref);
                if (preset == null) {
                    plugin.getLogger().warning("Unknown firework preset '" + ref + "' in sequence '" + sequenceId + "'");
                    return;
                }

                int count = Math.min(configLoader.get().maxFireworksPerShot(), Math.max(1, shot.count()));
                Double spreadOverride = shot.spreadOverride();

                for (int i = 0; i < count; i++) {
                    if (!tryConsumeFirework()) return;
                    spawnFirework(rs, preset, spreadOverride);
                }
            });
        }
    }

    private void spawnFirework(RunningShow rs, FireworkPreset preset, Double spreadOverride) {
        Location base = resolveLocation(rs);
        Location spawn = base.clone();

        FireworkLaunch launch = preset.launch();
        if (launch.mode() == FireworkLaunch.Mode.ABOVE) {
            spawn.add(0, launch.yOffset(), 0);
        } else if (launch.mode() == FireworkLaunch.Mode.RANDOM) {
            double spread = spreadOverride != null ? spreadOverride : launch.spread();
            double r = Math.random() * spread;
            double theta = Math.random() * Math.PI * 2;
            spawn.add(Math.cos(theta) * r, launch.yOffset(), Math.sin(theta) * r);
        }

        World w = spawn.getWorld();
        if (w == null) return;

        w.spawn(spawn, Firework.class, fw -> {
            FireworkMeta meta = fw.getFireworkMeta();
            meta.setPower(Math.max(0, Math.min(3, preset.power())));
            FireworkEffect.Builder b = FireworkEffect.builder()
                    .with(preset.type())
                    .trail(preset.trail())
                    .flicker(preset.flicker())
                    .withColor(preset.colors());
            if (preset.fades() != null && !preset.fades().isEmpty()) b.withFade(preset.fades());
            meta.clearEffects();
            meta.addEffect(b.build());
            fw.setFireworkMeta(meta);
        });
    }

    private synchronized boolean tryConsumeFirework() {
        long sec = System.currentTimeMillis() / 1000L;
        if (sec != currentSecond) {
            currentSecond = sec;
            fireworksThisSecond = 0;
        }
        if (fireworksThisSecond >= configLoader.get().maxFireworksPerSecond()) return false;
        fireworksThisSecond++;
        return true;
    }

    public void grantItem(RunningShow rs, ItemEventPayload payload) {
        Player p = Bukkit.getPlayer(rs.targetId());
        if (p == null) return;

        ItemStack stack = buildItem(payload.item());
        ItemDelivery delivery = payload.delivery();

        boolean placed = false;
        if (delivery == ItemDelivery.INVENTORY_ONLY || delivery == ItemDelivery.INVENTORY_THEN_DROP) {
            Map<Integer, ItemStack> leftover = p.getInventory().addItem(stack);
            placed = leftover.isEmpty();
            if (!leftover.isEmpty() && delivery == ItemDelivery.INVENTORY_THEN_DROP) {
                for (ItemStack left : leftover.values()) {
                    p.getWorld().dropItemNaturally(p.getLocation(), left);
                }
            }
        } else if (delivery == ItemDelivery.DROP_ONLY) {
            p.getWorld().dropItemNaturally(p.getLocation(), stack);
        }

        // Notify (required)
        if (payload.notifyPayload() != null) {
            sendMessage(rs, AudienceMode.PRIVATE, payload.notifyPayload().messageMiniMessage());
            playSound(rs, payload.notifyPayload().sound());
        } else {
            // Should not happen due to validation
            sendMessage(rs, AudienceMode.PRIVATE, "<yellow>Item granted.</yellow>");
            p.playSound(p.getLocation(), "minecraft:entity.experience_orb.pickup", SoundCategory.MASTER, 1.0f, 1.0f);
        }
    }

    private ItemStack buildItem(ItemPayload item) {
        Material mat = Material.matchMaterial(item.material(), true);
        if (mat == null) mat = Material.STONE;
        ItemStack stack = new ItemStack(mat, Math.max(1, item.amount()));
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (item.nameMiniMessage() != null && !item.nameMiniMessage().isBlank()) {
                meta.displayName(mm.deserialize(item.nameMiniMessage()));
            }
            if (item.loreMiniMessage() != null && !item.loreMiniMessage().isEmpty()) {
                List<Component> lore = new ArrayList<>();
                for (String line : item.loreMiniMessage()) lore.add(mm.deserialize(line));
                meta.lore(lore);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public void stopSafety(Player p) {
        ScaenaShowsConfig c = configLoader.get();

        p.removePotionEffect(PotionEffectType.LEVITATION);

        int slowFallTicks = c.stopSafetySlowFallingSeconds() * 20;
        if (slowFallTicks > 0) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, slowFallTicks, 0, false, false, true));
        }

        if (c.stopMessageMiniMessage() != null) {
            p.sendMessage(mm.deserialize(c.stopMessageMiniMessage()));
        }
        SoundSpec ss = c.stopSound();
        if (ss != null && IdValidator.validSoundId(ss.id())) {
            SoundCategory cat;
            try {
                cat = SoundCategory.valueOf(ss.category().toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                cat = SoundCategory.MASTER;
            }
            p.playSound(p.getLocation(), ss.id(), cat, ss.volume(), ss.pitch());
        }
    }

    public ScaenaShowsConfig config() {
        return configLoader.get();
    }

    public SequenceRegistry sequences() { return sequences; }
    public SceneRegistry scenes() { return scenes; }
    public FireworkPresetRegistry presets() { return fireworkPresets; }
    public ShowRegistry shows() { return shows; }
}
