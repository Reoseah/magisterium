package com.github.reoseah.magisterium.pages;

import java.util.List;

import com.github.reoseah.magisterium.MagisteriumScreen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class ChargedSlamPage extends MagisteriumPage {
	protected static final TranslatableText TITLE = new TranslatableText("container.magisterium.charged_slam");
	protected static final TranslatableText DESC0 = new TranslatableText("container.magisterium.charged_slam.desc0");

	public ChargedSlamPage(int slots, String texture) {
		super(slots, texture);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void updateWidgets(MagisteriumScreen screen) {
		super.updateWidgets(screen);
		screen.getConfirmButton().visible = true;
		screen.getConfirmButton().setPos(screen.getX() + 180, screen.getY() + 104);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void drawForeground(MagisteriumScreen screen, MatrixStack matrices, int mouseX, int mouseY) {
		super.drawForeground(screen, matrices, mouseX, mouseY);
		TextRenderer tr = screen.getTextRenderer();
		int titleX = LEFT_X_CENTER - tr.getWidth(TITLE) / 2;
		tr.draw(matrices, TITLE.formatted(Formatting.UNDERLINE), titleX, Y_OFFSET + 4, 0x000000);

		List<OrderedText> desc0 = tr.wrapLines(DESC0, 104);
		for (int i = 0; i < desc0.size(); i++) {
			tr.draw(matrices, desc0.get(i), LEFT_X_OFFSET, 32 + i * 9, 0x000000);
		}
	}
}
