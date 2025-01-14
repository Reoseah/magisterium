package io.github.reoseah.magisterium.screen;

import io.github.reoseah.magisterium.data.BookLoader;
import io.github.reoseah.magisterium.item.BookmarkItem;
import io.github.reoseah.magisterium.item.SpellBookItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ArcaneTableScreen extends HandledScreen<ArcaneTableScreenHandler> {
    public static final Identifier TEXTURE = Identifier.of("magisterium:textures/gui/arcane_table.png");

    public static final MutableText ONLY_FIRST_SEVEN_BOOKMARKS_WILL_SHOW = Text.translatable("container.magisterium.arcane_table.only_first_seven_bookmarks_will_show");
    public static final MutableText NAME_BOOKMARKS_TO_GIVE_TITLE = Text.translatable("container.magisterium.arcane_table.name_bookmarks_to_give_title");
    public static final MutableText CANNOT_INSERT = Text.translatable("container.magisterium.arcane_table.cannot_insert");

    public ArcaneTableScreen(ArcaneTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 204;
        this.backgroundHeight = 192;
        this.playerInventoryTitleX = 22;
        this.playerInventoryTitleY = this.backgroundHeight - 93;
        this.titleX = Integer.MIN_VALUE;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        if (this.handler.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
            var stack = this.focusedSlot.getStack();

            var book = this.handler.getSlot(0).getStack();
            var bookId = book.get(SpellBookItem.BOOK_PROPERTIES);
            if (bookId != null && this.handler.slots.indexOf(this.focusedSlot) > 18) {
                var bookData = BookLoader.getInstance().books.get(bookId);
                if (bookData != null && !bookData.supportInsertion) {
                    var tooltip = this.getTooltipFromItem(stack);
                    tooltip.add(CANNOT_INSERT.formatted(Formatting.RED));
                    context.drawTooltip(this.textRenderer, tooltip, stack.getTooltipData(), x, y);
                    return;
                }
            }


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
                    tooltip.add(ONLY_FIRST_SEVEN_BOOKMARKS_WILL_SHOW.formatted(Formatting.RED));
                    context.drawTooltip(this.textRenderer, tooltip, stack.getTooltipData(), x, y);
                    return;
                }
                if (!stack.contains(DataComponentTypes.CUSTOM_NAME)) {
                    var tooltip = this.getTooltipFromItem(stack);
                    tooltip.add(NAME_BOOKMARKS_TO_GIVE_TITLE.formatted(Formatting.RED));
                    context.drawTooltip(this.textRenderer, tooltip, stack.getTooltipData(), x, y);
                    return;
                }
            }
        }

        super.drawMouseoverTooltip(context, x, y);
    }
}
