package com.glukelonzales.entity.custom;

import com.glukelonzales.registry.ModEntities;
import com.glukelonzales.registry.ModItems;
import com.glukelonzales.registry.ModSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The Taco Boss's summoning ritual: gather 3 mariachis within {@link #CLUSTER_RADIUS} blocks of
 * each other, each holding a different instrument (vihuela, trumpet, violin — see {@link
 * MariachiEntity#interactMob}), and the ritual song starts. Once it's played all the way
 * through, the boss spawns roughly 50 blocks from whichever player is closest to the group.
 *
 * <p>Keeps things simple by tracking at most one active ritual at a time — if you start a second
 * trio elsewhere while one's already playing, it won't be picked up until the first resolves.
 */
public class MariachiRitual {
    private static final int CHECK_INTERVAL_TICKS = 20; // 1 second
    private static final double CLUSTER_RADIUS = 8.0;
    private static final int SONG_DURATION_TICKS = 3062; // ritual_song.ogg is ~153.1s
    private static final double SUMMON_DISTANCE = 50.0;
    private static final Set<Item> INSTRUMENTS = Set.of(ModItems.VIHUELA, ModItems.TRUMPET, ModItems.VIOLIN);

    private static Ritual active;

    private record Ritual(Set<UUID> participants, double x, double y, double z, int elapsedTicks) {
        Ritual tick() {
            return new Ritual(participants, x, y, z, elapsedTicks + CHECK_INTERVAL_TICKS);
        }
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(MariachiRitual::tick);
    }

    private static void tick(ServerWorld world) {
        if (world.getTime() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        if (active != null) {
            progressActive(world);
            return;
        }

        findAndStartRitual(world);
    }

    private static void progressActive(ServerWorld world) {
        List<MariachiEntity> trio = new ArrayList<>();
        for (UUID id : active.participants()) {
            if (world.getEntity(id) instanceof MariachiEntity mariachi && mariachi.isAlive()
                    && INSTRUMENTS.contains(mariachi.getMainHandStack().getItem())) {
                trio.add(mariachi);
            }
        }
        if (trio.size() < 3 || !stillClustered(trio)) {
            active = null; // someone wandered off, died, or dropped their instrument — ritual fizzles
            return;
        }

        active = active.tick();
        if (active.elapsedTicks() >= SONG_DURATION_TICKS) {
            summonBoss(world, active.x(), active.y(), active.z());
            active = null;
        }
    }

    private static void findAndStartRitual(ServerWorld world) {
        List<MariachiEntity> performers = new ArrayList<>();
        for (var entity : world.iterateEntities()) {
            if (entity instanceof MariachiEntity mariachi
                    && INSTRUMENTS.contains(mariachi.getMainHandStack().getItem())) {
                performers.add(mariachi);
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
        Set<UUID> participants = new HashSet<>();
        for (MariachiEntity mariachi : cluster) {
            x += mariachi.getX();
            y += mariachi.getY();
            z += mariachi.getZ();
            participants.add(mariachi.getUuid());
        }
        int count = cluster.size();
        x /= count;
        y /= count;
        z /= count;

        active = new Ritual(participants, x, y, z, 0);
        world.playSound(null, x, y, z, ModSounds.RITUAL_SONG, SoundCategory.RECORDS, 4.0F, 1.0F);
    }

    private static boolean stillClustered(List<MariachiEntity> trio) {
        for (MariachiEntity a : trio) {
            for (MariachiEntity b : trio) {
                if (a != b && a.squaredDistanceTo(b) > CLUSTER_RADIUS * CLUSTER_RADIUS) {
                    return false;
                }
            }
        }
        return true;
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
