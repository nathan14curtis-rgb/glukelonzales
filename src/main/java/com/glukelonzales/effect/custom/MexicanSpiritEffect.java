package com.glukelonzales.effect.custom;

import com.glukelonzales.registry.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.sound.SoundCategory;

/**
 * "The Mexican Spirit" — granted by eating a taco. Heals 1 HP every {@link #TICKS_PER_HEAL}
 * ticks (half vanilla Regeneration's base 50-tick interval, i.e. twice the rate) for however
 * long the effect lasts (30 seconds when applied from the taco's FoodComponent, alongside
 * Strength V). Also kicks off the jingle (9.7s) the moment it's applied.
 */
public class MexicanSpiritEffect extends StatusEffect {
    private static final int TICKS_PER_HEAL = 25;
    private static final float JINGLE_VOLUME = 0.2F; // cut twice now (1.0 -> 0.5 -> 0.2)

    public MexicanSpiritEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.isAlive() && entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0F);
        }
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % TICKS_PER_HEAL == 0;
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                ModSounds.MEXICAN_SPIRIT_JINGLE, SoundCategory.PLAYERS, JINGLE_VOLUME, 1.0F);
    }
}
