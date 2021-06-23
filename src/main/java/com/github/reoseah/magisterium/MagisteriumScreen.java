package com.github.reoseah.magisterium;

import com.github.reoseah.magisterium.pages.MagisteriumPage;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
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
	private TexturedButtonWidget confirmButton;

	private int pageIndex = -1;

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
		this.handler.getPage().updateWidgets(this);
	}

	protected void addPageButtons() {
		this.confirmButton = this.addDrawableChild(new TexturedButtonWidget(Integer.MIN_VALUE, Integer.MIN_VALUE, 16,
				16, 48, 192, 16, MAIN, 256, 256, button -> {
					// TODO
				}));
		this.nextPageButton = this.addDrawableChild(new PageTurnWidget(this.x + 206, this.y + 156, true, button -> {
			this.goToNextPage();
		}, true));
		this.previousPageButton = this.addDrawableChild(new PageTurnWidget(this.x + 26, this.y + 156, false, button -> {
			this.goToPreviousPage();
		}, true));
		this.updatePageButtons();
	}

	private void updatePageButtons() {
		this.nextPageButton.visible = this.handler.getPageIndex() < MagisteriumPage.all().size() - 1;
		this.previousPageButton.visible = this.handler.getPageIndex() > 0;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (this.handler.getPageIndex() != this.pageIndex) {
			this.pageIndex = this.handler.getPageIndex();
			this.handler.getPage().updateWidgets(this);
		}
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);
	}

	protected void goToPreviousPage() {
		this.client.interactionManager.clickButton(this.handler.syncId, 0);
		this.handler.setPageIndex(this.handler.getPageIndex() - 1);
		this.updatePageButtons();
	}

	protected void goToNextPage() {
		this.client.interactionManager.clickButton(this.handler.syncId, 1);
		this.handler.setPageIndex(this.handler.getPageIndex() + 1);
		this.updatePageButtons();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}
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

	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		super.drawForeground(matrices, mouseX, mouseY);
		this.handler.getPage().drawForeground(this, matrices, mouseX, mouseY);
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public TextRenderer getTextRenderer() {
		return this.textRenderer;
	}

	public TexturedButtonWidget getConfirmButton() {
		return this.confirmButton;
	}
}
