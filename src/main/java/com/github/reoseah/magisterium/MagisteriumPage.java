package com.github.reoseah.magisterium;

import java.util.ArrayList;
import java.util.List;

import com.github.reoseah.magisterium.mixined.MutableSlot;
import com.github.reoseah.magisterium.pages.WitchsBarrierPage;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class MagisteriumPage {
	private static final List<MagisteriumPage> ALL = new ArrayList<>();

	public static final MagisteriumPage WITCHES_BARRIER = new WitchsBarrierPage(2, "textures/gui/witches_barrier.png");

	protected static final int LEFT_X_OFFSET = 20;
	protected static final int RIGHT_X_OFFSET = 140;
	protected static final int Y_OFFSET = 12;
	protected static final int X_CENTER = 68;

	protected final Identifier texture;
	public final int slots;

	protected MagisteriumPage(int slots, String texture) {
		this.slots = slots;
		this.texture = Magisterium.createId(texture);
		ALL.add(this);
	}

	public void onSelected(MagisteriumHandler handler) {
		for (int i = this.slots; i < 16; i++) {
			((MutableSlot) handler.getSlot(i)).setPos(Integer.MIN_VALUE, Integer.MIN_VALUE);
		}
	}

	@Environment(EnvType.CLIENT)
	public void onSelected(MagisteriumScreen screen) {
		screen.getConfirmButton().visible = false;
	}

	@Environment(EnvType.CLIENT)
	public void drawBackground(MagisteriumScreen screen, MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, this.texture);
		screen.drawTexture(matrices, screen.getX(), screen.getY(), 0, 0, 256, 180);
	}

	@Environment(EnvType.CLIENT)
	public void drawForeground(MagisteriumScreen magisteriumScreen, MatrixStack matrices, int mouseX, int mouseY) {

	}

	public static List<MagisteriumPage> all() {
		return ALL;
	}

	public static MagisteriumPage fromIndex(int i) {
		return ALL.get(i);
	}
}
