package com.glukelonzales.entity.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

/**
 * Active only while stalking: keeps a lurking distance (roughly 9-18 blocks) from the nearest
 * player instead of closing in or fleeing, repositioning every {@link #REPATH_INTERVAL} ticks.
 * This is what makes the boss feel like it's "watching" rather than hunting, right up until
 * {@link TacoBossStareTrackerGoal} decides it's been watched too long.
 */
public class TacoBossStalkGoal extends Goal {
    private static final double STALK_RADIUS = 24.0D;
    private static final double PREFERRED_MIN = 9.0D;
    private static final double PREFERRED_MAX = 18.0D;
    private static final int REPATH_INTERVAL = 20;

    private final TacoBossEntity boss;
    private PlayerEntity nearestPlayer;
    private int repathCooldown;

    public TacoBossStalkGoal(TacoBossEntity boss) {
        this.boss = boss;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (boss.getPhase() != TacoBossEntity.Phase.STALKING) {
            return false;
        }
        nearestPlayer = boss.getWorld().getClosestPlayer(boss, STALK_RADIUS);
        return nearestPlayer != null;
    }

    @Override
    public boolean shouldContinue() {
        return boss.getPhase() == TacoBossEntity.Phase.STALKING
                && nearestPlayer != null
                && nearestPlayer.isAlive()
                && boss.squaredDistanceTo(nearestPlayer) <= Math.pow(STALK_RADIUS * 1.5, 2);
    }

    @Override
    public void tick() {
        boss.getLookControl().lookAt(nearestPlayer, 30.0F, 30.0F);

        if (--repathCooldown > 0) {
            return;
        }
        repathCooldown = REPATH_INTERVAL;

        double distSqr = boss.squaredDistanceTo(nearestPlayer);
        if (distSqr > PREFERRED_MAX * PREFERRED_MAX) {
            boss.getNavigation().startMovingTo(nearestPlayer, 1.0D);
        } else if (distSqr < PREFERRED_MIN * PREFERRED_MIN) {
            Vec3d away = boss.getPos().subtract(nearestPlayer.getPos()).normalize();
            Vec3d dest = boss.getPos().add(away.multiply(6.0D));
            boss.getNavigation().startMovingTo(dest.x, dest.y, dest.z, 1.0D);
        } else {
            boss.getNavigation().startMovingTo(
                    nearestPlayer.getX() + (boss.getRandom().nextDouble() - 0.5) * 4.0,
                    nearestPlayer.getY(),
                    nearestPlayer.getZ() + (boss.getRandom().nextDouble() - 0.5) * 4.0,
                    0.8D);
        }
    }

    @Override
    public void stop() {
        nearestPlayer = null;
        boss.getNavigation().stop();
    }
}
