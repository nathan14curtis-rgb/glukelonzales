package com.glukelonzales.client;

import com.glukelonzales.item.custom.ShootTacoPayload;
import com.glukelonzales.registry.ModItems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;

/**
 * Watches the vanilla "use item" key directly (rather than a Fabric interaction event) for the
 * sombrero's shoot ability, since an empty main hand with nothing targeted doesn't reliably send
 * an interact packet on its own. Fires once per discrete press (rising edge), not continuously
 * while held.
 */
public class SombreroClientHandler {
    private static boolean wasKeyDown;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(SombreroClientHandler::tick);
    }

    private static void tick(MinecraftClient client) {
        boolean keyDown = client.options.useKey.isPressed();
        boolean justPressed = keyDown && !wasKeyDown;
        wasKeyDown = keyDown;

        if (!justPressed) {
            return;
        }

        ClientPlayerEntity player = client.player;
        if (player == null || client.currentScreen != null || !player.getMainHandStack().isEmpty()) {
            return;
        }
        if (!player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.SOMBRERO)) {
            return;
        }

        ClientPlayNetworking.send(new ShootTacoPayload());
    }
}
