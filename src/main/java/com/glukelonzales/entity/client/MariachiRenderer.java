package com.glukelonzales.entity.client;

import com.glukelonzales.Glukelonzales;
import com.glukelonzales.entity.custom.MariachiEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

/** Standard player proportions (see the note on {@code ModEntities.MARIACHI_HEIGHT} for why). */
public class MariachiRenderer extends MobEntityRenderer<MariachiEntity, PlayerEntityModel<MariachiEntity>> {
	private static final Identifier TEXTURE = Glukelonzales.id("textures/entity/mariachi.png");

	public MariachiRenderer(EntityRendererFactory.Context context) {
		super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
	}

	@Override
	public Identifier getTexture(MariachiEntity entity) {
		return TEXTURE;
	}
}
