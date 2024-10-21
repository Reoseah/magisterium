package io.github.reoseah.magisterium.spellbook.element;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import com.mojang.datafixers.util.Pair;

public class SlotProperties {
    public final int x;
    public final int y;
    public final boolean output;
    public final Ingredient ingredient;
    public final Identifier background;

    public SlotProperties(int x, int y, boolean output, Ingredient ingredient) {
        this(x, y, output, ingredient, null);
    }

    public SlotProperties(int x, int y, boolean output, Ingredient ingredient, Identifier background) {
        this.x = x;
        this.y = y;
        this.background = background;
        this.ingredient = ingredient;
        this.output = output;
    }

    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return this.background != null ? Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, this.background) : null;
    }

    public SlotProperties withOffset(int x, int y) {
        return new SlotProperties(this.x + x, this.y + y, this.output, this.ingredient, this.background);
    }

    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(this.x);
        buf.writeVarInt(this.y);
        buf.writeBoolean(this.output);
        if (this.ingredient != null) {
            buf.writeBoolean(true);
            Ingredient.PACKET_CODEC.encode(buf, this.ingredient);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static SlotProperties read(RegistryByteBuf buf) {
        int x = buf.readVarInt();
        int y = buf.readVarInt();
        boolean output = buf.readBoolean();
        Ingredient ingredient = buf.readBoolean() ? Ingredient.PACKET_CODEC.decode(buf) : null;

        return new SlotProperties(x, y, output, ingredient);
    }
}
