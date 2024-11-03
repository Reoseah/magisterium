package io.github.reoseah.magisterium.data.element;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.gui.Drawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public record BookLayout(
        Int2ObjectMap<List<Drawable>> pages,
        Int2ObjectMap<Bookmark> bookmarks
) {
    public static final BookLayout EMPTY = new BookLayout(Int2ObjectMaps.emptyMap(), Int2ObjectMaps.emptyMap());

    public BookLayout(Int2ObjectMap<List<Drawable>> pages, Int2ObjectMap<Bookmark> bookmarks) {
        this.pages = Int2ObjectMaps.unmodifiable(pages);
        this.bookmarks = Int2ObjectMaps.unmodifiable(bookmarks);
    }

    public int getPageCount() {
        return this.pages.keySet().intStream().max().orElse(1);
    }

    public List<Drawable> getPage(int page) {
        return this.pages.getOrDefault(page, Collections.emptyList());
    }

    public SlotProperties[] getFoldSlots(int leftPage) {
        // TODO build and keep a separate map of slots per page
        Stream<SlotProperties> leftSlots = this.getPage(leftPage).stream()
                .filter(drawable -> drawable instanceof SlotPropertiesProvider)
                .flatMap(drawable -> ((SlotPropertiesProvider) drawable).getSlotProperties().stream());
        Stream<SlotProperties> rightSlots = this.getPage(leftPage + 1).stream()
                .filter(drawable -> drawable instanceof SlotPropertiesProvider)
                .flatMap(drawable -> ((SlotPropertiesProvider) drawable).getSlotProperties().stream());

        return Stream.concat(leftSlots, rightSlots).limit(16).toArray(SlotProperties[]::new);
    }

    public static class Builder {
        private final int leftX, rightX;
        private final int paddingTop;
        private final int pageHeight;

        private final Int2ObjectMap<List<Drawable>> pages = new Int2ObjectArrayMap<>();
        private final Int2ObjectMap<Bookmark> bookmarks = new Int2ObjectArrayMap<>();

        private int currentPageIdx;
        private int currentY;
        private boolean allowWrap = true;

        public Builder(BookProperties properties) {
            this.leftX = properties.pageLeftX;
            this.rightX = properties.pageRightX;
            this.paddingTop = properties.pageY;
            this.pageHeight = properties.pageHeight;

            this.currentPageIdx = 0;
            this.currentY = this.paddingTop;
        }

        public BookLayout build() {
            return new BookLayout(this.pages, this.bookmarks);
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

        public void startNewFold() {
            if (this.getCurrentPage() % 2 != 0) {
                this.advancePage();
            } else if (!this.isNewPage()) {
                this.advancePage();
                this.advancePage();
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
            this.bookmarks.put(this.currentPageIdx, chapter);
        }

        public int getCurrentBookmark() {
            return this.bookmarks.size();
        }

        public boolean isWrapAllowed() {
            return this.allowWrap;
        }

        public void allowWrap(boolean allowWrap) {
            this.allowWrap = allowWrap;
        }
    }
}
