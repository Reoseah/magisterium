package com.github.reoseah.magisterium;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public enum MagisteriumFold {
	TEST_PAGE_0(Magisterium.createId("textures/gui/test_page_0.png")) {
	},
	TEST_PAGE_1(Magisterium.createId("textures/gui/test_page_1.png")) {
	},
	TEST_PAGE_2(Magisterium.createId("textures/gui/test_page_2.png")) {
	},
	TEST_PAGE_3(Magisterium.createId("textures/gui/test_page_3.png")) {
	};

	protected static final int LEFT_X_OFFSET = 20;
	protected static final int RIGHT_X_OFFSET = 140;
	protected static final int Y_OFFSET = 12;

	protected final Identifier texture;

	private MagisteriumFold(Identifier texture) {
		this.texture = texture;
	}

	@Environment(EnvType.CLIENT)
	public void drawBackground(MagisteriumScreen screen, MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, this.texture);
		screen.drawTexture(matrices, screen.getX(), screen.getY(), 0, 0, 256, 180);
	}
}
