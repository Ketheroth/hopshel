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
		this.ySize = 133;
		this.playerInventoryTitleY = this.ySize - 94;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
		super.drawGuiContainerForegroundLayer(matrixStack, x, y);
		this.font.func_243248_b(matrixStack, this.title, (float) this.titleX, (float) this.titleY, 4210752);
		this.font.func_243248_b(matrixStack, this.playerInventory.getDisplayName(), (float) this.playerInventoryTitleX, (float) this.playerInventoryTitleY, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(GUI);
		int relX = (this.width - this.xSize) / 2;
		int relY = (this.height - this.ySize) / 2;
		this.blit(matrixStack, relX, relY, 0, 0, this.xSize, this.ySize);
	}
}
