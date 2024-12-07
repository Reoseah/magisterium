package io.github.reoseah.magisterium.magisterium.page.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.magisterium.page.BookLayout;
import io.github.reoseah.magisterium.magisterium.page.BookProperties;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;

import java.util.ArrayList;
import java.util.List;

public class Group implements NormalPageElement {
    public static final MapCodec<Group> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            NormalPageElement.CODEC.listOf().fieldOf("elements").forGetter(group -> group.elements) //
    ).apply(instance, Group::new));

    private final List<NormalPageElement> elements;

    public Group(List<NormalPageElement> elements) {
        this.elements = elements;
    }

    @Override
    public MapCodec<? extends PageElement> getCodec() {
        return CODEC;
    }

    @Override
    public void visit(BookLayout.Builder builder, BookProperties properties, TextRenderer textRenderer) {
        var height = this.getHeight(properties.pageWidth, properties.pageHeight, textRenderer);
        if (builder.getMaxY() - builder.getCurrentY() < height && !builder.isNewPage()) {
            builder.advancePage();
        }

        builder.allowWrap(false);
        for (var element : this.elements) {
            element.visit(builder, properties, textRenderer);
        }
        builder.allowWrap(true);
    }

    @Override
    public int getHeight(int width, int pageHeight, TextRenderer textRenderer) {
        var height = 0;
        for (var element : this.elements) {
            if (height != 0) {
                height += element.getTopMargin();
            }
            height += element.getHeight(width, pageHeight, textRenderer);
        }
        return height;
    }

    @Override
    public Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        var dy = 0;
        var drawables = new ArrayList<Drawable>(this.elements.size());
        for (var element : this.elements) {
            if (dy != 0) {
                dy += element.getTopMargin();
            }
            var drawable = element.createWidget(x, y + dy, properties, maxHeight - dy, textRenderer);
            drawables.add(drawable);

            dy += element.getHeight(properties.pageWidth, properties.pageHeight, textRenderer);
        }
        return new GroupDrawable(drawables);
    }

    private static class GroupDrawable implements Drawable, Element {
        private final ArrayList<Drawable> drawables;

        public GroupDrawable(ArrayList<Drawable> drawables) {
            this.drawables = drawables;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            for (var drawable : this.drawables) {
                drawable.render(context, mouseX, mouseY, delta);
            }
        }

        @Override
        public void setFocused(boolean focused) {

        }

        @Override
        public boolean isFocused() {
            return false;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            for (var drawable : this.drawables) {
                if (drawable instanceof Element element //
                        && element.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            for (var drawable : this.drawables) {
                if (drawable instanceof Element element //
                        && element.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }
            return false;
        }
    }
}
