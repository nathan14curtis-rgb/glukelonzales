package com.glukelonzales.entity.custom;

import com.glukelonzales.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The taco boss. Three phases, synced to the client via {@link #PHASE} so client code
 * (see {@code GlukelonzalesClient}) can react to them for the chase music:
 *
 * <ul>
 *   <li>{@code STALKING}  - lurks near a player, keeping its distance. Every nearby player's
 *       gaze is tracked by {@link TacoBossStareTrackerGoal}; staring continuously for
 *       {@link #STARE_TRIGGER_TICKS} snaps the boss into CHASING with that player as target.</li>
 *   <li>{@code CHASING}   - sprints straight at its target ({@link TacoBossChargeGoal}) while
 *       a looping mariachi track follows it client-side and swells as it closes in (vanilla's
 *       normal distance attenuation does that automatically). Once within melee reach it
 *       flips to ATTACKING.</li>
 *   <li>{@code ATTACKING} - rapid low-damage melee ({@link TacoBossRapidMeleeGoal}: half a
 *       heart, ~2-3 hits/sec), mariachi cuts out and random taunt clips play instead, and
 *       taco projectiles ({@link TacoBossRangedAttackGoal}) keep coming on a cooldown.</li>
 * </ul>
 *
 * If the target is lost or gets far enough away for long enough, the boss gives up and
 * returns to STALKING (see {@link #tick()}).
 */
public class TacoBossEntity extends HostileEntity {

    public enum Phase { STALKING, CHASING, ATTACKING }

    private static final TrackedData<Integer> PHASE =
            DataTracker.registerData(TacoBossEntity.class, TrackedDataHandlerRegistry.INTEGER);

    /** Detection radius, in blocks, for the stare-tracking check while stalking. */
    public static final double DETECTION_RANGE = 40.0D;
    /** Field-of-view half-angle, in degrees, a player must be looking within to count as "staring". */
    public static final double STARE_FOV_DEGREES = 25.0D;
    /** Continuous stare ticks required to snap the boss into a charge (60 ticks = 3 seconds). */
    public static final int STARE_TRIGGER_TICKS = 60;
    /** How close the boss needs to be before it switches from charging to attacking. */
    public static final double MELEE_REACH = 3.0D;

    private static final double STALK_SPEED = 0.32D;
    private static final double CHASE_SPEED = 0.58D;
    private static final double GIVE_UP_RANGE = 64.0D;
    private static final int GIVE_UP_TICKS = 300; // 15 seconds with no valid target/contact
    private static final double ATTACK_GIVE_UP_RANGE = 10.0D;
    private static final int TAUNT_INTERVAL_TICKS = 70;

    private final Map<UUID, Integer> stareTicks = new HashMap<>();
    private int giveUpCounter;

    public TacoBossEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 50;
    }

    public static DefaultAttributeContainer.Builder createTacoBossAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STALK_SPEED)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, DETECTION_RANGE)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(PHASE, Phase.STALKING.ordinal());
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new TacoBossStareTrackerGoal(this));
        this.goalSelector.add(1, new TacoBossRapidMeleeGoal(this));
        this.goalSelector.add(2, new TacoBossChargeGoal(this));
        this.goalSelector.add(3, new TacoBossRangedAttackGoal(this));
        this.goalSelector.add(4, new TacoBossStalkGoal(this));
        this.goalSelector.add(5, new LookAroundGoal(this));
    }

    public Phase getPhase() {
        return Phase.values()[this.dataTracker.get(PHASE)];
    }

    public void setPhase(Phase phase) {
        if (this.getPhase() == phase) {
            return;
        }
        this.dataTracker.set(PHASE, phase.ordinal());
        this.giveUpCounter = 0;

        EntityAttributeInstance speed = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (phase == Phase.STALKING) {
            this.setTarget(null);
            this.stareTicks.clear();
            if (speed != null) {
                speed.setBaseValue(STALK_SPEED);
            }
        } else if (speed != null) {
            speed.setBaseValue(CHASE_SPEED);
        }
    }

    public Map<UUID, Integer> getStareTicks() {
        return stareTicks;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            return;
        }

        LivingEntity target = this.getTarget();
        Phase phase = this.getPhase();

        if (phase != Phase.STALKING) {
            boolean lost = target == null || !target.isAlive()
                    || this.squaredDistanceTo(target) > GIVE_UP_RANGE * GIVE_UP_RANGE;
            if (lost) {
                if (++giveUpCounter > GIVE_UP_TICKS) {
                    setPhase(Phase.STALKING);
                }
            } else {
                giveUpCounter = 0;
            }
        }

        if (phase == Phase.ATTACKING && target != null
                && this.squaredDistanceTo(target) > ATTACK_GIVE_UP_RANGE * ATTACK_GIVE_UP_RANGE) {
            setPhase(Phase.CHASING);
        }

        if (phase == Phase.ATTACKING && this.age % TAUNT_INTERVAL_TICKS == 0 && this.random.nextFloat() < 0.6F) {
            playRandomTaunt();
        }
    }

    private void playRandomTaunt() {
        var taunts = ModSounds.TACO_BOSS_TAUNTS;
        SoundEvent taunt = taunts.get(this.random.nextInt(taunts.size()));
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), taunt,
                SoundCategory.HOSTILE, 1.6F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }
}
