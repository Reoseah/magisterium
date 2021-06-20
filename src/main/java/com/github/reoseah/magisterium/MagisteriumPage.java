package com.github.reoseah.magisterium;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public enum MagisteriumPage {
	TEST_PAGE_0(0, Magisterium.createId("textures/gui/test_page_0.png")) {
	},
	TEST_PAGE_1(0, Magisterium.createId("textures/gui/test_page_1.png")) {
	},
	TEST_PAGE_2(0, Magisterium.createId("textures/gui/test_page_2.png")) {
	},
	TEST_PAGE_3(0, Magisterium.createId("textures/gui/test_page_3.png")) {
	},
	WITCHES_BARRIER(2, Magisterium.createId("textures/gui/witches_barrier.png")) {
		public final TranslatableText title = new TranslatableText("container.magisterium.witches_barrier");
		public final TranslatableText desc0 = new TranslatableText("container.magisterium.witches_barrier.desc0");
		public final TranslatableText desc1 = new TranslatableText("container.magisterium.witches_barrier.desc1");

		@Override
		public void drawForeground(MagisteriumScreen screen, MatrixStack matrices, int mouseX, int mouseY) {
			super.drawForeground(screen, matrices, mouseX, mouseY);
			TextRenderer tr = screen.getTextRenderer();
			int titleX = X_CENTER - tr.getWidth(this.title) / 2;
			tr.draw(matrices, this.title, titleX, Y_OFFSET + 4, 0x000000);

			List<OrderedText> wrappedDesc0 = tr.wrapLines(this.desc0, 114);
			for (int i = 0; i < wrappedDesc0.size(); i++) {
				tr.draw(matrices, wrappedDesc0.get(i), LEFT_X_OFFSET, 32 + i * 9, 0x000000);
			}

			tr.drawWithShadow(matrices, "5", X_CENTER - tr.getWidth("5") / 2, 95, 0x80FF20);
		}
	};

	protected static final int LEFT_X_OFFSET = 20;
	protected static final int RIGHT_X_OFFSET = 140;
	protected static final int Y_OFFSET = 12;
	protected static final int X_CENTER = 68;

	protected final Identifier texture;
	public final int slots;

	private MagisteriumPage(int slots, Identifier texture) {
		this.slots = slots;
		this.texture = texture;
	}

	@Environment(EnvType.CLIENT)
	public void drawBackground(MagisteriumScreen screen, MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, this.texture);
		screen.drawTexture(matrices, screen.getX(), screen.getY(), 0, 0, 256, 180);
	}

	public void drawForeground(MagisteriumScreen magisteriumScreen, MatrixStack matrices, int mouseX, int mouseY) {

	}
}
