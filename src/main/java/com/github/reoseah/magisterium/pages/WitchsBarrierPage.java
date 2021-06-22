package com.github.reoseah.magisterium.pages;

import java.util.List;

import com.github.reoseah.magisterium.MagisteriumHandler;
import com.github.reoseah.magisterium.MagisteriumScreen;
import com.github.reoseah.magisterium.mixined.MutableSlot;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class WitchsBarrierPage extends MagisteriumPage {
	protected static final TranslatableText TITLE = new TranslatableText("container.magisterium.witchs_barrier");
	protected static final TranslatableText DESC0 = new TranslatableText("container.magisterium.witchs_barrier.desc0");
	protected static final TranslatableText DESC1 = new TranslatableText("container.magisterium.witchs_barrier.desc1");
	protected static final TranslatableText DESC2 = new TranslatableText("container.magisterium.witchs_barrier.desc2");

	public WitchsBarrierPage(int slots, String texture) {
		super(slots, texture);
	}

	@Override
	public void onSelected(MagisteriumHandler handler) {
		super.onSelected(handler);
		((MutableSlot) handler.getSlot(0)).setPos(43, 91);
		((MutableSlot) handler.getSlot(1)).setPos(77, 91);
	}

	@Override
	public boolean canQuickTransfer(Slot slot, int index, ItemStack stack) {
		Item item = stack.getItem();
		return switch (index) {
		case 0 -> item == Items.REDSTONE // duration
				|| item == Items.GLOWSTONE_DUST // strength
				|| item == Items.GUNPOWDER || item == Items.SUGAR;
		case 1 -> item == Items.MAGMA_CREAM // fire aspect
				|| item == Items.SLIME_BALL;
		default -> false;
		};
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void updateWidgets(MagisteriumScreen screen) {
		super.updateWidgets(screen);
		screen.getConfirmButton().visible = true;
		screen.getConfirmButton().setPos(screen.getX() + 61, screen.getY() + 122);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void drawForeground(MagisteriumScreen screen, MatrixStack matrices, int mouseX, int mouseY) {
		super.drawForeground(screen, matrices, mouseX, mouseY);
		TextRenderer tr = screen.getTextRenderer();
		int titleX = LEFT_X_CENTER - tr.getWidth(TITLE) / 2;
		tr.draw(matrices, TITLE.formatted(Formatting.UNDERLINE), titleX, Y_OFFSET + 4, 0x000000);

		List<OrderedText> desc0 = tr.wrapLines(DESC0, 108);
		for (int i = 0; i < desc0.size(); i++) {
			tr.draw(matrices, desc0.get(i), LEFT_X_OFFSET, 32 + i * 9, 0x000000);
		}

		tr.drawWithShadow(matrices, "5", LEFT_X_CENTER - tr.getWidth("5") / 2, 95, 0x80FF20);

		List<OrderedText> desc1 = tr.wrapLines(DESC1, 108);
		for (int i = 0; i < desc1.size(); i++) {
			tr.draw(matrices, desc1.get(i), LEFT_X_OFFSET, 140 + i * 9, 0x000000);
		}

		List<OrderedText> desc2 = tr.wrapLines(DESC2, 104);
		for (int i = 0; i < desc2.size(); i++) {
			tr.draw(matrices, desc2.get(i), RIGHT_X_OFFSET, Y_OFFSET + 6 + i * 9, 0x000000);
		}
	}

}