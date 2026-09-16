package com.glukelonzales.item.custom;

import com.glukelonzales.entity.custom.TacoProjectileEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

/**
 * Wearing the sombrero and right-clicking with an empty main hand fires a taco: 10 damage
 * (5 hearts) before armor reduction, so armor lowers it the same way it would any other hit.
 *
 * <p>This is driven by {@link ShootTacoPayload} rather than {@code UseItemCallback} — the empty-
 * hand-in-open-air case turned out not to reliably reach the server (vanilla's client-side
 * decision about whether to even send an interact packet for an empty hand isn't guaranteed the
 * way it is for a held item), so instead the client watches the vanilla "use item" key directly
 * (see {@code SombreroClientHandler}) and tells the server explicitly.
 */
public class SombreroTacoAbility {
    private static final float DAMAGE = 10.0F; // 5 hearts, before armor

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ShootTacoPayload.ID, ShootTacoPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ShootTacoPayload.ID,
                (payload, context) -> shoot(context.player().getServerWorld(), context.player()));
    }

    private static void shoot(ServerWorld world, PlayerEntity player) {
        Vec3d look = player.getRotationVec(1.0F);
        TacoProjectileEntity taco = new TacoProjectileEntity(world, player, DAMAGE);
        taco.setPosition(player.getX() + look.x, player.getEyeY() - 0.1 + look.y * 0.5, player.getZ() + look.z);
        taco.setVelocity(look.x, look.y, look.z, 1.8F, 0.5F);

        world.spawnEntity(taco);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 1.0F, 0.8F);
    }
}
