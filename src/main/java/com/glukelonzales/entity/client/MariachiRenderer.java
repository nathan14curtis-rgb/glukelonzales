package com.glukelonzales.entity.client;

import com.glukelonzales.Glukelonzales;
import com.glukelonzales.entity.custom.MariachiEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class MariachiRenderer extends MobEntityRenderer<MariachiEntity, MariachiModel<MariachiEntity>> {
	private static final Identifier TEXTURE = Glukelonzales.id("textures/entity/mariachi.png");

	public MariachiRenderer(EntityRendererFactory.Context context) {
		super(context, new MariachiModel<>(context.getPart(MariachiModel.LAYER)), 0.5f);
	}

	@Override
	public Identifier getTexture(MariachiEntity entity) {
		return TEXTURE;
	}
}
