package com.glukelonzales.item.custom;

import com.glukelonzales.entity.custom.TacoProjectileEntity;
import com.glukelonzales.registry.ModItems;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Wearing the sombrero and right-clicking with an empty main hand fires a taco: 10 damage
 * (5 hearts) before armor reduction, so armor lowers it the same way it would any other hit.
 * Hooks {@link UseItemCallback} rather than overriding {@code ArmorItem#use} because that only
 * fires when the sombrero itself is the held item, not when it's merely worn.
 */
public class SombreroTacoAbility {
    private static final float DAMAGE = 10.0F; // 5 hearts, before armor

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (hand != Hand.MAIN_HAND) {
                return TypedActionResult.pass(ItemStack.EMPTY);
            }
            ItemStack heldStack = player.getStackInHand(hand);
            if (!heldStack.isEmpty()) {
                return TypedActionResult.pass(ItemStack.EMPTY);
            }
            if (!player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.SOMBRERO)) {
                return TypedActionResult.pass(ItemStack.EMPTY);
            }

            shoot(world, player);
            return TypedActionResult.success(heldStack);
        });
    }

    private static void shoot(World world, PlayerEntity player) {
        if (world.isClient) {
            return;
        }

        Vec3d look = player.getRotationVec(1.0F);
        TacoProjectileEntity taco = new TacoProjectileEntity(world, player, DAMAGE);
        taco.setPosition(player.getX() + look.x, player.getEyeY() - 0.1 + look.y * 0.5, player.getZ() + look.z);
        taco.setVelocity(look.x, look.y, look.z, 1.8F, 0.5F);

        ((ServerWorld) world).spawnEntity(taco);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 1.0F, 0.8F);
    }
}
