package com.glukelonzales.client;

import com.glukelonzales.entity.custom.TacoBossEntity;
import com.glukelonzales.registry.ModSounds;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;

/**
 * The chase music. It's just a looping sound anchored to the boss's live position each tick —
 * Minecraft's normal distance-based attenuation is what makes it "get louder as it gets closer"
 * once the player is in CHASING range, no extra volume math needed here. Stops itself (via
 * {@link #done}) the moment the boss leaves CHASING, dies, or unloads, which is also the cue
 * for it to hand off to the taunt clips in {@link com.glukelonzales.entity.custom.TacoBossEntity#tick()}.
 */
public class TacoBossMariachiSound extends MovingSoundInstance {
    private final TacoBossEntity boss;

    public TacoBossMariachiSound(TacoBossEntity boss) {
        super(ModSounds.TACO_BOSS_MARIACHI, SoundCategory.HOSTILE, boss.getRandom());
        this.boss = boss;
        this.repeat = true;
        this.repeatDelay = 0;
        this.x = boss.getX();
        this.y = boss.getY();
        this.z = boss.getZ();
    }

    @Override
    public void tick() {
        if (!boss.isAlive() || boss.isRemoved() || boss.getPhase() != TacoBossEntity.Phase.CHASING) {
            this.done = true;
            return;
        }
        this.x = boss.getX();
        this.y = boss.getY();
        this.z = boss.getZ();
    }
}
