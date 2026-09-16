package com.glukelonzales.client;

import com.glukelonzales.entity.custom.TacoBossEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

/**
 * The chase music. It's just a looping sound anchored to the boss's live position each tick —
 * Minecraft's normal distance-based attenuation is what makes it "get louder as it gets closer"
 * as the player is stalked/chased, no extra volume math needed here. Stops itself (via
 * {@link #setDone()}) once the boss stops being engaged (back to STALKING) or dies/unloads.
 *
 * Two variants exist ({@code taco_boss_mariachi} above half health, {@code
 * taco_boss_mariachi_warning} at or below half health) — see {@link GlukelonzalesClient}, which
 * picks the sound event/volume and swaps this instance out when the boss's health crosses that
 * threshold mid-loop.
 */
public class TacoBossMariachiSound extends MovingSoundInstance {
    private final TacoBossEntity boss;
    private final SoundEvent trackId;

    public TacoBossMariachiSound(TacoBossEntity boss, SoundEvent track, float volume) {
        super(track, SoundCategory.HOSTILE, boss.getRandom());
        this.boss = boss;
        this.trackId = track;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = volume;
        this.x = boss.getX();
        this.y = boss.getY();
        this.z = boss.getZ();
    }

    /** Which registered track this instance is playing, so the client tick loop can tell when
     *  the boss has crossed the half-health line and needs a different one. */
    public SoundEvent getTrackId() {
        return trackId;
    }

    @Override
    public void tick() {
        if (!boss.isAlive() || boss.isRemoved() || boss.getPhase() == TacoBossEntity.Phase.STALKING) {
            this.setDone();
            return;
        }
        this.x = boss.getX();
        this.y = boss.getY();
        this.z = boss.getZ();
    }
}
