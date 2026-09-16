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
 * icon from this, same as vanilla's snowball/egg); flight physics are real, and it does {@link
 * #damage} on a direct hit before discarding. Used both by the boss's ranged attack (4 damage)
 * and the sombrero's shoot ability (10 damage — 5 hearts — before armor reduction).
 */
public class TacoProjectileEntity extends ThrownItemEntity {
    /** Default used only by the {@code (EntityType, World)} constructor, i.e. the client-side
     *  copy created from a spawn packet — the server-side instance that actually deals damage
     *  is always built through one of the constructors below with an explicit amount. */
    private static final float DEFAULT_DAMAGE = 4.0F;

    private final float damage;

    public TacoProjectileEntity(EntityType<? extends TacoProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.damage = DEFAULT_DAMAGE;
    }

    public TacoProjectileEntity(World world, LivingEntity owner) {
        this(world, owner, DEFAULT_DAMAGE);
    }

    public TacoProjectileEntity(World world, LivingEntity owner, float damage) {
        super(ModEntities.TACO_PROJECTILE, owner, world);
        this.damage = damage;
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
        if (!this.getWorld().isClient) {
            this.discard();
        }
    }
}
