package io.github.reoseah.magisterium.data.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.network.c2s.StartSpellPayload;
import io.github.reoseah.magisterium.network.c2s.StopSpellPayload;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

import java.util.List;

// TODO: have the utterance text shortly highlight to indicate finishing
public class Spell implements NormalPageElement {
    public static final MapCodec<Spell> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            TextCodecs.CODEC.fieldOf("text").forGetter(spell -> spell.text), //
            Identifier.CODEC.fieldOf("effect").forGetter(spell -> spell.effect), //
            Codec.FLOAT.fieldOf("duration").forGetter(spell -> spell.duration) //
    ).apply(instance, Spell::new));

    protected final Text text;
    protected final Identifier effect;
    protected final float duration;

    public Spell(Text text, Identifier effect, float duration) {
        this.text = text;
        this.effect = effect;
        this.duration = duration;
    }

    @Override
    public MapCodec<? extends PageElement> getCodec() {
        return CODEC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getHeight(int width, int pageHeight, TextRenderer textRenderer) {
        var lines = textRenderer.wrapLines(this.text, width - 12);
        var linesAsString = lines.stream().map(t -> {
            StringBuilder builder = new StringBuilder();
            t.accept((index, style, codePoint) -> {
                builder.appendCodePoint(codePoint);
                return true;
            });
            return builder.toString();
        }).toList();

        int height = 0;
        for (String line : linesAsString) {
            if (line.isEmpty()) {
                height += 4;
            } else {
                height += textRenderer.fontHeight;
            }
        }
        return height;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getTopMargin() {
        return NormalPageElement.super.getTopMargin() + 2;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        return new SpellWidget(this, this.text, x, y, properties, properties.pageWidth, textRenderer);
    }

    @Environment(EnvType.CLIENT)
    public static class SpellWidget implements Drawable, Element {
        private final Spell spell;

        private final int buttonX;
        private final int buttonY;

        private final BookProperties properties;
        private final TextRenderer textRenderer;
        private final List<OrderedText> lines;
        private final List<String> linesAsString;
        private final IntList linesY;
        private final int textLength;
        private final int x;

        private boolean mouseDown = false;
        private long mouseDownTime = 0L;

        public SpellWidget(Spell spell, Text text, int x, int y, BookProperties properties, int width, TextRenderer textRenderer) {
            this.spell = spell;
            this.properties = properties;
            this.textRenderer = textRenderer;

            this.lines = textRenderer.wrapLines(text, width - properties.spellButtonWidth);
            this.linesAsString = lines.stream().map(t -> {
                StringBuilder builder = new StringBuilder();
                t.accept((index, style, codePoint) -> {
                    builder.appendCodePoint(codePoint);
                    return true;
                });
                return builder.toString();
            }).toList();

            int nonWrappedLength = text.getString().length();
            this.textLength = nonWrappedLength - lines.size() + 1;

            this.linesY = new IntArrayList(linesAsString.size());
            int nextY = y;
            for (String line : linesAsString) {
                this.linesY.add(nextY);
                if (line.isEmpty()) {
                    nextY += 4;
                } else {
                    nextY += textRenderer.fontHeight;
                }
            }

            this.buttonX = x - 2;
            this.buttonY = y - 2;
            this.x = x;
        }

        public void finish() {
            this.mouseDown = false;
            this.mouseDownTime = 0;
        }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
            if (this.mouseDown || mouseX > buttonX && mouseY > buttonY
                    && mouseX < buttonX + properties.spellButtonWidth && mouseY < buttonY + properties.spellButtonHeight) {
                ctx.drawTexture(RenderLayer::getGuiTextured, properties.texture, buttonX, buttonY, properties.spellButtonU, properties.spellButtonActiveV, properties.spellButtonWidth, properties.spellButtonHeight, 256, 256);
            } else {
                ctx.drawTexture(RenderLayer::getGuiTextured, properties.texture, buttonX, buttonY, properties.spellButtonU, properties.spellButtonV, properties.spellButtonWidth, properties.spellButtonHeight, 256, 256);
            }

            float readTime = this.mouseDown ? (System.currentTimeMillis() - this.mouseDownTime) / 1000F : 0;
            float ratio = (readTime / spell.duration);

            int coloredCharacters = Math.round(this.textLength * ratio);

            for (int i = 0; i < lines.size(); i++) {
                ctx.drawText(textRenderer, lines.get(i), x + properties.spellButtonWidth, linesY.getInt(i), 0x000000, false);
                if (coloredCharacters > 0) {
                    int color = ratio > 1 ? 0xdd4c1e : 0xce1e00;
                    var substring = linesAsString.get(i).substring(0, Math.min(coloredCharacters, linesAsString.get(i).length()));
                    var literal = Text.literal(substring).styled(style -> style.withFont(Identifier.of("magisterium", "small_caps")));
                    ctx.drawText(textRenderer, literal, x + properties.spellButtonWidth, linesY.getInt(i), color, false);
                    coloredCharacters -= linesAsString.get(i).length();
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX > buttonX && mouseY > buttonY
                    && mouseX < buttonX + properties.spellButtonWidth && mouseY < buttonY + properties.spellButtonHeight) {
                this.mouseDown = true;
                this.mouseDownTime = System.currentTimeMillis();

                ClientPlayNetworking.send(new StartSpellPayload(spell.effect));
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (this.mouseDown) {
                this.mouseDown = false;
                this.mouseDownTime = 0;

                ClientPlayNetworking.send(StopSpellPayload.INSTANCE);
                return true;
            }
            return false;
        }

        @Override
        public void setFocused(boolean focused) {
            // we can't provide reasonable focus behavior
            // so no accessibility features for you
        }

        @Override
        public boolean isFocused() {
            return false;
        }
    }
}
