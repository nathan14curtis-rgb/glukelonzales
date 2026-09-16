package com.glukelonzales.entity.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.EnumSet;

/**
 * The special attack: lobs a {@link TacoProjectileEntity} at the target on a cooldown while
 * chasing or attacking (not while merely stalking). Runs alongside melee once in range, so the
 * boss keeps throwing tacos even mid-beatdown.
 */
public class TacoBossRangedAttackGoal extends Goal {
    private static final int COOLDOWN_TICKS = 10; // ~7x the original 70-tick cooldown
    private static final double MIN_RANGE = 4.0D;
    private static final double MAX_RANGE = 40.0D;

    private final TacoBossEntity boss;
    private int cooldown;

    public TacoBossRangedAttackGoal(TacoBossEntity boss) {
        this.boss = boss;
        this.setControls(EnumSet.noneOf(Goal.Control.class));
    }

    @Override
    public boolean canStart() {
        TacoBossEntity.Phase phase = boss.getPhase();
        LivingEntity target = boss.getTarget();
        if (phase == TacoBossEntity.Phase.STALKING || target == null || !target.isAlive()) {
            return false;
        }
        double distSqr = boss.squaredDistanceTo(target);
        return distSqr >= MIN_RANGE * MIN_RANGE && distSqr <= MAX_RANGE * MAX_RANGE && boss.canSee(target);
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void tick() {
        if (--cooldown > 0) {
            return;
        }
        LivingEntity target = boss.getTarget();
        if (target == null) {
            return;
        }
        cooldown = COOLDOWN_TICKS;

        TacoProjectileEntity taco = new TacoProjectileEntity(boss.getWorld(), boss);
        taco.setPosition(boss.getX(), boss.getEyeY() - 0.3, boss.getZ());

        double dx = target.getX() - taco.getX();
        double dy = (target.getEyeY() - 0.25) - taco.getY();
        double dz = target.getZ() - taco.getZ();
        taco.setVelocity(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.1, dz, 1.6F, 2.0F);

        ((ServerWorld) boss.getWorld()).spawnEntity(taco);
        boss.getWorld().playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.HOSTILE, 1.0F, 0.7F);
    }
}
