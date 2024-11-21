package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;

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
        return (context, mouseX, mouseY, delta) -> {
            for (var drawable : drawables) {
                drawable.render(context, mouseX, mouseY, delta);
            }
        };
    }
}
