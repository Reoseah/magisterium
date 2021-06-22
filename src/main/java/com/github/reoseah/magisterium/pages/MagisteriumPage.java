package com.github.reoseah.magisterium.pages;

import java.util.ArrayList;
import java.util.List;

import com.github.reoseah.magisterium.Magisterium;
import com.github.reoseah.magisterium.MagisteriumHandler;
import com.github.reoseah.magisterium.MagisteriumScreen;
import com.github.reoseah.magisterium.mixined.MutableSlot;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public abstract class MagisteriumPage {
	private static final List<MagisteriumPage> ALL = new ArrayList<>();

	public static final MagisteriumPage APPRENTICE_SPELLS = new ApprenticeSpellsPage(0,
			"textures/gui/apprentice_spells.png");
	public static final MagisteriumPage CHARGED_SLAM = new ChargedSlamPage(0, "textures/gui/charged_slam.png");
	public static final MagisteriumPage WITCHES_BARRIER = new WitchsBarrierPage(2, "textures/gui/witchs_barrier.png");
	public static final MagisteriumPage NOXIOUS_BURST = new NoxiousBurstPage(3, "textures/gui/noxious_burst.png");

	protected static final int LEFT_X_OFFSET = 20;
	protected static final int RIGHT_X_OFFSET = 138;
	protected static final int Y_OFFSET = 12;
	protected static final int LEFT_X_CENTER = 68;
	protected static final int RIGHT_X_CENTER = 188;

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
	public void updateWidgets(MagisteriumScreen screen) {
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

	public boolean canQuickTransfer(Slot slot, int index, ItemStack stack) {
		return index < this.slots;
	}

	public static List<MagisteriumPage> all() {
		return ALL;
	}

	public static MagisteriumPage fromIndex(int i) {
		return ALL.get(i);
	}

}
