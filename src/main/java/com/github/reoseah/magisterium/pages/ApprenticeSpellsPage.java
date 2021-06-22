package com.github.reoseah.magisterium.pages;

import java.util.List;

import com.github.reoseah.magisterium.MagisteriumScreen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class ApprenticeSpellsPage extends MagisteriumPage {
	protected static final TranslatableText QUOTE0 = new TranslatableText(
			"container.magisterium.apprentice_spells.quote0");
	protected static final TranslatableText QUOTE1 = new TranslatableText(
			"container.magisterium.apprentice_spells.quote1");

	protected static final TranslatableText CHAPTER = new TranslatableText("container.magisterium.chapter");
	protected static final TranslatableText TITLE1 = new TranslatableText("container.magisterium.apprentice_spells");
	protected static final TranslatableText DESC0 = new TranslatableText(
			"container.magisterium.apprentice_spells.desc0");
	protected static final TranslatableText DESC1 = new TranslatableText(
			"container.magisterium.apprentice_spells.desc1");

	public ApprenticeSpellsPage(int slots, String texture) {
		super(slots, texture);
	}

	@Override
	public void drawForeground(MagisteriumScreen screen, MatrixStack matrices, int mouseX, int mouseY) {
		super.drawForeground(screen, matrices, mouseX, mouseY);

		TextRenderer tr = screen.getTextRenderer();
		tr.draw(matrices, QUOTE0, LEFT_X_CENTER - tr.getWidth(QUOTE0) / 2, 52, 0x000000);
		tr.draw(matrices, QUOTE1, LEFT_X_CENTER - tr.getWidth(QUOTE1) / 2, 118, 0x000000);

		tr.draw(matrices, CHAPTER, RIGHT_X_CENTER - tr.getWidth(CHAPTER) / 2, Y_OFFSET, 0x000000);

		tr.draw(matrices, TITLE1.formatted(Formatting.UNDERLINE), RIGHT_X_CENTER - tr.getWidth(TITLE1) / 2,
				Y_OFFSET + 38, 0x000000);

		List<OrderedText> desc0 = tr.wrapLines(DESC0, 104);
		for (int i = 0; i < desc0.size(); i++) {
			tr.draw(matrices, desc0.get(i), RIGHT_X_OFFSET, Y_OFFSET + 59 + i * 9, 0x000000);
		}

		List<OrderedText> desc1 = tr.wrapLines(DESC1, 104);
		for (int i = 0; i < desc1.size(); i++) {
			tr.draw(matrices, desc1.get(i), RIGHT_X_OFFSET, Y_OFFSET + 112 + i * 9, 0x000000);
		}
	}

}
