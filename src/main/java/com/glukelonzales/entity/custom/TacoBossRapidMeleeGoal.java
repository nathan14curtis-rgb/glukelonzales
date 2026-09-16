package com.glukelonzales.entity.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.Hand;

import java.util.EnumSet;

/**
 * ATTACKING phase: closes any remaining gap and lands a hit every {@link #ATTACK_INTERVAL_TICKS}
 * ticks for {@link #DAMAGE_PER_HIT} (half a heart) — matching the "half a heart, 2-3 hits a
 * second" spec. {@link TacoBossEntity#tick()} is what bumps the phase back to CHASING if the
 * target manages to put distance between itself and the boss.
 */
public class TacoBossRapidMeleeGoal extends Goal {
    private static final int ATTACK_INTERVAL_TICKS = 8; // ~2.5 attacks/sec
    private static final float DAMAGE_PER_HIT = 1.0F; // half a heart

    private final TacoBossEntity boss;
    private int attackCooldown;

    public TacoBossRapidMeleeGoal(TacoBossEntity boss) {
        this.boss = boss;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return boss.getPhase() == TacoBossEntity.Phase.ATTACKING && boss.getTarget() != null;
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void start() {
        attackCooldown = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = boss.getTarget();
        if (target == null) {
            return;
        }

        boss.getLookControl().lookAt(target, 30.0F, 30.0F);
        boss.getNavigation().startMovingTo(target, 1.2D);

        if (boss.squaredDistanceTo(target) > TacoBossEntity.MELEE_REACH * TacoBossEntity.MELEE_REACH) {
            return;
        }

        if (--attackCooldown <= 0) {
            attackCooldown = ATTACK_INTERVAL_TICKS;
            boss.swingHand(Hand.MAIN_HAND);
            target.damage(boss.getWorld().getDamageSources().mobAttack(boss), DAMAGE_PER_HIT);
        }
    }
}
