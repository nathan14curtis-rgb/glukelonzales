package com.glukelonzales.entity.client;

import com.glukelonzales.Glukelonzales;
import com.glukelonzales.entity.custom.MariachiEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;

/**
 * A player-shaped model built against a standard 64x64 skin, with one difference:
 * the legs are {@link #LEG_SHORTENING} pixels shorter than Steve's.
 *
 * <p>Vanilla proportions, in model pixels (16 px = 1 block), y grows downwards:
 * head occupies -8..0, body 0..12, legs 12..24 — 32 px overall, feet resting at y=24.
 * Shortening the legs alone would leave the feet floating, so everything above the
 * legs is pushed down by the same amount ({@link #VERTICAL_OFFSET}) and the feet stay
 * planted at y=24. The model therefore stands 28 px (1.75 blocks) tall instead of 32.
 */
public class MariachiModel<T extends MariachiEntity> extends BipedEntityModel<T> {
	public static final EntityModelLayer LAYER =
			new EntityModelLayer(Glukelonzales.id("mariachi"), "main");

	/** Vanilla biped leg height, in model pixels. */
	public static final float VANILLA_LEG_HEIGHT = 12.0f;
	/** How much shorter this character's legs are than Steve's, in model pixels. */
	public static final float LEG_SHORTENING = 4.0f;
	/** This character's leg height, in model pixels. */
	public static final float LEG_HEIGHT = VANILLA_LEG_HEIGHT - LEG_SHORTENING;
	/** Head, body and arms drop by this much so the feet stay on the ground. */
	public static final float VERTICAL_OFFSET = LEG_SHORTENING;

	/** Thickness of the second (overlay) skin layer. */
	private static final Dilation OVERLAY = new Dilation(0.25f);
	/** The hat layer is slightly thicker in vanilla — it carries the sombrero here. */
	private static final Dilation HAT_LAYER = new Dilation(0.5f);

	public MariachiModel(ModelPart root) {
		super(root);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();
		float dy = VERTICAL_OFFSET;

		root.addChild("head",
				ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, Dilation.NONE),
				ModelTransform.pivot(0.0f, dy, 0.0f));
		root.addChild("hat",
				ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, HAT_LAYER),
				ModelTransform.pivot(0.0f, dy, 0.0f));

		ModelPartData body = root.addChild("body",
				ModelPartBuilder.create().uv(16, 16).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, Dilation.NONE),
				ModelTransform.pivot(0.0f, dy, 0.0f));
		body.addChild("jacket",
				ModelPartBuilder.create().uv(16, 32).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, OVERLAY),
				ModelTransform.NONE);

		ModelPartData rightArm = root.addChild("right_arm",
				ModelPartBuilder.create().uv(40, 16).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, Dilation.NONE),
				ModelTransform.pivot(-5.0f, 2.0f + dy, 0.0f));
		rightArm.addChild("right_sleeve",
				ModelPartBuilder.create().uv(40, 32).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, OVERLAY),
				ModelTransform.NONE);

		ModelPartData leftArm = root.addChild("left_arm",
				ModelPartBuilder.create().uv(32, 48).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, Dilation.NONE),
				ModelTransform.pivot(5.0f, 2.0f + dy, 0.0f));
		leftArm.addChild("left_sleeve",
				ModelPartBuilder.create().uv(48, 48).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, OVERLAY),
				ModelTransform.NONE);

		// Legs keep their vanilla pivot height (12) plus the offset, so they hang from the
		// lowered body; being LEG_HEIGHT tall they end at 12 + 4 + 8 = 24, the ground line.
		ModelPartData rightLeg = root.addChild("right_leg",
				ModelPartBuilder.create().uv(0, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, LEG_HEIGHT, 4.0f, Dilation.NONE),
				ModelTransform.pivot(-1.9f, VANILLA_LEG_HEIGHT + dy, 0.0f));
		rightLeg.addChild("right_pants",
				ModelPartBuilder.create().uv(0, 32).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, LEG_HEIGHT, 4.0f, OVERLAY),
				ModelTransform.NONE);

		ModelPartData leftLeg = root.addChild("left_leg",
				ModelPartBuilder.create().uv(16, 48).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, LEG_HEIGHT, 4.0f, Dilation.NONE),
				ModelTransform.pivot(1.9f, VANILLA_LEG_HEIGHT + dy, 0.0f));
		leftLeg.addChild("left_pants",
				ModelPartBuilder.create().uv(0, 48).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, LEG_HEIGHT, 4.0f, OVERLAY),
				ModelTransform.NONE);

		return TexturedModelData.of(modelData, 64, 64);
	}
}
