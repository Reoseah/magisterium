package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EmptySpellEffect extends SpellEffect {
    public static final EmptySpellEffect INSTANCE = new EmptySpellEffect();

    public static final MapCodec<EmptySpellEffect> CODEC = MapCodec.unit(INSTANCE);

    public EmptySpellEffect() {
        super(Identifier.of("magisterium:empty"), 0);
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(ServerPlayerEntity player, Inventory inventory, SpellBookScreenHandler.Context screenContext) {

    }
}
