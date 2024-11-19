package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;
import java.util.Optional;

public class PageInventory extends SimpleBlock {
    public static final MapCodec<PageInventory> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Codecs.POSITIVE_INT.optionalFieldOf("height", 0).forGetter(inventory -> inventory.height), //
            SlotProperties.CODEC.codec().listOf().fieldOf("slots").forGetter(inventory -> inventory.slots), //
            Background.CODEC.codec().optionalFieldOf("background").forGetter(inventory -> inventory.background) //
    ).apply(instance, PageInventory::new));

    public final int height;
    public final List<SlotProperties> slots;
    public final Optional<Background> background;

    public PageInventory(int height, List<SlotProperties> slots, Optional<PageInventory.Background> background) {
        this.height = height != 0 ? height : slots.stream().mapToInt(slot -> slot.output ? slot.y + 18 + 4 : slot.y + 18).max().orElse(0) + 1;
        this.background = background;
        this.slots = slots;
    }

    @Override
    public MapCodec<? extends PageElement> getCodec() {
        return CODEC;
    }

    @Override
    protected int getHeight(int width, TextRenderer textRenderer) {
        return this.height;
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        return new Widget(properties, x, y, background, slots);
    }

    @Environment(EnvType.CLIENT)
    private static class Widget implements Drawable, SlotPropertiesProvider {
        private final BookProperties properties;
        private final int x;
        private final int y;
        private final Optional<Background> background;
        private final List<SlotProperties> slots;

        public Widget(BookProperties properties, int x, int y, Optional<Background> background, List<SlotProperties> slots) {
            this.properties = properties;
            this.x = x;
            this.y = y;
            this.background = background;
            this.slots = slots;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            if (this.background.isPresent()) {
                var background = this.background.get();
                context.drawTexture(RenderLayer::getGuiTextured, background.texture, x + background.x, y + background.y, background.u, background.v, background.width, background.height, 256, 256);
            }
            for (SlotProperties slot : this.slots) {
                if (slot.output) {
                    context.drawTexture(RenderLayer::getGuiTextured, properties.texture, x + slot.x - 5, y + slot.y - 5, properties.resultSlotU, properties.resultSlotV, 26, 26, 256, 256);
                } else {
                    context.drawTexture(RenderLayer::getGuiTextured, properties.texture, x + slot.x - 1, y + slot.y - 1, properties.slotU, properties.slotV, 18, 18, 256, 256);
                }
            }
        }

        @Override
        public List<SlotProperties> getSlotProperties() {
            return this.slots.stream().map(slot -> slot.offset(this.x, this.y)).toList();
        }
    }

    public record Background(Identifier texture, int x, int y, int u, int v, int width, int height) {
        public static final MapCodec<Background> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
                Identifier.CODEC.fieldOf("texture").forGetter(Background::texture), //
                Codecs.POSITIVE_INT.fieldOf("x").forGetter(Background::x), //
                Codecs.POSITIVE_INT.fieldOf("y").forGetter(Background::y), //
                Codecs.POSITIVE_INT.fieldOf("u").forGetter(Background::u), //
                Codecs.POSITIVE_INT.fieldOf("v").forGetter(Background::v), //
                Codecs.POSITIVE_INT.fieldOf("width").forGetter(Background::width), //
                Codecs.POSITIVE_INT.fieldOf("height").forGetter(Background::height) //
        ).apply(instance, Background::new));
    }
}
