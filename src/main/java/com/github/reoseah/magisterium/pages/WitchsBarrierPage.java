package com.github.reoseah.magisterium.pages;

import java.util.List;

import com.github.reoseah.magisterium.MagisteriumHandler;
import com.github.reoseah.magisterium.MagisteriumPage;
import com.github.reoseah.magisterium.MagisteriumScreen;
import com.github.reoseah.magisterium.mixined.MutableSlot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TranslatableText;

public class WitchsBarrierPage extends MagisteriumPage {
	protected static final TranslatableText TITLE = new TranslatableText("container.magisterium.witches_barrier");
	protected static final TranslatableText DESC0 = new TranslatableText("container.magisterium.witches_barrier.desc0");
	protected static final TranslatableText DESC1 = new TranslatableText("container.magisterium.witches_barrier.desc1");

	public WitchsBarrierPage(int slots, String texture) {
		super(slots, texture);
	}

	@Override
	public void onSelected(MagisteriumHandler handler) {
		super.onSelected(handler);
		((MutableSlot) handler.getSlot(0)).setPos(43, 91);
		((MutableSlot) handler.getSlot(1)).setPos(77, 91);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void onSelected(MagisteriumScreen screen) {
		super.onSelected(screen);
		screen.getConfirmButton().visible = true;
		screen.getConfirmButton().setPos(screen.getX() + 61, screen.getY() + 122);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void drawForeground(MagisteriumScreen screen, MatrixStack matrices, int mouseX, int mouseY) {
		super.drawForeground(screen, matrices, mouseX, mouseY);
		TextRenderer tr = screen.getTextRenderer();
		int titleX = X_CENTER - tr.getWidth(WitchsBarrierPage.TITLE) / 2;
		tr.draw(matrices, WitchsBarrierPage.TITLE, titleX, Y_OFFSET + 4, 0x000000);

		List<OrderedText> wrappedDesc0 = tr.wrapLines(WitchsBarrierPage.DESC0, 114);
		for (int i = 0; i < wrappedDesc0.size(); i++) {
			tr.draw(matrices, wrappedDesc0.get(i), LEFT_X_OFFSET, 32 + i * 9, 0x000000);
		}

		tr.drawWithShadow(matrices, "5", X_CENTER - tr.getWidth("5") / 2, 95, 0x80FF20);
	}
}