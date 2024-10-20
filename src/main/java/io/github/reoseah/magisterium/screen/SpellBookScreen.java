package io.github.reoseah.magisterium.screen;

import io.github.reoseah.magisterium.spellbook.BookProperties;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SpellBookScreen extends HandledScreen<SpellBookScreenHandler> {
    public static final Identifier TEXTURE = Identifier.of("magisterium:textures/gui/spell_book.png");

    private static final int PAGE_WIDTH = 102;
    private static final int PAGE_HEIGHT = 140;
    private static final int TOP_OFFSET = 15;
    private static final int LEFT_PAGE_OFFSET = 17;
    private static final int RIGHT_PAGE_OFFSET = 138;

    private static final int BOOKMARK_OFFSET = TOP_OFFSET + 4;
    private static final int BOOKMARK_HEIGHT = 20;
    private static final int FULL_BOOKMARK_WIDTH = 135;
    private static final int FULL_BOOKMARK_U = 64;
    private static final int FULL_BOOKMARK_V = 192;
    private static final int HIDDEN_BOOKMARK_WIDTH = 16;
    private static final int HIDDEN_BOOKMARK_U = 224;
    private static final int HIDDEN_BOOKMARK_V = 192;

    private static final int PLAYER_SLOTS_U = 0;
    private static final int PLAYER_SLOTS_V = 224;
    private static final int PLAYER_SLOTS_HEIGHT = 32;
    private static final int PLAYER_SLOTS_WIDTH = 176;

    private static final int SLOT_U = 202;
    private static final int SLOT_V = 224;
    private static final int RESULT_SLOT_U = 176;
    private static final int RESULT_SLOT_V = 224;

    private final BookProperties properties = new BookProperties(TEXTURE, PAGE_WIDTH, PAGE_HEIGHT, TOP_OFFSET, LEFT_PAGE_OFFSET, RIGHT_PAGE_OFFSET, BOOKMARK_OFFSET, BOOKMARK_HEIGHT, FULL_BOOKMARK_WIDTH, FULL_BOOKMARK_U, FULL_BOOKMARK_V, HIDDEN_BOOKMARK_WIDTH, HIDDEN_BOOKMARK_U, HIDDEN_BOOKMARK_V, SLOT_U, SLOT_V, RESULT_SLOT_U, RESULT_SLOT_V);

    private int playerSlotsY;

    public SpellBookScreen(SpellBookScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 256;
        this.backgroundHeight = 180;
        this.playerInventoryTitleX = Integer.MIN_VALUE;
        this.playerInventoryTitleY = Integer.MIN_VALUE;
        this.titleX = Integer.MIN_VALUE;
        this.titleY = Integer.MIN_VALUE;
    }

    @Override
    protected void init() {
        super.init();

        this.playerSlotsY = Math.min(this.backgroundHeight + 4, this.height - this.y - PLAYER_SLOTS_HEIGHT);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.getMatrices().push();
        context.getMatrices().translate(this.x, this.y, 0);

        context.drawTexture(this.properties.texture, 0, 0, 0, 0, this.backgroundWidth, this.backgroundHeight);
        context.drawTexture(this.properties.texture, (this.backgroundWidth - PLAYER_SLOTS_WIDTH) / 2, this.playerSlotsY, PLAYER_SLOTS_U, PLAYER_SLOTS_V, PLAYER_SLOTS_WIDTH, PLAYER_SLOTS_HEIGHT);
    }
}
