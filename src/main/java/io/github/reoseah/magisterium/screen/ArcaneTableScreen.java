package io.github.reoseah.magisterium.screen;

import io.github.reoseah.magisterium.item.BookmarkItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ArcaneTableScreen extends HandledScreen<ArcaneTableScreenHandler> {
    public static final Identifier TEXTURE = Identifier.of("magisterium:textures/gui/arcane_table.png");

    public ArcaneTableScreen(ArcaneTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 212;
        this.backgroundHeight = 192;
        this.playerInventoryTitleX = 16;
        this.playerInventoryTitleY = this.backgroundHeight - 93;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = 122 - this.textRenderer.getWidth(this.title) / 2;
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0xf6b734, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        if (this.handler.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
            var stack = this.focusedSlot.getStack();
            if (stack.isOf(BookmarkItem.INSTANCE)) {
                int bookmarks = 0;
                for (int i = 1; this.handler.getSlot(i) != this.focusedSlot && i < 19; i++) {
                    var slot = this.handler.getSlot(i);
                    if (slot.getStack().isOf(BookmarkItem.INSTANCE)) {
                        bookmarks++;
                    }
                }
                if (bookmarks > 7) {
                    var tooltip = this.getTooltipFromItem(stack);
                    tooltip.add(Text.translatable("magisterium.gui.only_first_seven_bookmarks_will_show").formatted(Formatting.RED));
                    context.drawTooltip(this.textRenderer, tooltip, stack.getTooltipData(), x, y);
                    return;
                }
                if (!stack.contains(DataComponentTypes.CUSTOM_NAME)) {
                    var tooltip = this.getTooltipFromItem(stack);
                    tooltip.add(Text.translatable("magisterium.gui.name_bookmarks_to_give_title").formatted(Formatting.RED));
                    context.drawTooltip(this.textRenderer, tooltip, stack.getTooltipData(), x, y);
                    return;
                }
            }
        }

        super.drawMouseoverTooltip(context, x, y);
    }
}
