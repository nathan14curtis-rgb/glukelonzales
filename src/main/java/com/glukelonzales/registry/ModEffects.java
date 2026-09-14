package com.glukelonzales.registry;

import com.glukelonzales.Glukelonzales;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Status effects (potion effects). Needs a lang key + a 18x18 icon texture. */
public class ModEffects {
	// public static final RegistryEntry<StatusEffect> TACO_POWER =
	//         register("taco_power", new TacoPowerEffect(StatusEffectCategory.BENEFICIAL, 0xE8B24A));

	public static StatusEffect register(String name, StatusEffect effect) {
		return Registry.register(Registries.STATUS_EFFECT, Glukelonzales.id(name), effect);
	}

	public static void register() {
		Glukelonzales.LOGGER.debug("Registering status effects for {}", Glukelonzales.MOD_ID);
	}
}
