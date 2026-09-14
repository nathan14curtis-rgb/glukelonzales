package com.glukelonzales.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/** Example status effect. Override applyUpdateEffect for per-tick behaviour. */
public class TacoPowerEffect extends StatusEffect {
	public TacoPowerEffect(StatusEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
		// Runs on the ticks selected by canApplyUpdateEffect.
		return true;
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}
}
