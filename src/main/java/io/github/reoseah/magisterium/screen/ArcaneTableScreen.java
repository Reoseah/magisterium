package io.github.reoseah.magisterium.screen;

import io.github.reoseah.magisterium.item.RibbonItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ArcaneTableScreen extends HandledScreen<ArcaneTableScreenHandler> {
    public static final Identifier TEXTURE = Identifier.of("magisterium:textures/gui/arcane_table.png");

    public ArcaneTableScreen(ArcaneTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
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
            if (stack.isOf(RibbonItem.INSTANCE)) {
                int bookmarks = 0;
                for (int i = 1; this.handler.getSlot(i) != this.focusedSlot; i++) {
                    var slot = this.handler.getSlot(i);
                    if (slot.getStack().isOf(RibbonItem.INSTANCE)) {
                        bookmarks++;
                    }
                }
                if (bookmarks > 7) {
                    var tooltip = this.getTooltipFromItem(stack);
                    tooltip.add(Text.translatable("magisterium.gui.only_first_seven_bookmarks_will_show").formatted(Formatting.RED));
                    context.drawTooltip(this.textRenderer, tooltip, stack.getTooltipData(), x, y);
                    return;
                }
            }
        }

        super.drawMouseoverTooltip(context, x, y);
    }
}
