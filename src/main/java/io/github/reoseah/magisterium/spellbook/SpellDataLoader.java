package io.github.reoseah.magisterium.spellbook;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import io.github.reoseah.magisterium.spellbook.element.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.recipe.Ingredient;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;

public class SpellDataLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogManager.getLogger();

    public static ImmutableMap<Identifier, SpellData> SPELLS = ImmutableMap.of();

    public SpellDataLoader() {
        super(GSON, "magisterium/spells");
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("magisterium", "spells");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        boolean errors = false;
        var builder = ImmutableMap.<Identifier, SpellData>builder();

        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            try {
                var json = JsonHelper.asObject(entry.getValue(), "spell_data");
                var elements = new ArrayList<BookElement>();
                try {
                    var elementsJson = JsonHelper.getArray(json, "elements");

                    for (int i = 0; i < elementsJson.size(); i++) {
                        var element = readElement(JsonHelper.asObject(elementsJson.get(i), "element"));
                        elements.add(element);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error loading spell elements from {}", entry.getKey(), e);
                    errors = true;
                }

                var spell = new SpellData(elements.toArray(new BookElement[0]));
                builder.put(entry.getKey(), spell);
            } catch (Exception e) {
                LOGGER.error("Error loading spell data from {}", entry.getKey(), e);
                errors = true;
            }
        }

        if (!errors) {
            SPELLS = builder.build();
            LOGGER.info("Loaded {} spell data", SPELLS.size());
        } else {
            throw new IllegalStateException("Failed to load spell data");
        }
    }

    private static BookElement readElement(JsonObject json) {
        String type = JsonHelper.getString(json, "type");
        return switch (type) {
            case "heading" -> new Heading(JsonHelper.getString(json, "translation_key"));
            case "paragraph" -> new Paragraph(JsonHelper.getString(json, "translation_key"));
            case "page_break" -> new PageBreak();
            case "chapter" -> new Chapter(JsonHelper.getString(json, "translation_key"));
            case "fold" -> {
                JsonArray leftJson = JsonHelper.getArray(json, "left", new JsonArray());
                JsonArray rightJson = JsonHelper.getArray(json, "right", new JsonArray());
                BookSimpleElement[] left = readSimpleElements(leftJson);
                BookSimpleElement[] right = readSimpleElements(rightJson);
                yield new Fold(left, right);
            }
            case "vertically_centered" -> {
                var element = readElement(JsonHelper.getObject(json, "element"));
                if (element instanceof BookSimpleElement simple) {
                    yield new VerticalCenterElement(simple);
                } else {
                    throw new JsonParseException("Cannot use special element here: " + element);
                }
            }
            case "utterance" -> {
                String translationKey = JsonHelper.getString(json, "translation_key");
                Identifier id = Identifier.of(JsonHelper.getString(json, "id"));
                int duration = JsonHelper.getInt(json, "duration");
                yield new Utterance(translationKey, id, duration);
            }
            case "inventory" -> {
                JsonArray slotsJson = JsonHelper.getArray(json, "slots");

                if (slotsJson.size() >= 16) {
                    throw new JsonParseException("Too many slots for inventory element");
                }

                SlotConfiguration[] slots = new SlotConfiguration[slotsJson.size()];
                for (int j = 0; j < slotsJson.size(); j++) {
                    JsonObject slot = JsonHelper.asObject(slotsJson.get(j), "slot");
                    int x = JsonHelper.getInt(slot, "x");
                    int y = JsonHelper.getInt(slot, "y");
                    Identifier background = slot.has("background") ? Identifier.of(JsonHelper.getString(slot, "background")) : null;
                    boolean output = slot.has("output") && JsonHelper.getBoolean(slot, "output");
                    Ingredient ingredient = slot.has("ingredient") ? Ingredient.ALLOW_EMPTY_CODEC.parse(JsonOps.INSTANCE, slot.get("ingredient")).getOrThrow() : null;

                    slots[j] = new SlotConfiguration(x, y, output, ingredient, background);
                }

                BookInventoryElement.Image background = null;
                if (json.has("background")) {
                    JsonObject backgroundJson = JsonHelper.asObject(json.get("background"), "background");
                    Identifier texture = Identifier.of(JsonHelper.getString(backgroundJson, "texture"));
                    int x = JsonHelper.getInt(backgroundJson, "x");
                    int y = JsonHelper.getInt(backgroundJson, "y");
                    int u = JsonHelper.getInt(backgroundJson, "u");
                    int v = JsonHelper.getInt(backgroundJson, "v");
                    int width = JsonHelper.getInt(backgroundJson, "width");
                    int height = JsonHelper.getInt(backgroundJson, "height");
                    background = new BookInventoryElement.Image(texture, x, y, u, v, width, height);
                }
                int height = JsonHelper.getInt(json, "height", 0);

                yield new BookInventoryElement(height, background, slots);
            }
//            case "illustration" -> {
//                Identifier texture = new Identifier(JsonHelper.getString(json, "texture"));
//                int u = JsonHelper.getInt(json, "u");
//                int v = JsonHelper.getInt(json, "v");
//                int width = JsonHelper.getInt(json, "width");
//                int height = JsonHelper.getInt(json, "height");
//                yield new Illustration(texture, u, v, width, height);
//            }
            default -> throw new JsonParseException("Unknown element type: " + type);
        };
    }

    private static BookSimpleElement[] readSimpleElements(JsonArray elementsJson) {
        BookSimpleElement[] elements = new BookSimpleElement[elementsJson.size()];
        for (int i = 0; i < elementsJson.size(); i++) {
            var element = readElement(JsonHelper.asObject(elementsJson.get(i), "element"));
            if (element instanceof BookSimpleElement simple) {
                elements[i] = simple;
            } else {
                throw new JsonParseException("Cannot use special element here: " + element);
            }
        }
        return elements;
    }
}
