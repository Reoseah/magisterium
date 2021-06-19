package com.github.reoseah.magisterium;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MagisteriumScreen extends HandledScreen<MagisteriumHandler> {
	public static final Identifier MAIN = Magisterium.createId("textures/gui/magisterium.png");
	public static final Identifier PLAYER_SLOTS = Magisterium.createId("textures/gui/player_slots.png");

	private PageTurnWidget nextPageButton;
	private PageTurnWidget previousPageButton;

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
		this.addPageButtons();
	}

	protected void addPageButtons() {
		this.nextPageButton = this.addDrawableChild(new PageTurnWidget(this.x + 206, this.y + 156, true, button -> {
			this.goToNextPage();
		}, true));
		this.previousPageButton = this.addDrawableChild(new PageTurnWidget(this.x + 26, this.y + 156, false, button -> {
			this.goToPreviousPage();
		}, true));
		this.updatePageButtons();
	}

	private void updatePageButtons() {
		this.nextPageButton.visible = this.handler.getPageIndex() < MagisteriumPage.values().length - 1;
		this.previousPageButton.visible = this.handler.getPageIndex() > 0;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);
	}

	protected void goToPreviousPage() {
//		      if (this.pageIndex > 0) {
//		         --this.pageIndex;
//		      }

		this.updatePageButtons();
	}

	protected void goToNextPage() {
//		      if (this.pageIndex < this.getPageCount() - 1) {
//		         ++this.pageIndex;
//		      }

		this.updatePageButtons();
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else {
			switch (keyCode) {
			case 266:
				this.previousPageButton.onPress();
				return true;
			case 267:
				this.nextPageButton.onPress();
				return true;
			default:
				return false;
			}
		}
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, MAIN);
		this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, 180);

		RenderSystem.setShaderTexture(0, PLAYER_SLOTS);
		this.drawTexture(matrices, this.x + 41, this.y + 180 + 4, 0, 0, 176, 100);

		this.handler.getPage().drawBackground(this, matrices, delta, mouseX, mouseY);
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}
}
