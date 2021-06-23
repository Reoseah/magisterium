package com.github.reoseah.magisterium.pages;

import java.util.List;

import com.github.reoseah.magisterium.MagisteriumHandler;
import com.github.reoseah.magisterium.MagisteriumScreen;
import com.github.reoseah.magisterium.mixined.MutableSlot;
import com.mojang.datafixers.util.Pair;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class NoxiousBurstPage extends MagisteriumPage {
	protected static final TranslatableText TITLE = new TranslatableText("container.magisterium.noxious_burst");
	protected static final TranslatableText DESC0 = new TranslatableText("container.magisterium.noxious_burst.desc0");

	public NoxiousBurstPage(int slots, String texture) {
		super(slots, texture);
	}

	@Override
	public void onSelected(MagisteriumHandler handler) {
		super.onSelected(handler);
		((MutableSlot) handler.getSlot(0)).setPos(166, 82);
		((MutableSlot) handler.getSlot(1)).setPos(180, 55);
		((MutableSlot) handler.getSlot(2)).setPos(194, 82);
	}

	@Override
	public boolean canQuickTransfer(Slot slot, int index, ItemStack stack) {
		Item item = stack.getItem();
		return switch (index) {
		case 0 -> item == Items.REDSTONE // duration
				|| item == Items.GLOWSTONE_DUST // strength
			;
		case 1 -> item == Items.FERMENTED_SPIDER_EYE;
		case 2 -> item == Items.SUGAR || item == Items.RABBIT_FOOT // Slowness
				|| item == Items.GLISTERING_MELON_SLICE // Harming
				|| item == Items.SPIDER_EYE // Poison
			;
		default -> false;
		};
	}

	@Override
	public Pair<Identifier, Identifier> getBackgroundSprite(int index) {
		return switch (index) {
		case 0 -> SLOT_DUST;
		case 1 -> SLOT_FERMENTED_SPIDER_EYE;
		case 2 -> SLOT_QUESTION_MARK;
		default -> null;
		};
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

	@Environment(EnvType.CLIENT)
	@Override
	public void updateWidgets(MagisteriumScreen screen) {
		super.updateWidgets(screen);
		screen.getConfirmButton().visible = true;
		screen.getConfirmButton().setPos(screen.getX() + 180, screen.getY() + 104);
	}

}
