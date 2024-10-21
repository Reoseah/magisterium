package io.github.reoseah.magisterium.screen;

import com.google.common.collect.ImmutableList;
import io.github.reoseah.magisterium.network.SlotLayoutPayload;
import io.github.reoseah.magisterium.spellbook.BookLayout;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import io.github.reoseah.magisterium.spellbook.SpellData;
import io.github.reoseah.magisterium.spellbook.SpellDataLoader;
import io.github.reoseah.magisterium.spellbook.element.SlotProperties;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class SpellBookScreen extends HandledScreen<SpellBookScreenHandler> {
    private static final Logger LOGGER = LogManager.getLogger();

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

    // FIXME wip code, spell list should be obtained from the item stack
    private final List<Identifier> spellIds = ImmutableList.of( //
            Identifier.of("magisterium:test_spell"), //
            Identifier.of("magisterium:awaken_the_flame"), //
            Identifier.of("magisterium:quench_the_flame") //
    );

    private BookLayout layout;
    private int page;

    private PageTurnWidget previousPageButton;
    private PageTurnWidget nextPageButton;

    // if the screen is too small, the player slots are drawn over the book texture a bit
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
        for (var slot : this.handler.slots) {
            if (slot.inventory instanceof PlayerInventory) {
                ((MutableSlot) slot).magisterium$setPos(slot.x, this.playerSlotsY + 8);
            }
        }

        if (this.layout == null) {
            var builder = new BookLayout.Builder(this.properties);
            for (var spellId : this.spellIds) {
                var spell = SpellDataLoader.SPELLS.get(spellId);
                if (spell == null) {
                    LOGGER.warn("Spell data for id {} not found", spellId);
                    continue;
                }
                for (var element : spell.elements) {
                    element.visit(builder, this.properties, this.textRenderer);
                }
            }
            this.layout = builder.build();
        }

        this.previousPageButton = this.addDrawableChild(new PageTurnWidget(this.x + 26, this.y + 156, false, button -> {
            this.client.interactionManager.clickButton(this.handler.syncId, SpellBookScreenHandler.PREVIOUS_PAGE_BUTTON);
            this.handler.currentPage.set(this.handler.currentPage.get() - 2);
        }, true));
        this.nextPageButton = this.addDrawableChild(new PageTurnWidget(this.x + 206, this.y + 156, true, button -> {
            this.client.interactionManager.clickButton(this.handler.syncId, SpellBookScreenHandler.NEXT_PAGE_BUTTON);
            this.handler.currentPage.set(this.handler.currentPage.get() + 2);
        }, true));

        this.updatePage(this.handler.currentPage.get());
    }

    protected void updatePage(int page) {
        this.page = page;

        this.previousPageButton.visible = page > 0;
        this.nextPageButton.visible = page + 1 < this.layout.getPageCount();

        this.setFocused(null);

        SlotProperties[] slots = this.layout.getFoldSlots(this.page);
        this.handler.applySlotProperties(slots);
        ClientPlayNetworking.send(new SlotLayoutPayload(slots));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int page = this.handler.currentPage.get();
        if (this.page != page) {
            this.updatePage(page);
        }

        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.getMatrices().push();
        context.getMatrices().translate(this.x, this.y, 0);
        int mouseXInGui = mouseX - this.x;
        int mouseYInGui = mouseY - this.y;

        context.drawTexture(this.properties.texture, 0, 0, 0, 0, this.backgroundWidth, this.backgroundHeight);
        context.drawTexture(this.properties.texture, (this.backgroundWidth - PLAYER_SLOTS_WIDTH) / 2, this.playerSlotsY, PLAYER_SLOTS_U, PLAYER_SLOTS_V, PLAYER_SLOTS_WIDTH, PLAYER_SLOTS_HEIGHT);

        for (var element : this.layout.getPage(this.page)) {
            element.render(context, mouseXInGui, mouseYInGui, delta);
        }
        for (var element : this.layout.getPage(this.page + 1)) {
            element.render(context, mouseXInGui, mouseYInGui, delta);
        }

        context.getMatrices().pop();
    }

    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        super.drawMouseoverTooltip(context, x, y);

//        x -= this.x;
//        y -= this.y;
//        int currentPage = this.handler.currentPage.get();
//        int i = 0;
//        for (Int2ObjectMap.Entry<Bookmark> entry : this.layout.bookmarks().int2ObjectEntrySet()) {
//            int chapterPage = entry.getIntKey();
//            if (chapterPage != currentPage) {
//                int bookmarkY = this.properties.getBookmarkY(i);
//                int bookmarkX = 256 / 2 + (chapterPage > currentPage ? this.properties.bookmarkFullWidth - this.properties.bookmarkHiddenWidth : -this.properties.bookmarkFullWidth);
//
//                boolean hovered = x > bookmarkX && x < bookmarkX + this.properties.bookmarkHiddenWidth && y > bookmarkY && y < bookmarkY + this.properties.bookmarkHeight;
//
//                if (hovered) {
//                    context.drawTooltip(this.textRenderer, entry.getValue().getName(), this.x + x, this.y + y);
//                }
//            }
//            i++;
//        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        int currentPage = this.handler.currentPage.get();
//        int i = 0;
//        for (Int2ObjectMap.Entry<Chapter> entry : this.layout.bookmarks().int2ObjectEntrySet()) {
//            int chapterPage = entry.getIntKey();
//
//            if (chapterPage != currentPage) {
//                int bookmarkY = this.y + this.properties.getBookmarkY(i);
//                int bookmarkX = this.x + this.properties.getBookmarkX(chapterPage < currentPage);
//
//                if (mouseX > bookmarkX && mouseX < bookmarkX + this.properties.bookmarkHiddenWidth && mouseY > bookmarkY && mouseY < bookmarkY + this.properties.bookmarkHeight) {
////                    ClientPlayNetworking.send(new UseBookmarkPayload(chapterPage));
//
//                    this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F));
//                    return true;
//                }
//            }
//            i++;
//        }

        for (Drawable drawable : this.layout.getPage(this.page)) {
            if (drawable instanceof Element element) {
                if (element.mouseClicked(mouseX - this.x, mouseY - this.y, button)) {
                    return true;
                }
            }
        }
        for (Drawable drawable : this.layout.getPage(this.page + 1)) {
            if (drawable instanceof Element element) {
                if (element.mouseClicked(mouseX - this.x, mouseY - this.y, button)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Drawable drawable : this.layout.getPage(this.page)) {
            if (drawable instanceof Element element) {
                if (element.mouseReleased(mouseX - this.x, mouseY - this.y, button)) {
                    return true;
                }
            }
        }
        for (Drawable drawable : this.layout.getPage(this.page + 1)) {
            if (drawable instanceof Element element) {
                if (element.mouseReleased(mouseX - this.x, mouseY - this.y, button)) {
                    return true;
                }
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        if (mouseY >= this.y + this.playerSlotsY && mouseY <= this.y + this.playerSlotsY + PLAYER_SLOTS_HEIGHT) {
            return mouseX < left || mouseX >= (left + this.backgroundWidth);
        }
        return super.isClickOutsideBounds(mouseX, mouseY, left, top, button);
    }
}
