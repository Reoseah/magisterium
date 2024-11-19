package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;

public class EmptySpellEffect extends SpellEffect {
    public static final EmptySpellEffect INSTANCE = new EmptySpellEffect();

    public static final MapCodec<EmptySpellEffect> CODEC = MapCodec.unit(INSTANCE);

    public EmptySpellEffect() {
        super(0);
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(ServerPlayerEntity player, Inventory inventory, SpellBookScreenHandler.Context screenContext) {

    }
}
