package com.glukelonzales.item.custom;

import com.glukelonzales.Glukelonzales;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * "Fire a taco" — sent client to server when the player right-clicks with an empty main hand
 * while wearing the sombrero. Carries no data; see {@link SombreroTacoAbility}.
 */
public record ShootTacoPayload() implements CustomPayload {
    public static final Id<ShootTacoPayload> ID = new Id<>(Glukelonzales.id("shoot_taco"));
    public static final PacketCodec<PacketByteBuf, ShootTacoPayload> CODEC = PacketCodec.unit(new ShootTacoPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
