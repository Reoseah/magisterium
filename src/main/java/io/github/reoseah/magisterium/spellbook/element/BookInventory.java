package io.github.reoseah.magisterium.spellbook.element;


import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class BookInventory extends SimpleBlock {
    public final int height;
    public final SlotProperties[] slots;
    public final @Nullable BookInventory.Image background;

    public BookInventory(int height, @Nullable Image background, SlotProperties... slots) {
        this.height = height != 0 ? height : Arrays.stream(slots).mapToInt(slot -> slot.output ? slot.y + 18 + 4 : slot.y + 18).max().orElse(0) + 1;
        this.background = background;
        this.slots = slots;
    }

    @Override
    protected int getHeight(int width, TextRenderer textRenderer) {
        return this.height;
    }

    @Override
    protected Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        return new Widget(properties, x, y);
    }

    private class Widget implements Drawable, SlotPropertiesProvider {
        private final BookProperties properties;
        private final int x;
        private final int y;

        public Widget(BookProperties properties, int x, int y) {
            this.properties = properties;
            this.x = x;
            this.y = y;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            if (BookInventory.this.background != null) {
                context.drawTexture(
                        BookInventory.this.background.texture,
                        x + BookInventory.this.background.x,
                        y + BookInventory.this.background.y,
                        BookInventory.this.background.u,
                        BookInventory.this.background.v,
                        BookInventory.this.background.width,
                        BookInventory.this.background.height);
            }
            for (SlotProperties slot : BookInventory.this.slots) {
                if (slot.output) {
                    context.drawTexture(properties.texture, x + slot.x - 5, y + slot.y - 5, properties.resultSlotU, properties.resultSlotV, 26, 26);

                } else {
                    context.drawTexture(properties.texture, x + slot.x - 1, y + slot.y - 1, properties.slotU, properties.slotV, 18, 18);
                }
            }
        }

        @Override
        public SlotProperties[] getSlotProperties() {
            return Arrays.stream(BookInventory.this.slots).map(slot -> slot.withOffset(this.x, this.y)).toArray(SlotProperties[]::new);
        }
    }

    public record Image(Identifier texture, int x, int y, int u, int v, int width, int height) {
    }
}
