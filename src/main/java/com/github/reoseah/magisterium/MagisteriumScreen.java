package com.github.reoseah.magisterium;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MagisteriumScreen extends HandledScreen<MagisteriumHandler> {
	public static final Identifier MAIN = Magisterium.createId("textures/gui/magisterium.png");
	public static final Identifier PLAYER_SLOTS = Magisterium.createId("textures/gui/player_slots.png");

	public MagisteriumScreen(MagisteriumHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundWidth = 256;
		this.backgroundHeight = 284;
		this.playerInventoryTitleX = 41 + this.playerInventoryTitleX;
		this.playerInventoryTitleY = this.backgroundHeight - 94;
		this.titleX = Integer.MIN_VALUE;
		this.titleY = Integer.MIN_VALUE;
	}

	@Override
	protected void init() {
		super.init();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, MAIN);
		this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, 180);

		RenderSystem.setShaderTexture(0, PLAYER_SLOTS);
		this.drawTexture(matrices, this.x + 41, this.y + 180 + 4, 0, 0, 176, 100);
	}
}
