package com.glukelonzales.entity.custom;

import com.glukelonzales.registry.ModEntities;
import com.glukelonzales.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

/**
 * A thrown taco. Renders as a flying taco (via {@link #getDefaultItem()} — {@code
 * ThrownItemEntity} already implements the client rendering interface and derives the flying
 * icon from this, same as vanilla's snowball/egg); flight physics are real.
 *
 * <p>Two modes: a plain one does {@link #damage} to whatever it directly hits, then discards —
 * used by the boss's ranged attack above half health (4 damage) and the sombrero's shoot ability
 * (10 damage, 5 hearts, before armor). An {@link #explosive} one instead detonates with power
 * {@link #explosionPower} on ANY impact (block or entity), like a ghast fireball with fire
 * disabled — used by the boss's ranged attack once it's at or below half health, at 2x the
 * normal ghast-fireball power.
 */
public class TacoProjectileEntity extends ThrownItemEntity {
    /** Default used only by the {@code (EntityType, World)} constructor, i.e. the client-side
     *  copy created from a spawn packet — the server-side instance that actually deals damage
     *  is always built through one of the constructors below with an explicit amount. */
    private static final float DEFAULT_DAMAGE = 4.0F;
    private static final float DEFAULT_EXPLOSION_POWER = 1.0F; // same as a ghast fireball

    private final float damage;
    private final boolean explosive;
    private final float explosionPower;

    public TacoProjectileEntity(EntityType<? extends TacoProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.damage = DEFAULT_DAMAGE;
        this.explosive = false;
        this.explosionPower = DEFAULT_EXPLOSION_POWER;
    }

    public TacoProjectileEntity(World world, LivingEntity owner) {
        this(world, owner, DEFAULT_DAMAGE);
    }

    public TacoProjectileEntity(World world, LivingEntity owner, float damage) {
        this(world, owner, damage, false, DEFAULT_EXPLOSION_POWER);
    }

    public TacoProjectileEntity(World world, LivingEntity owner, float damage, boolean explosive) {
        this(world, owner, damage, explosive, DEFAULT_EXPLOSION_POWER);
    }

    public TacoProjectileEntity(World world, LivingEntity owner, float damage, boolean explosive, float explosionPower) {
        super(ModEntities.TACO_PROJECTILE, owner, world);
        this.damage = damage;
        this.explosive = explosive;
        this.explosionPower = explosionPower;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.TACO;
    }

    @Override
    protected void onEntityHit(EntityHitResult hitResult) {
        super.onEntityHit(hitResult);
        if (this.getWorld().isClient) {
            return;
        }
        if (explosive) {
            explode();
            return;
        }

        Entity target = hitResult.getEntity();
        Entity owner = this.getOwner();
        if (target == owner) {
            return;
        }
        var source = owner instanceof LivingEntity livingOwner
                ? this.getWorld().getDamageSources().mobProjectile(this, livingOwner)
                : this.getWorld().getDamageSources().generic();
        target.damage(source, this.damage);
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult hitResult) {
        super.onBlockHit(hitResult);
        if (this.getWorld().isClient) {
            return;
        }
        if (explosive) {
            explode();
        } else {
            this.discard();
        }
    }

    private void explode() {
        this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(),
                explosionPower, false, World.ExplosionSourceType.MOB);
        this.discard();
    }
}
