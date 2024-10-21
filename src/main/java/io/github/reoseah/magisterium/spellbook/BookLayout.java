package io.github.reoseah.magisterium.spellbook;

import io.github.reoseah.magisterium.spellbook.element.BookInventory;
import io.github.reoseah.magisterium.spellbook.element.Bookmark;
import io.github.reoseah.magisterium.spellbook.element.SlotConfiguration;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.gui.Drawable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public record BookLayout(
        Int2ObjectMap<List<Drawable>> pages,
        // TODO rename to bookmarks
        Int2ObjectMap<Bookmark> chapters
) {
    public BookLayout(Int2ObjectMap<List<Drawable>> pages, Int2ObjectMap<Bookmark> chapters) {
        this.pages = Int2ObjectMaps.unmodifiable(pages);
        this.chapters = Int2ObjectMaps.unmodifiable(chapters);
    }

    public int getPageCount() {
        return this.pages.keySet().intStream().max().orElse(1);
    }

    public List<Drawable> getPage(int page) {
        return this.pages.getOrDefault(page, Collections.emptyList());
    }

    public SlotConfiguration[] getFoldSlots(int leftPage) {
        // TODO build and keep a separate map of slots per page
        Stream<SlotConfiguration> leftSlots = this.getPage(leftPage).stream()
                .filter(drawable -> drawable instanceof BookInventory)
                .flatMap(drawable -> Arrays.stream(((BookInventory) drawable).getSlots()));
        Stream<SlotConfiguration> rightSlots = this.getPage(leftPage + 1).stream()
                .filter(drawable -> drawable instanceof BookInventory)
                .flatMap(drawable -> Arrays.stream(((BookInventory) drawable).getSlots()));

        return Stream.concat(leftSlots, rightSlots).limit(16).toArray(SlotConfiguration[]::new);
    }

    public static class Builder {
        private final int leftX, rightX;
        private final int paddingTop;
        private final int pageHeight;

        private final Int2ObjectMap<List<Drawable>> pages = new Int2ObjectArrayMap<>();
        private final Int2ObjectMap<Bookmark> chapters = new Int2ObjectArrayMap<>();

        private int currentPageIdx;
        private int currentY;
        private boolean allowWrap = true;

        public Builder(BookProperties properties) {
            this.leftX = properties.leftPageOffset;
            this.rightX = properties.rightPageOffset;
            this.paddingTop = properties.topOffset;
            this.pageHeight = properties.pageHeight;

            this.currentPageIdx = 0;
            this.currentY = this.paddingTop;
        }

        public BookLayout build() {
            return new BookLayout(this.pages, this.chapters);
        }

        public int getCurrentPage() {
            return this.currentPageIdx;
        }

        public void addWidget(Drawable drawable) {
            this.pages.computeIfAbsent(this.currentPageIdx, ArrayList::new).add(drawable);
        }

        public void advancePage() {
            if (this.allowWrap) {
                this.currentPageIdx++;
                this.currentY = this.paddingTop;
            }
        }

        public boolean isNewPage() {
            return this.currentY == this.paddingTop;
        }

        public int getCurrentY() {
            return this.currentY;
        }

        public void setCurrentY(int newY) {
            if (newY < this.getMaxY()) {
                this.currentY = newY;
            } else if (this.allowWrap) {
                this.advancePage();
            }
        }

        public int getCurrentX() {
            return this.currentPageIdx % 2 == 0 ? this.leftX : this.rightX;
        }

        public int getNextX() {
            return this.currentPageIdx % 2 == 0 ? this.rightX : this.leftX;
        }

        public int getMinY() {
            return this.paddingTop;
        }

        public int getMaxY() {
            return this.paddingTop + this.pageHeight;
        }

        public void markBookmark(Bookmark chapter) {
            this.chapters.put(this.currentPageIdx, chapter);
        }

        public int getCurrentBookmark() {
            return this.chapters.size();
        }

        public boolean isWrapAllowed() {
            return this.allowWrap;
        }

        public void allowWrap(boolean allowWrap) {
            this.allowWrap = allowWrap;
        }
    }
}
