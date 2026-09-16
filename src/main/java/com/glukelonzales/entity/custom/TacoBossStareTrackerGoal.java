package com.glukelonzales.entity.custom;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.List;

/**
 * Always active while stalking. Tracks how long each nearby player has been staring at the
 * boss (within {@link TacoBossEntity#STARE_FOV_DEGREES} and with line of sight) and snaps it
 * into {@link TacoBossEntity.Phase#CHASING} once one crosses {@link TacoBossEntity#STARE_TRIGGER_TICKS}.
 * Looking away decays the count instead of resetting it outright, so a nervous glance doesn't
 * buy a full reset.
 */
public class TacoBossStareTrackerGoal extends Goal {
    private static final double STARE_COS_THRESHOLD = Math.cos(Math.toRadians(TacoBossEntity.STARE_FOV_DEGREES));

    private final TacoBossEntity boss;

    public TacoBossStareTrackerGoal(TacoBossEntity boss) {
        this.boss = boss;
        this.setControls(EnumSet.noneOf(Goal.Control.class));
    }

    @Override
    public boolean canStart() {
        return boss.getPhase() == TacoBossEntity.Phase.STALKING;
    }

    @Override
    public void tick() {
        // No distance cap here — the boss always knows where every player is; only the
        // FOV/line-of-sight check in isStaringAt() below gates whether it's being watched.
        List<? extends PlayerEntity> players = boss.getWorld().getPlayers().stream()
                .filter(p -> p.isAlive() && !p.isSpectator())
                .toList();

        for (PlayerEntity player : players) {
            boolean staring = isStaringAt(player);
            int current = boss.getStareTicks().getOrDefault(player.getUuid(), 0);
            current = staring
                    ? Math.min(current + 1, TacoBossEntity.STARE_TRIGGER_TICKS)
                    : Math.max(current - 2, 0);
            boss.getStareTicks().put(player.getUuid(), current);

            if (current >= TacoBossEntity.STARE_TRIGGER_TICKS) {
                boss.setTarget(player);
                boss.setPhase(TacoBossEntity.Phase.CHASING);
                return;
            }
        }
    }

    private boolean isStaringAt(PlayerEntity player) {
        if (!player.canSee(boss)) {
            return false;
        }
        Vec3d toBoss = boss.getEyePos().subtract(player.getEyePos()).normalize();
        Vec3d look = player.getRotationVec(1.0F);
        return look.dotProduct(toBoss) >= STARE_COS_THRESHOLD;
    }
}
