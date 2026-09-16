package com.glukelonzales.entity.custom;

import com.glukelonzales.registry.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.List;

/**
 * Natural spawning for the Mariachi: every {@link #CHECK_INTERVAL_TICKS} ticks, each player
 * standing in a desert-family biome gets a {@link #SPAWN_CHANCE} roll; a hit tries to place one
 * nearby. "Desert-family" is desert plus the badlands variants (the "hardened clay"/terracotta
 * ones), per spec.
 */
public class MariachiSpawner {
    private static final int CHECK_INTERVAL_TICKS = 75;
    private static final float SPAWN_CHANCE = 0.001F;
    private static final int MIN_RADIUS = 8;
    private static final int MAX_RADIUS = 24;

    private static final List<RegistryKey<Biome>> DESERT_LIKE_BIOMES = List.of(
            BiomeKeys.DESERT, BiomeKeys.BADLANDS, BiomeKeys.ERODED_BADLANDS, BiomeKeys.WOODED_BADLANDS);

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(MariachiSpawner::tick);
    }

    private static void tick(ServerWorld world) {
        if (world.getTime() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!isDesertLike(world, player.getBlockPos())) {
                continue;
            }
            if (world.random.nextFloat() < SPAWN_CHANCE) {
                trySpawnNear(world, player);
            }
        }
    }

    private static void trySpawnNear(ServerWorld world, ServerPlayerEntity player) {
        int radius = MIN_RADIUS + world.random.nextInt(MAX_RADIUS - MIN_RADIUS + 1);
        double angle = world.random.nextDouble() * Math.PI * 2.0;
        int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * radius);
        int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * radius);
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
        BlockPos pos = new BlockPos(x, y, z);

        if (!isDesertLike(world, pos)) {
            return;
        }
        if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir()) {
            return;
        }

        MariachiEntity mariachi = ModEntities.MARIACHI.create(world);
        if (mariachi == null) {
            return;
        }
        mariachi.refreshPositionAndAngles(x + 0.5, y, z + 0.5, world.random.nextFloat() * 360.0F, 0.0F);
        mariachi.initialize(world, world.getLocalDifficulty(pos), SpawnReason.NATURAL, null);
        world.spawnEntity(mariachi);
    }

    private static boolean isDesertLike(ServerWorld world, BlockPos pos) {
        var biome = world.getBiomeAccess().getBiome(pos);
        for (RegistryKey<Biome> key : DESERT_LIKE_BIOMES) {
            if (biome.matchesKey(key)) {
                return true;
            }
        }
        return false;
    }
}
