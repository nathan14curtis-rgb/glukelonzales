package com.glukelonzales.entity.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * CHASING phase: sprints straight at the target (see {@link TacoBossEntity#setPhase} for the
 * speed boost applied on entering this phase). Once within {@link TacoBossEntity#MELEE_REACH}
 * it flips the boss to ATTACKING, handing off to {@link TacoBossRapidMeleeGoal}.
 */
public class TacoBossChargeGoal extends Goal {
    private final TacoBossEntity boss;
    private int repathCooldown;

    public TacoBossChargeGoal(TacoBossEntity boss) {
        this.boss = boss;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return boss.getPhase() == TacoBossEntity.Phase.CHASING && boss.getTarget() != null;
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void tick() {
        LivingEntity target = boss.getTarget();
        if (target == null) {
            return;
        }
        boss.getLookControl().lookAt(target, 30.0F, 30.0F);

        if (boss.squaredDistanceTo(target) <= TacoBossEntity.MELEE_REACH * TacoBossEntity.MELEE_REACH) {
            boss.setPhase(TacoBossEntity.Phase.ATTACKING);
            return;
        }

        if (--repathCooldown <= 0) {
            repathCooldown = 5;
            boss.getNavigation().startMovingTo(target, 1.35D);
        }
    }

    @Override
    public void stop() {
        repathCooldown = 0;
    }
}
