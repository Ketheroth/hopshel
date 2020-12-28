package com.nbrichau.hopshel.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.nbrichau.hopshel.HopshelMod;
import com.nbrichau.hopshel.client.renderer.entity.model.HopshelModel;
import com.nbrichau.hopshel.entity.HopshelEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class HopshelRenderer extends MobRenderer<HopshelEntity, HopshelModel> {
	private static final ResourceLocation HOPSHEL_TEXTURES = new ResourceLocation(HopshelMod.MODID, "textures/entity/hopshel.png");

	public HopshelRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new HopshelModel(), 0.5F);
	}

	@Override
	public ResourceLocation getEntityTexture(HopshelEntity entity) {
		return HOPSHEL_TEXTURES;
	}

	@Override
	public void render(HopshelEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
}
