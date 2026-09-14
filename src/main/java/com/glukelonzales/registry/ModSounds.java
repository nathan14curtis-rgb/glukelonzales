package com.glukelonzales.registry;

import com.glukelonzales.Glukelonzales;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Sound events. Each entry also needs a matching key in
 * assets/glukelonzales/sounds.json pointing at an .ogg file.
 */
public class ModSounds {
	// public static final SoundEvent MARIACHI_MUSIC = register("mariachi_music");

	public static SoundEvent register(String name) {
		Identifier id = Glukelonzales.id(name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	public static void register() {
		Glukelonzales.LOGGER.debug("Registering sounds for {}", Glukelonzales.MOD_ID);
	}
}
