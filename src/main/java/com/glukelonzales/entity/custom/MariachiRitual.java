package com.glukelonzales.entity.custom;

import com.glukelonzales.registry.ModEntities;
import com.glukelonzales.registry.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The Taco Boss's summoning ritual: gather 3 mariachis within {@link #CLUSTER_RADIUS} blocks of
 * each other, each holding a different instrument (vihuela, trumpet, violin — see {@link
 * MariachiEntity#interactMob}), and the ritual song starts. Once it's played all the way
 * through, the boss spawns roughly 50 blocks from whichever player is closest to the group.
 *
 * <p>The cluster check only matters for *triggering* the ritual — once it's started, mariachis
 * wandering apart (normal AnimalEntity AI over a 2.5-minute song) don't cancel it, only actually
 * losing the required instrument or dying does. Earlier requiring them to stay clustered the
 * whole time meant a ritual could silently fizzle from ordinary wandering, with the song still
 * audibly finishing and no boss ever showing up.
 *
 * <p>The song itself plays from a single invisible, invulnerable marker armor stand (see {@link
 * #isRitualMarker}) rather than a direct {@code world.playSound} broadcast — that was triggering
 * multiple overlapping full-length plays whenever the ritual got re-detected (e.g. from the
 * clustering jitter above), since there's no way to cancel an already-started broadcast. Tying
 * it to one entity's lifetime guarantees exactly one playback per ritual, and gets normal
 * distance-based attenuation for free since it's a positioned sound (see
 * {@code GlukelonzalesClient#updateRitualSound}).
 *
 * <p>Keeps things simple by tracking at most one active ritual at a time — if you start a second
 * trio elsewhere while one's already playing, it won't be picked up until the first resolves.
 *
 * <p><b>Bug history:</b> {@link ServerTickEvents#END_WORLD_TICK} fires once per *loaded
 * dimension* per server tick (overworld, nether, end are all usually loaded at once), but
 * {@link #active} was shared static state with no dimension check. The nether/end's tick call
 * would look up the mariachis' UUIDs in the wrong world, find nothing, and immediately cancel
 * the ritual — and on the very next overworld tick, re-detect the same still-valid trio and
 * restart from zero. That produced a brand new ~2.5-minute song roughly every second,
 * compounding forever (explaining the overlapping/never-ending audio) while the actual progress
 * counter never got anywhere near {@link #SONG_DURATION_TICKS} (explaining the boss never
 * spawning). Fixed by pinning an active ritual to the specific {@link ServerWorld} it started
 * in and ignoring tick calls from any other one entirely.
 */
public class MariachiRitual {
    private static final int CHECK_INTERVAL_TICKS = 20; // 1 second
    private static final double CLUSTER_RADIUS = 8.0;
    private static final int SONG_DURATION_TICKS = 3062; // ritual_song.ogg is ~153.1s
    private static final double SUMMON_DISTANCE = 50.0;
    private static final Set<Item> INSTRUMENTS = Set.of(ModItems.VIHUELA, ModItems.TRUMPET, ModItems.VIOLIN);

    /** Distinguishes the sound-carrier armor stand from any the player might place themselves. */
    private static final String MARKER_NAME = "glukelonzales_ritual_marker";

    private static Ritual active;

    private record Ritual(ServerWorld world, Map<UUID, Item> requiredInstruments, UUID markerId, int elapsedTicks) {
        Ritual tick() {
            return new Ritual(world, requiredInstruments, markerId, elapsedTicks + CHECK_INTERVAL_TICKS);
        }
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(MariachiRitual::tick);
    }

    public static boolean isRitualMarker(ArmorStandEntity stand) {
        return stand.hasCustomName() && MARKER_NAME.equals(stand.getCustomName().getString());
    }

    private static void tick(ServerWorld world) {
        if (world.getTime() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        if (active != null) {
            // Ignore tick calls from any other loaded dimension entirely — don't touch `active`
            // at all, since the mariachis/marker simply don't exist there and a lookup would
            // wrongly read as "gone".
            if (active.world() == world) {
                progressActive(world);
            }
            return;
        }

        findAndStartRitual(world);
    }

    private static void progressActive(ServerWorld world) {
        for (var entry : active.requiredInstruments().entrySet()) {
            if (!(world.getEntity(entry.getKey()) instanceof MariachiEntity mariachi) || !mariachi.isAlive()
                    || !mariachi.getMainHandStack().isOf(entry.getValue())) {
                cancelActive(world); // someone died or lost their instrument — ritual fizzles
                return;
            }
        }

        active = active.tick();
        if (active.elapsedTicks() >= SONG_DURATION_TICKS) {
            if (world.getEntity(active.markerId()) instanceof ArmorStandEntity marker) {
                summonBoss(world, marker.getX(), marker.getY(), marker.getZ());
                marker.discard();
            }
            active = null;
        }
    }

    private static void cancelActive(ServerWorld world) {
        if (world.getEntity(active.markerId()) instanceof ArmorStandEntity marker) {
            marker.discard();
        }
        active = null;
    }

    private static void findAndStartRitual(ServerWorld world) {
        List<MariachiEntity> performers = new ArrayList<>();
        for (var entity : world.iterateEntities()) {
            if (entity instanceof MariachiEntity mariachi
                    && INSTRUMENTS.contains(mariachi.getMainHandStack().getItem())) {
                performers.add(mariachi);
            } else if (entity instanceof ArmorStandEntity stand && isRitualMarker(stand)) {
                // Self-heal: with no active ritual there should be no marker at all. Clears out
                // anything orphaned by the dimension bug above on worlds upgrading from it.
                stand.discard();
            }
        }
        if (performers.size() < 3) {
            return;
        }

        for (int i = 0; i < performers.size(); i++) {
            List<MariachiEntity> cluster = new ArrayList<>();
            cluster.add(performers.get(i));
            for (int j = 0; j < performers.size(); j++) {
                if (i == j) {
                    continue;
                }
                if (performers.get(i).squaredDistanceTo(performers.get(j)) <= CLUSTER_RADIUS * CLUSTER_RADIUS) {
                    cluster.add(performers.get(j));
                }
            }
            if (hasOneOfEach(cluster)) {
                startRitual(world, cluster);
                return;
            }
        }
    }

    private static void startRitual(ServerWorld world, List<MariachiEntity> cluster) {
        double x = 0, y = 0, z = 0;
        Map<UUID, Item> requiredInstruments = new HashMap<>();
        for (MariachiEntity mariachi : cluster) {
            x += mariachi.getX();
            y += mariachi.getY();
            z += mariachi.getZ();
            requiredInstruments.put(mariachi.getUuid(), mariachi.getMainHandStack().getItem());
        }
        int count = cluster.size();
        x /= count;
        y /= count;
        z /= count;

        ArmorStandEntity marker = new ArmorStandEntity(world, x, y, z);
        marker.setInvisible(true);
        marker.setInvulnerable(true);
        marker.setNoGravity(true);
        marker.setSilent(true);
        marker.setCustomName(Text.literal(MARKER_NAME));
        marker.setCustomNameVisible(false);
        world.spawnEntity(marker);

        active = new Ritual(world, requiredInstruments, marker.getUuid(), 0);
    }

    private static boolean hasOneOfEach(List<MariachiEntity> cluster) {
        boolean vihuela = false, trumpet = false, violin = false;
        for (MariachiEntity mariachi : cluster) {
            Item held = mariachi.getMainHandStack().getItem();
            vihuela |= held == ModItems.VIHUELA;
            trumpet |= held == ModItems.TRUMPET;
            violin |= held == ModItems.VIOLIN;
        }
        return vihuela && trumpet && violin;
    }

    private static void summonBoss(ServerWorld world, double ritualX, double ritualY, double ritualZ) {
        ServerPlayerEntity nearest = null;
        double nearestDistSqr = Double.MAX_VALUE;
        for (ServerPlayerEntity player : world.getPlayers()) {
            double distSqr = player.squaredDistanceTo(ritualX, ritualY, ritualZ);
            if (distSqr < nearestDistSqr) {
                nearestDistSqr = distSqr;
                nearest = player;
            }
        }
        double originX = nearest != null ? nearest.getX() : ritualX;
        double originZ = nearest != null ? nearest.getZ() : ritualZ;

        BlockPos spawnPos = findSafeSpawn(world, originX, originZ);

        TacoBossEntity boss = ModEntities.TACO_BOSS.create(world);
        if (boss == null) {
            return;
        }
        boss.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                world.random.nextFloat() * 360.0F, 0.0F);
        world.spawnEntity(boss);
    }

    /** Picks a point ~{@link #SUMMON_DISTANCE} blocks from (originX, originZ) and stands it on
     *  the surface there, so the boss doesn't spawn stuck in the ground (or falling from the
     *  sky) — tries a handful of directions in case the first has no headroom. */
    private static BlockPos findSafeSpawn(ServerWorld world, double originX, double originZ) {
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = world.random.nextDouble() * Math.PI * 2.0;
            int x = (int) Math.round(originX + Math.cos(angle) * SUMMON_DISTANCE);
            int z = (int) Math.round(originZ + Math.sin(angle) * SUMMON_DISTANCE);
            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);

            BlockPos pos = new BlockPos(x, y, z);
            boolean clear = true;
            for (int dy = 0; dy < 4; dy++) {
                if (!world.getBlockState(pos.up(dy)).isAir()) {
                    clear = false;
                    break;
                }
            }
            if (clear) {
                return pos;
            }
        }
        // Fall back to whatever the last attempt found rather than not spawning the boss at all.
        int x = (int) Math.round(originX + SUMMON_DISTANCE);
        int z = (int) Math.round(originZ);
        return new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z), z);
    }
}
