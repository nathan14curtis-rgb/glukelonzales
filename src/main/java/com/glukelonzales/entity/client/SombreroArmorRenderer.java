package com.glukelonzales.entity.client;

import com.glukelonzales.Glukelonzales;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/** Renders the sombrero on the wearer's head using {@link SombreroModel}. */
public class SombreroArmorRenderer implements ArmorRenderer {
    private static final Identifier TEXTURE = Glukelonzales.id("textures/entity/sombrero.png");

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
                        LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {
        if (slot != EquipmentSlot.HEAD) {
            return;
        }

        SombreroModel model = new SombreroModel(
                MinecraftClient.getInstance().getEntityModelLoader().getModelPart(SombreroModel.LAYER));
        model.getRoot().copyTransform(contextModel.getHead());
        ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, model, TEXTURE);
    }
}
