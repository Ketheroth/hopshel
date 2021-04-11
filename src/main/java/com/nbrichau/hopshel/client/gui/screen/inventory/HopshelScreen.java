package com.nbrichau.hopshel.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.nbrichau.hopshel.HopshelMod;
import com.nbrichau.hopshel.inventory.container.HopshelContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class HopshelScreen extends ContainerScreen<HopshelContainer> {
	private ResourceLocation GUI = new ResourceLocation(HopshelMod.MODID, "textures/gui/hopshel_gui.png");

	public HopshelScreen(HopshelContainer container, PlayerInventory inv, ITextComponent name) {
		super(container, inv, name);
		this.imageHeight = 133;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int x, int y) {
		super.renderLabels(matrixStack, x, y);
		this.font.draw(matrixStack, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
		this.font.draw(matrixStack, this.inventory.getDisplayName(), (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(GUI);
		int relX = (this.width - this.imageWidth) / 2;
		int relY = (this.height - this.imageHeight) / 2;
		this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
	}
}
