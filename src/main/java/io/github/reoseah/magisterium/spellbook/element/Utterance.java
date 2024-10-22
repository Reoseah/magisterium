package io.github.reoseah.magisterium.spellbook.element;


import io.github.reoseah.magisterium.network.StartUtterancePayload;
import io.github.reoseah.magisterium.network.StopUtterancePayload;
import io.github.reoseah.magisterium.spellbook.BookProperties;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

// TODO: have the utterance text shortly highlight to indicate finishing
// TODO: consider renaming this and other Utterance* classes to Spell or SpellElement
public class Utterance extends SimpleBlock {
    protected final String translationKey;
    protected final Identifier id;
    protected final float duration;

    public Utterance(String translationKey, Identifier id, float duration) {
        this.translationKey = translationKey;
        this.id = id;
        this.duration = duration;
    }

    @Override
    protected int getHeight(int width, TextRenderer textRenderer) {
        MutableText text = Text.translatable(this.translationKey);
        List<OrderedText> lines = textRenderer.wrapLines(text, width - 12);
        List<String> linesAsString = lines.stream().map(t -> {
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
    protected int getTopMargin() {
        return super.getTopMargin() + 2;
    }

    @Override
    protected Drawable createWidget(int x, int y, BookProperties properties, int maxHeight, TextRenderer textRenderer) {
        return new UtteranceWidget(this.translationKey, x, y, properties, properties.pageWidth, textRenderer);
    }

    private class UtteranceWidget implements Drawable, Element {
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

        public UtteranceWidget(String translationKey, int x, int y, BookProperties properties, int width, TextRenderer textRenderer) {
            this.properties = properties;
            this.textRenderer = textRenderer;

            MutableText text = Text.translatable(translationKey);
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

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
            if (this.mouseDown || mouseX > buttonX && mouseY > buttonY
                    && mouseX < buttonX + properties.spellButtonWidth && mouseY < buttonY + properties.spellButtonHeight) {
                ctx.drawTexture(properties.texture, buttonX, buttonY, properties.spellButtonU, properties.spellButtonActiveV, properties.spellButtonWidth, properties.spellButtonHeight);
            } else {
                ctx.drawTexture(properties.texture, buttonX, buttonY, properties.spellButtonU, properties.spellButtonV, properties.spellButtonWidth, properties.spellButtonHeight);
            }

            float readTime = this.mouseDown ? (System.currentTimeMillis() - this.mouseDownTime) / 1000F : 0;
            float ratio = (readTime / Utterance.this.duration);

            if (ratio > 1) {
                // TODO: send a packet from the server when utterance is finished
                //       probably reusing utterance fields on screen handler on client for this
                //       which currently are only used by the server
                this.mouseDown = false;
                this.mouseDownTime = 0;
            }

            int coloredCharacters = Math.round(this.textLength * ratio);

            for (int i = 0; i < lines.size(); i++) {
                ctx.drawText(textRenderer, lines.get(i), x + properties.spellButtonWidth, linesY.getInt(i), 0x000000, false);
                if (coloredCharacters > 0) {
                    ctx.drawText(textRenderer, linesAsString.get(i).substring(0, Math.min(coloredCharacters, linesAsString.get(i).length())), x + properties.spellButtonWidth, linesY.getInt(i), 0xce1e00, false);
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

                ClientPlayNetworking.send(new StartUtterancePayload(id));
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (this.mouseDown) {
                this.mouseDown = false;
                this.mouseDownTime = 0;

                ClientPlayNetworking.send(new StopUtterancePayload());
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
