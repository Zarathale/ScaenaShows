package com.scaena.shows.runtime;

import com.scaena.shows.model.*;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class RunningShow {

    private final UUID targetId;
    private final String showId;
    private final ShowMode mode;
    private final AudienceMode audienceMode;
    private final Location origin;

    private final boolean directorScenes;

    private final int durationTicks;

    private int currentTick = 0;
    private boolean paused = false;
    private long pausedAtMillis = -1L;

    // tick -> list of actions (SceneEvent or SceneCall marker)
    private final Map<Integer, List<Object>> scheduled = new HashMap<>();

    private BossBar bossBar;
    private AudienceMode bossBarAudience;
    private boolean bossBarVisible = false;

    private final Set<PotionEffectType> effectsApplied = new HashSet<>();

    // Scene-wide title overlay support
    private String activeSceneTextMiniMessage = null;
    private int activeSceneTextEndsAtTick = -1;
    private int titleLockUntilTick = -1;
    private boolean sceneTextNeedsResend = false;

    private final MiniMessage mm = MiniMessage.miniMessage();

    private RunningShow(UUID targetId, String showId, ShowMode mode, AudienceMode audienceMode, Location origin, int durationTicks, boolean directorScenes) {
        this.targetId = targetId;
        this.showId = showId;
        this.mode = mode;
        this.audienceMode = audienceMode;
        this.origin = origin;
        this.durationTicks = Math.max(1, durationTicks);
        this.directorScenes = directorScenes;
    }

    public static RunningShow create(ShowManager mgr, ShowDefinition def, Player target, ShowMode mode, AudienceMode audienceMode, boolean directorScenes) {
        int duration = DurationCalculator.calculate(mgr, def);
        RunningShow rs = new RunningShow(target.getUniqueId(), def.id(), mode, audienceMode, target.getLocation().clone(), duration, directorScenes);

        // show-level bossbar
        if (def.bossbar() != null && def.bossbar().enabled()) {
            rs.createBossbar(def.bossbar(), audienceMode, target);
        }

        rs.scheduleTimeline(mgr, def);
        return rs;
    }

    private void createBossbar(ShowBossbarDefaults b, AudienceMode showAudienceMode, Player target) {
        BossBar.Color color = BossBar.Color.YELLOW;
        BossBar.Overlay overlay = BossBar.Overlay.PROGRESS;
        try { color = BossBar.Color.valueOf(b.color().toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
        try { overlay = BossBar.Overlay.valueOf(b.overlay().toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}

        this.bossBar = BossBar.bossBar(mm.deserialize(b.titleMiniMessage() == null ? "" : b.titleMiniMessage()), 0f, color, overlay);
        this.bossBarAudience = (showAudienceMode == AudienceMode.PRIVATE) ? AudienceMode.PRIVATE : b.audience();
        this.bossBarVisible = true;

        if (bossBarAudience == AudienceMode.PRIVATE) {
            target.showBossBar(bossBar);
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) p.showBossBar(bossBar);
        }
    }

    private void scheduleTimeline(ShowManager mgr, ShowDefinition def) {
        for (ShowTimelineEntry te : def.timeline()) {
            if (te instanceof ShowTimelineEntry.SceneCall sc) {
                addSceneCallAt(sc.atTick(), sc.sceneId());
            } else if (te instanceof ShowTimelineEntry.EventEntry ee) {
                scheduleEventAt(te.atTick(), ee.event());
            }
        }
    }

    private void scheduleEventAt(int baseTick, SceneEvent ev) {
        if (ev instanceof SceneEvent.SoundEvent se) addAt(baseTick + se.atTick(), se);
        else if (ev instanceof SceneEvent.ParticleEvent pe) addAt(baseTick + pe.atTick(), pe);
        else if (ev instanceof SceneEvent.MessageEvent me) addAt(baseTick + me.atTick(), me);
        else if (ev instanceof SceneEvent.TitleEvent te) addAt(baseTick + te.atTick(), te);
        else if (ev instanceof SceneEvent.BossbarEvent be) addAt(baseTick + be.atTick(), be);
        else if (ev instanceof SceneEvent.EffectEvent ee) addAt(baseTick + ee.atTick(), ee);
        else if (ev instanceof SceneEvent.SequenceEvent se) addAt(baseTick + se.atTick(), se);
        else if (ev instanceof SceneEvent.ItemEvent ie) addAt(baseTick + ie.atTick(), ie);
    }

    private void addAt(int tick, Object obj) {
        scheduled.computeIfAbsent(tick, k -> new ArrayList<>()).add(obj);
    }

    /**
     * Schedule an arbitrary action to run at a given absolute tick of this show.
     */
    public void enqueue(int atTick, Runnable action) {
        if (action == null) return;
        addAt(atTick, action);
    }

    private void addSceneCallAt(int tick, String sceneId) {
        addAt(tick, new SceneCall(sceneId));
    }

    public boolean tick(ShowManager mgr) {
        // Update bossbar progress first
        if (bossBar != null && bossBarVisible) {
            float prog = Math.min(1f, (float) currentTick / (float) durationTicks);
            bossBar.progress(prog);
        }

        // Execute scheduled things
        List<Object> list = scheduled.remove(currentTick);
        if (list != null) {
            for (Object o : list) {
                if (o instanceof Runnable r) {
                    try { r.run(); } catch (Exception ignored) {}
                    continue;
                }
                if (o instanceof SceneCall call) {
                    // Expand scene events into the schedule relative to *this tick*
                    Scene scene = mgr.scenes().get(call.sceneId);
                    if (scene != null) {
                        onSceneStart(mgr, scene);
                        for (SceneEvent ev : scene.events()) {
                            scheduleEventAt(currentTick, ev);
                        }
                    }
                } else if (o instanceof SceneEvent.SoundEvent se) {
                    mgr.playSound(this, se.sound());
                } else if (o instanceof SceneEvent.ParticleEvent pe) {
                    mgr.spawnParticle(this, pe.particle());
                } else if (o instanceof SceneEvent.MessageEvent me) {
                    mgr.sendMessage(this, me.message().audience(), me.message().miniMessage());
                } else if (o instanceof SceneEvent.TitleEvent te) {
                    // Lock scene text overlay until this title naturally completes.
                    TitlePayload tp = te.title();
                    if (tp != null) {
                        int lock = Math.max(0, tp.fadeInTicks()) + Math.max(0, tp.stayTicks()) + Math.max(0, tp.fadeOutTicks());
                        titleLockUntilTick = Math.max(titleLockUntilTick, currentTick + lock);
                        sceneTextNeedsResend = true;
                    }
                    mgr.sendTitle(this, te.title());
                } else if (o instanceof SceneEvent.BossbarEvent be) {
                    handleBossbarEvent(be.bossbar());
                } else if (o instanceof SceneEvent.EffectEvent ee) {
                    mgr.applyEffect(this, ee.effect());
                } else if (o instanceof SceneEvent.SequenceEvent se) {
                    mgr.runSequence(this, currentTick, se.sequence().sequenceId(), 0);
                } else if (o instanceof SceneEvent.ItemEvent ie) {
                    mgr.grantItem(this, ie.item());
                }
            }
        }

        // Scene-wide overlay title (re)assertion
        if (activeSceneTextMiniMessage != null && currentTick < activeSceneTextEndsAtTick) {
            if (currentTick >= titleLockUntilTick && sceneTextNeedsResend) {
                int remaining = Math.max(1, activeSceneTextEndsAtTick - currentTick);
                mgr.showSceneTextOverlay(this, activeSceneTextMiniMessage, remaining);
                sceneTextNeedsResend = false;
            }
        }

        currentTick++;
        if (currentTick > durationTicks) {
            stopAndCleanup(mgr, false, null);
            return true;
        }
        return false;
    }

    private void onSceneStart(ShowManager mgr, Scene scene) {
        if (directorScenes) {
            mgr.showSceneDirectorLabel(this, scene);
        }

        String st = scene.sceneTextMiniMessage();
        if (st != null && !st.isBlank() && scene.durationTicks() > 0) {
            activeSceneTextMiniMessage = st;
            activeSceneTextEndsAtTick = currentTick + scene.durationTicks();
            // If a title is currently active, wait; otherwise show immediately.
            if (currentTick >= titleLockUntilTick) {
                mgr.showSceneTextOverlay(this, st, scene.durationTicks());
                sceneTextNeedsResend = false;
            } else {
                sceneTextNeedsResend = true;
            }
        } else {
            activeSceneTextMiniMessage = null;
            activeSceneTextEndsAtTick = -1;
            sceneTextNeedsResend = false;
        }
    }

    private void handleBossbarEvent(BossbarPayload bb) {
        if (bb.action() == BossbarPayload.Action.HIDE) {
            hideBossbar();
            return;
        }

        // Ensure bossbar exists
        if (bossBar == null) {
            BossBar.Color color = BossBar.Color.YELLOW;
            BossBar.Overlay overlay = BossBar.Overlay.PROGRESS;
            try { color = BossBar.Color.valueOf(bb.color().toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
            try { overlay = BossBar.Overlay.valueOf(bb.overlay().toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
            bossBar = BossBar.bossBar(mm.deserialize(bb.titleMiniMessage() == null ? "" : bb.titleMiniMessage()), 0f, color, overlay);
            bossBarAudience = (audienceMode == AudienceMode.PRIVATE) ? AudienceMode.PRIVATE : bb.audience();
        } else if (bb.titleMiniMessage() != null) {
            bossBar.name(mm.deserialize(bb.titleMiniMessage()));
        }

        showBossbar();
    }

    private void showBossbar() {
        if (bossBar == null) return;
        bossBarVisible = true;
        if (bossBarAudience == AudienceMode.PRIVATE) {
            Player t = Bukkit.getPlayer(targetId);
            if (t != null) t.showBossBar(bossBar);
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) p.showBossBar(bossBar);
        }
    }

    private void hideBossbar() {
        if (bossBar == null || !bossBarVisible) return;
        bossBarVisible = false;
        for (Player p : Bukkit.getOnlinePlayers()) p.hideBossBar(bossBar);
    }

    public void stopAndCleanup(ShowManager mgr, boolean forced, String reason) {
        hideBossbar();
        Player p = Bukkit.getPlayer(targetId);
        if (p == null) return;
        // Minimal cleanup on natural end; full stop-safety on forced stop.
        if (forced) {
            mgr.stopSafety(p);
        } else {
            if (effectsApplied.contains(PotionEffectType.LEVITATION)) {
                p.removePotionEffect(PotionEffectType.LEVITATION);
            }
        }
    }

    public void pause() {
        if (paused) return;
        paused = true;
        pausedAtMillis = System.currentTimeMillis();
        if (bossBar != null && bossBarVisible && bossBarAudience == AudienceMode.PRIVATE) {
            Player p = Bukkit.getPlayer(targetId);
            if (p != null) p.hideBossBar(bossBar);
        }
    }

    public void resume() {
        paused = false;
        pausedAtMillis = -1L;
    }

    public void showBossbarToJoinerIfBroadcast(Player joiner) {
        if (bossBar != null && bossBarVisible && bossBarAudience == AudienceMode.BROADCAST) {
            joiner.showBossBar(bossBar);
        }
    }

    public void ensureBossbarShownToRejoined(Player rejoined) {
        if (bossBar != null && bossBarVisible && bossBarAudience == AudienceMode.PRIVATE) {
            rejoined.showBossBar(bossBar);
        }
    }

    public boolean isPaused() { return paused; }
    public long getPausedAtMillis() { return pausedAtMillis; }

    public UUID targetId() { return targetId; }
    public String showId() { return showId; }
    public ShowMode mode() { return mode; }
    public AudienceMode audienceMode() { return audienceMode; }
    public Location origin() { return origin; }
    public int currentTick() { return currentTick; }

    public void trackEffect(PotionEffectType type) { if (type != null) effectsApplied.add(type); }

    // Marker
    private static final class SceneCall {
        final String sceneId;
        SceneCall(String sceneId) { this.sceneId = sceneId; }
    }
}
