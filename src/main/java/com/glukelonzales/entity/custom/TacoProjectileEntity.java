package com.glukelonzales.entity.custom;

import com.glukelonzales.registry.ModEntities;
import com.glukelonzales.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

/**
 * The boss's special ranged attack. Renders as a flying taco, and the flight physics and damage
 * are real: 4 damage on a direct hit, discards on any impact.
 */
public class TacoProjectileEntity extends ThrownItemEntity implements FlyingItemEntity {
    private static final float DAMAGE = 4.0F;

    public TacoProjectileEntity(EntityType<? extends TacoProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public TacoProjectileEntity(World world, LivingEntity owner) {
        super(ModEntities.TACO_PROJECTILE, owner, world);
    }

    @Override
    protected Item getItem() {
        return ModItems.TACO;
    }

    @Override
    public ItemStack getStack() {
        return new ItemStack(ModItems.TACO);
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
        target.damage((ServerWorld) this.getWorld(), source, DAMAGE);
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
