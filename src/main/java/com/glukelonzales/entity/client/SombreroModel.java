package com.glukelonzales.entity.client;

import com.glukelonzales.Glukelonzales;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * The sombrero, worn on the head. Ported from the Bedrock geometry documented in
 * {@code docs/sombrero-model.md} (cube origins/sizes/uv, texture 128x128).
 *
 * <p>That doc's Bedrock coordinates are y-up with y=24 being the top of the head (i.e. the head
 * bone's pivot height). Minecraft's Java {@link ModelPart} cuboids are y-down and relative to
 * their own pivot (which we place at the head's pivot via {@link #getRoot()} +
 * {@link ModelPart#copyTransform}, see {@code SombreroArmorRenderer}), so each cube's Java-space
 * origin is {@code -(bedrockY + bedrockHeight - 24)}. Every cube in the source doc is centered on
 * x=0 and z=0, so there's no left/right or front/back ambiguity to get wrong in that conversion.
 */
public class SombreroModel extends Model {
    public static final EntityModelLayer LAYER = new EntityModelLayer(Glukelonzales.id("sombrero"), "main");

    private final ModelPart hat;

    public SombreroModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.hat = root.getChild("hat");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        root.addChild("hat", ModelPartBuilder.create()
                        // outer brim
                        .uv(0, 0).cuboid(-16.0f, -1.5f, -16.0f, 32.0f, 1.0f, 32.0f, Dilation.NONE)
                        // inner brim
                        .uv(0, 34).cuboid(-14.0f, -2.5f, -14.0f, 28.0f, 1.0f, 28.0f, Dilation.NONE)
                        // crown
                        .uv(0, 64).cuboid(-5.0f, -10.5f, -5.0f, 10.0f, 9.0f, 10.0f, Dilation.NONE)
                        // hat band
                        .uv(44, 64).cuboid(-6.0f, -4.0f, -6.0f, 12.0f, 2.0f, 12.0f, Dilation.NONE)
                        // crown cap
                        .uv(44, 84).cuboid(-4.0f, -11.5f, -4.0f, 8.0f, 1.0f, 8.0f, Dilation.NONE),
                ModelTransform.NONE);

        return TexturedModelData.of(modelData, 128, 128);
    }

    /** Copy the wearer's head pose onto this before rendering — see SombreroArmorRenderer. */
    public ModelPart getRoot() {
        return hat;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        hat.render(matrices, vertexConsumer, light, overlay, color);
    }
}
