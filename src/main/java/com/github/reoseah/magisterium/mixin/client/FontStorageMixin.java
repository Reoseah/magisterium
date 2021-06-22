package com.github.reoseah.magisterium.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.Glyph;

@Mixin(FontStorage.class)
public class FontStorageMixin {
	private static @Final @Mutable Glyph SPACE;
	static {
		SPACE = () -> {
			return 3.0F;
		};
	}
}
