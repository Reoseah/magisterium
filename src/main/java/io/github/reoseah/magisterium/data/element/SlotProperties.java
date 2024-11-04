package io.github.reoseah.magisterium.data.element;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public class SlotProperties {
    public static final MapCodec<SlotProperties> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Codecs.NON_NEGATIVE_INT.fieldOf("x").forGetter(slot -> slot.x), //
            Codecs.NON_NEGATIVE_INT.fieldOf("y").forGetter(slot -> slot.y), //
            Codec.BOOL.optionalFieldOf("output", false).forGetter(slot -> slot.output), //
            Ingredient.CODEC.optionalFieldOf("ingredient").forGetter(slot -> slot.ingredient), //
            Identifier.CODEC.optionalFieldOf("background").forGetter(slot -> slot.background) //
    ).apply(instance, SlotProperties::new));

    public final int x;
    public final int y;
    public final boolean output;
    public final Optional<Ingredient> ingredient;
    public final Optional<Identifier> background;

    public SlotProperties(int x, int y, boolean output, Optional<Ingredient> ingredient, Optional<Identifier> background) {
        this.x = x;
        this.y = y;
        this.background = background;
        this.ingredient = ingredient;
        this.output = output;
    }

    public SlotProperties offset(int x, int y) {
        return new SlotProperties(this.x + x, this.y + y, this.output, this.ingredient, this.background);
    }

    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return this.background.isPresent() ? Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, this.background.get()) : null;
    }

    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(this.x);
        buf.writeVarInt(this.y);
        buf.writeBoolean(this.output);
        if (this.ingredient.isPresent()) {
            buf.writeBoolean(true);
            Ingredient.PACKET_CODEC.encode(buf, this.ingredient.get());
        } else {
            buf.writeBoolean(false);
        }
        if (this.background.isPresent()) {
            buf.writeBoolean(true);
            buf.writeIdentifier(this.background.get());
        } else {
            buf.writeBoolean(false);
        }
    }

    public static SlotProperties read(RegistryByteBuf buf) {
        int x = buf.readVarInt();
        int y = buf.readVarInt();
        boolean output = buf.readBoolean();
        Optional<Ingredient> ingredient = buf.readBoolean() ? Optional.of(Ingredient.PACKET_CODEC.decode(buf)) : Optional.empty();
        Optional<Identifier> background = buf.readBoolean() ? Optional.of(buf.readIdentifier()) : Optional.empty();

        return new SlotProperties(x, y, output, ingredient, background);
    }
}
