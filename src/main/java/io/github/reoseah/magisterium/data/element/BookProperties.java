package io.github.reoseah.magisterium.data.element;

import net.minecraft.util.Identifier;

// TODO: load this from data
//       use nested objects like `bookmark.width`,
//       likely with a few classes like Point, Rectangle, etc.
public final class BookProperties {
    public final Identifier texture;
    public final int pageWidth;
    public final int pageHeight;
    public final int pageY;
    public final int pageLeftX;
    public final int pageRightX;
    public final int bookmarkOffset;
    public final int bookmarkHeight;
    public final int bookmarkWidth;
    public final int bookmarkU;
    public final int bookmarkV;
    public final int bookmarkTipWidth;
    public final int bookmarkTipU;
    public final int bookmarkTipV;
    public final int slotU;
    public final int slotV;
    public final int resultSlotU;
    public final int resultSlotV;

    public final int spellButtonU = 48;
    public final int spellButtonV = 194;
    public final int spellButtonActiveV = 210;
    public final int spellButtonWidth = 12;
    public final int spellButtonHeight = 14;

    public BookProperties(Identifier texture, int pageWidth, int pageHeight, int pageY, int pageLeftX,
                          int pageRightX, int bookmarkOffset, int bookmarkHeight, int bookmarkWidth,
                          int bookmarkU, int bookmarkV, int bookmarkTipWidth, int bookmarkTipU,
                          int bookmarkTipV, int slotU, int slotV, int resultSlotU, int resultSlotV) {
        this.texture = texture;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.pageY = pageY;
        this.pageLeftX = pageLeftX;
        this.pageRightX = pageRightX;
        this.bookmarkOffset = bookmarkOffset;
        this.bookmarkHeight = bookmarkHeight;
        this.bookmarkWidth = bookmarkWidth;
        this.bookmarkU = bookmarkU;
        this.bookmarkV = bookmarkV;
        this.bookmarkTipWidth = bookmarkTipWidth;
        this.bookmarkTipU = bookmarkTipU;
        this.bookmarkTipV = bookmarkTipV;
        this.slotU = slotU;
        this.slotV = slotV;
        this.resultSlotU = resultSlotU;
        this.resultSlotV = resultSlotV;
    }

    public int getBookmarkY(int idx) {
        return this.bookmarkOffset + idx * this.bookmarkHeight;
    }

    public int getBookmarkX(boolean isLeft) {
        if (isLeft) {
            return 256 / 2 - this.bookmarkWidth;
        } else {
            return 256 / 2 + this.bookmarkWidth - this.bookmarkTipWidth;
        }
    }
}
