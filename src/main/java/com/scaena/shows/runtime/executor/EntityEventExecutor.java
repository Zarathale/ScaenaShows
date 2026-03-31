package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.event.*;
import com.scaena.shows.model.event.EntityBehaviorEvents.*;
import com.scaena.shows.model.event.EntityMgmtEvents.*;
import com.scaena.shows.model.event.TextEvents.*;
import com.scaena.shows.runtime.RunningShow;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Handles all entity management and entity behavior events:
 * SPAWN_ENTITY, DESPAWN_ENTITY, CAPTURE_ENTITIES, RELEASE_ENTITIES,
 * ENTITY_AI, ENTITY_SPEED, ENTITY_EFFECT, ENTITY_EQUIP,
 * ENTITY_INVISIBLE, ENTITY_VELOCITY, BOSS_HEALTH_BAR
 */
public final class EntityEventExecutor implements EventExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final Logger log;

    public EntityEventExecutor(Logger log) {
        this.log = log;
    }

    @Override
    public void execute(ShowEvent event, RunningShow show) {
        switch (event.type()) {
            case SPAWN_ENTITY      -> handleSpawn((SpawnEntityEvent) event, show);
            case DESPAWN_ENTITY    -> handleDespawn((DespawnEntityEvent) event, show);
            case CAPTURE_ENTITIES  -> handleCapture((CaptureEntitiesEvent) event, show);
            case RELEASE_ENTITIES  -> handleRelease((ReleaseEntitiesEvent) event, show);
            case ENTITY_AI         -> handleEntityAi((EntityAiEvent) event, show);
            case ENTITY_SPEED      -> handleEntitySpeed((EntitySpeedEvent) event, show);
            case ENTITY_EFFECT     -> handleEntityEffect((EntityEffectEvent) event, show);
            case ENTITY_EQUIP      -> handleEntityEquip((EntityEquipEvent) event, show);
            case ENTITY_INVISIBLE  -> handleEntityInvisible((EntityInvisibleEvent) event, show);
            case ENTITY_VELOCITY   -> handleEntityVelocity((EntityVelocityEvent) event, show);
            case BOSS_HEALTH_BAR   -> handleBossHealthBar((BossHealthBarEvent) event, show);
            default -> {}
        }
    }

    // ------------------------------------------------------------------
    // SPAWN_ENTITY
    // ------------------------------------------------------------------
    private void handleSpawn(SpawnEntityEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;

        EntityType type;
        try { type = EntityType.valueOf(e.entityType.toUpperCase()); }
        catch (IllegalArgumentException ex) {
            log.warning("[ScaenaShows] Unknown entity type: " + e.entityType); return;
        }

        Location spawnLoc = anchor.clone().add(e.offsetX, e.offsetY, e.offsetZ);
        Entity entity = spawnLoc.getWorld().spawnEntity(spawnLoc, type);

        // Apply name
        if (!e.name.isEmpty()) {
            entity.setCustomName(e.name);
            entity.setCustomNameVisible(true);
        }

        // Baby variant
        if (e.baby && entity instanceof Ageable ageable) {
            ageable.setBaby();
        }

        // Variant / Profession
        // Villager, Cat, Wolf use Registry API (Paper 1.20.5+ — enum valueOf deprecated for removal)
        // Horse, Sheep retain enum valueOf (not deprecated)
        if (entity instanceof Villager villager) {
            if (e.profession != null) {
                Villager.Profession prof = Registry.VILLAGER_PROFESSION.get(
                    NamespacedKey.minecraft(e.profession.toLowerCase()));
                if (prof != null) { villager.setProfession(prof); }
                else { log.warning("[ScaenaShows] Unknown villager profession: " + e.profession); }
            }
            if (e.variant != null) {
                Villager.Type vtype = Registry.VILLAGER_TYPE.get(
                    NamespacedKey.minecraft(e.variant.toLowerCase()));
                if (vtype != null) { villager.setVillagerType(vtype); }
                else { log.warning("[ScaenaShows] Unknown villager type: " + e.variant); }
            }
        } else if (entity instanceof Cat cat && e.variant != null) {
            Cat.Type ctype = Registry.CAT_VARIANT.get(
                NamespacedKey.minecraft(e.variant.toLowerCase()));
            if (ctype != null) { cat.setCatType(ctype); }
            else { log.warning("[ScaenaShows] Unknown cat type: " + e.variant); }
        } else if (entity instanceof Horse horse && e.variant != null) {
            try { horse.setColor(Horse.Color.valueOf(e.variant.toUpperCase())); }
            catch (IllegalArgumentException ex) { log.warning("[ScaenaShows] Unknown horse color: " + e.variant); }
        } else if (entity instanceof Sheep sheep && e.variant != null) {
            try { sheep.setColor(DyeColor.valueOf(e.variant.toUpperCase())); }
            catch (IllegalArgumentException ex) { log.warning("[ScaenaShows] Unknown sheep color: " + e.variant); }
        } else if (entity instanceof Wolf wolf && e.variant != null) {
            Wolf.Variant wv = Registry.WOLF_VARIANT.get(
                NamespacedKey.minecraft(e.variant.toLowerCase()));
            if (wv != null) { wolf.setVariant(wv); }
            else { log.warning("[ScaenaShows] Unknown wolf variant: " + e.variant); }
        }

        // Equipment
        if (entity instanceof LivingEntity living) {
            EntityEquipment eq = living.getEquipment();
            if (eq != null) {
                if (itemOf(e.helmetItem)     != null) eq.setHelmet(itemOf(e.helmetItem));
                if (itemOf(e.chestplateItem) != null) eq.setChestplate(itemOf(e.chestplateItem));
                if (itemOf(e.leggingsItem)   != null) eq.setLeggings(itemOf(e.leggingsItem));
                if (itemOf(e.bootsItem)      != null) eq.setBoots(itemOf(e.bootsItem));
                if (itemOf(e.mainHandItem)   != null) eq.setItemInMainHand(itemOf(e.mainHandItem));
                if (itemOf(e.offHandItem)    != null) eq.setItemInOffHand(itemOf(e.offHandItem));
            }

            // OPS-026 — attribute overrides (only applied when explicitly set, i.e. > 0)
            if (e.maxHealth > 0) {
                var attr = living.getAttribute(Attribute.MAX_HEALTH);
                if (attr != null) {
                    attr.setBaseValue(e.maxHealth);
                    living.setHealth(e.maxHealth); // sync current HP to new max
                }
            }
            if (e.speed > 0) {
                var attr = living.getAttribute(Attribute.MOVEMENT_SPEED);
                if (attr != null) attr.setBaseValue(e.speed);
            }
            if (e.scale > 0) {
                var attr = living.getAttribute(Attribute.SCALE);
                if (attr != null) attr.setBaseValue(e.scale);
            }
        }

        if (!e.name.isEmpty()) show.registerSpawnedEntity(e.name, entity);
    }

    // ------------------------------------------------------------------
    // DESPAWN_ENTITY
    // ------------------------------------------------------------------
    private void handleDespawn(DespawnEntityEvent e, RunningShow show) {
        Entity entity = resolveEntity(e.target, show);
        if (entity == null) { log.fine("[ScaenaShows] DESPAWN: entity not found: " + e.target); return; }

        if (e.particleBurst) {
            entity.getWorld().spawnParticle(Particle.EXPLOSION, entity.getLocation(), 5);
        }
        entity.remove();
    }

    // ------------------------------------------------------------------
    // CAPTURE_ENTITIES
    // ------------------------------------------------------------------
    private void handleCapture(CaptureEntitiesEvent e, RunningShow show) {
        Location anchor = show.getAnchorLocation();
        if (anchor == null) return;

        EntityType type;
        try { type = EntityType.valueOf(e.entityType.toUpperCase()); }
        catch (IllegalArgumentException ex) {
            log.warning("[ScaenaShows] CAPTURE_ENTITIES: unknown entity type: " + e.entityType); return;
        }

        List<Entity> nearby = new ArrayList<>();
        for (Entity ent : anchor.getWorld().getNearbyEntities(anchor, e.radius, e.radius, e.radius)) {
            if (ent.getType() == type) nearby.add(ent);
        }

        // Limit to maxCount
        List<UUID> captured = new ArrayList<>();
        for (int i = 0; i < Math.min(nearby.size(), e.maxCount); i++) {
            captured.add(nearby.get(i).getUniqueId());
        }

        show.setEntityGroup(e.groupName, captured);
        log.fine("[ScaenaShows] CAPTURE_ENTITIES: captured " + captured.size()
            + " " + e.entityType + " into group '" + e.groupName + "'");
    }

    // ------------------------------------------------------------------
    // RELEASE_ENTITIES — removes a captured entity group from show control.
    // Optionally re-enables AI on all group members. Silent no-op if group
    // is unknown or empty.
    // ------------------------------------------------------------------
    private void handleRelease(ReleaseEntitiesEvent e, RunningShow show) {
        if (!e.target.startsWith("entity_group:")) {
            log.warning("[ScaenaShows] RELEASE_ENTITIES: target must be 'entity_group:<name>', got: " + e.target);
            return;
        }
        String groupName = e.target.substring("entity_group:".length());
        List<UUID> group = show.getEntityGroup(groupName);

        if (group.isEmpty()) return; // silent no-op

        if (e.restoreAi) {
            for (UUID uid : group) {
                Entity entity = Bukkit.getEntity(uid);
                if (entity instanceof Mob mob) {
                    mob.setAI(true);
                }
            }
        }

        // Remove the group from show tracking (entities remain in world)
        show.releaseEntityGroup(groupName);
        log.fine("[ScaenaShows] RELEASE_ENTITIES: released group '" + groupName
            + "' (" + group.size() + " entities)");
    }

    // ------------------------------------------------------------------
    // ENTITY_AI
    // ------------------------------------------------------------------
    private void handleEntityAi(EntityAiEvent e, RunningShow show) {
        for (Entity entity : resolveEntities(e.target, show)) {
            if (entity instanceof Mob mob) {
                mob.setAI(e.enabled);
            }
        }
    }

    // ------------------------------------------------------------------
    // ENTITY_SPEED
    // ------------------------------------------------------------------
    private void handleEntitySpeed(EntitySpeedEvent e, RunningShow show) {
        for (Entity entity : resolveEntities(e.target, show)) {
            if (entity instanceof LivingEntity living
                    && living.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                living.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(e.speed * 0.2);
            }
        }
    }

    // ------------------------------------------------------------------
    // ENTITY_EFFECT
    // ------------------------------------------------------------------
    private void handleEntityEffect(EntityEffectEvent e, RunningShow show) {
        PotionEffectType type = PotionEffectType.getByName(e.effectId.toUpperCase());
        if (type == null) { log.warning("[ScaenaShows] Unknown potion effect: " + e.effectId); return; }
        for (Entity entity : resolveEntities(e.target, show)) {
            if (entity instanceof LivingEntity living) {
                living.addPotionEffect(new PotionEffect(type, e.durationTicks, e.amplifier));
            }
        }
    }

    // ------------------------------------------------------------------
    // ENTITY_EQUIP
    // ------------------------------------------------------------------
    private void handleEntityEquip(EntityEquipEvent e, RunningShow show) {
        for (Entity entity : resolveEntities(e.target, show)) {
            if (!(entity instanceof LivingEntity living)) continue;
            EntityEquipment eq = living.getEquipment();
            if (eq == null) continue;
            if (itemOf(e.helmet)     != null) eq.setHelmet(itemOf(e.helmet));
            if (itemOf(e.chestplate) != null) eq.setChestplate(itemOf(e.chestplate));
            if (itemOf(e.leggings)   != null) eq.setLeggings(itemOf(e.leggings));
            if (itemOf(e.boots)      != null) eq.setBoots(itemOf(e.boots));
            if (itemOf(e.mainHand)   != null) eq.setItemInMainHand(itemOf(e.mainHand));
            if (itemOf(e.offHand)    != null) eq.setItemInOffHand(itemOf(e.offHand));
        }
    }

    // ------------------------------------------------------------------
    // ENTITY_INVISIBLE
    // ------------------------------------------------------------------
    private void handleEntityInvisible(EntityInvisibleEvent e, RunningShow show) {
        for (Entity entity : resolveEntities(e.target, show)) {
            if (entity instanceof LivingEntity living) {
                living.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY, e.durationTicks, 0, false, false));
            }
        }
    }

    // ------------------------------------------------------------------
    // ENTITY_VELOCITY
    // ------------------------------------------------------------------
    private void handleEntityVelocity(EntityVelocityEvent e, RunningShow show) {
        for (Entity entity : resolveEntities(e.target, show)) {
            entity.setVelocity(new org.bukkit.util.Vector(e.vecX, e.vecY, e.vecZ));
        }
    }

    // ------------------------------------------------------------------
    // BOSS_HEALTH_BAR  (OPS-026)
    // Creates an entity-linked bossbar that tracks live HP via EntityCombatListener.
    // ------------------------------------------------------------------
    private void handleBossHealthBar(BossHealthBarEvent e, RunningShow show) {
        Entity entity = resolveEntity(e.target, show);
        if (!(entity instanceof LivingEntity living)) {
            log.warning("[ScaenaShows] BOSS_HEALTH_BAR: entity not found or not LivingEntity: " + e.target);
            return;
        }

        BossBar.Color color;
        BossBar.Overlay overlay;
        try { color   = BossBar.Color.valueOf(e.color.toUpperCase()); }
        catch (Exception ex) { color = BossBar.Color.RED; }
        try { overlay = BossBar.Overlay.valueOf(e.overlay.toUpperCase()); }
        catch (Exception ex) { overlay = BossBar.Overlay.PROGRESS; }

        BossBar bar = BossBar.bossBar(MM.deserialize(e.title), 1.0f, color, overlay);

        // Show to all current participants
        for (Player p : show.getOnlineParticipants()) {
            p.showBossBar(bar);
        }
        show.addActiveBossBar(bar); // registered for cleanup on show end

        // Record tracker so EntityCombatListener can react to damage/death
        // Use attribute value directly — getMaxHealth() is deprecated in Paper 1.21
        var maxHpAttr = living.getAttribute(Attribute.MAX_HEALTH);
        double maxHp  = maxHpAttr != null ? maxHpAttr.getValue() : living.getHealth();
        RunningShow.BossHealthBarTracker tracker = new RunningShow.BossHealthBarTracker(
            entity.getUniqueId(),
            bar,
            maxHp,
            e.deathLine,
            e.deathLineColor,
            e.deathLinePauseTicks,
            e.victoryCue
        );
        show.registerBossHealthBar(entity.getUniqueId(), tracker);

        log.fine("[ScaenaShows] BOSS_HEALTH_BAR registered for entity '"
            + entity.getUniqueId() + "' in show '" + show.show.id + "' (maxHp=" + maxHp + ")");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Resolve a target string to an Entity.
     * Supported: entity:spawned:Name, entity_group:Name (returns first member)
     */
    private Entity resolveEntity(String target, RunningShow show) {
        if (target == null) return null;
        if (target.startsWith("entity:spawned:")) {
            String name = target.substring("entity:spawned:".length());
            return show.getSpawnedEntity(name);
        }
        if (target.startsWith("entity_group:")) {
            String groupName = target.substring("entity_group:".length());
            List<UUID> group = show.getEntityGroup(groupName);
            if (group.isEmpty()) return null;
            return Bukkit.getEntity(group.get(0));
        }
        if (target.startsWith("entity:world:")) {
            String customName = target.substring("entity:world:".length());
            Location anchor = show.getAnchorLocation();
            if (anchor == null) return null;
            for (Entity ent : anchor.getWorld().getEntities()) {
                if (customName.equals(ent.getCustomName())) return ent;
            }
            return null;
        }
        return null;
    }

    /**
     * Resolve a target string to a list of entities (supports entity_group).
     */
    private List<Entity> resolveEntities(String target, RunningShow show) {
        if (target == null) return List.of();
        if (target.startsWith("entity:spawned:")) {
            Entity e = show.getSpawnedEntity(target.substring("entity:spawned:".length()));
            return e != null ? List.of(e) : List.of();
        }
        if (target.startsWith("entity_group:")) {
            String groupName = target.substring("entity_group:".length());
            List<UUID> uuids = show.getEntityGroup(groupName);
            List<Entity> out = new ArrayList<>();
            for (UUID uid : uuids) {
                Entity e = Bukkit.getEntity(uid);
                if (e != null) out.add(e);
            }
            return out;
        }
        if (target.startsWith("entity:world:")) {
            String customName = target.substring("entity:world:".length());
            Location anchor = show.getAnchorLocation();
            if (anchor == null) return List.of();
            for (Entity ent : anchor.getWorld().getEntities()) {
                if (customName.equals(ent.getCustomName())) return List.of(ent);
            }
            return List.of();
        }
        return List.of();
    }

    private ItemStack itemOf(String material) {
        if (material == null || material.isEmpty()) return null;
        try { return new ItemStack(Material.valueOf(material.toUpperCase())); }
        catch (IllegalArgumentException e) { return null; }
    }
}
