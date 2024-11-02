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
        if (errors) {
            throw new IllegalStateException("Failed to load spell data");
        }

        SPELLS = builder.build();
        LOGGER.debug("Loaded {} spell data", SPELLS.size());
    }

    private static BookElement readElement(JsonObject json) {
        var type = JsonHelper.getString(json, "type");
        return switch (type) {
            case "heading" -> new Heading(JsonHelper.getString(json, "translation_key"));
            case "paragraph" -> new Paragraph(JsonHelper.getString(json, "translation_key"));
            case "page_break" -> new PageBreak();
            case "chapter" -> new BookmarkElement(JsonHelper.getString(json, "translation_key"));
            case "fold" -> {
                var left = readSimpleElements(JsonHelper.getArray(json, "left", new JsonArray()));
                var right = readSimpleElements(JsonHelper.getArray(json, "right", new JsonArray()));
                yield new Fold(left, right);
            }
            case "vertically_centered" -> {
                var element = readElement(JsonHelper.getObject(json, "element"));
                if (element instanceof SimpleBlock simple) {
                    yield new VerticalCenterElement(simple);
                } else {
                    throw new JsonParseException("Cannot use special element here: " + element);
                }
            }
            case "utterance" -> {
                var translationKey = JsonHelper.getString(json, "translation_key");
                var id = Identifier.of(JsonHelper.getString(json, "id"));
                int duration = JsonHelper.getInt(json, "duration");
                yield new Utterance(translationKey, id, duration);
            }
            case "inventory" -> {
                var slotsJson = JsonHelper.getArray(json, "slots");

                if (slotsJson.size() >= 16) {
                    throw new JsonParseException("Too many slots for inventory element");
                }

                var slots = new SlotProperties[slotsJson.size()];
                for (int j = 0; j < slotsJson.size(); j++) {
                    var slot = JsonHelper.asObject(slotsJson.get(j), "slot");
                    int x = JsonHelper.getInt(slot, "x");
                    int y = JsonHelper.getInt(slot, "y");
                    var background = slot.has("background") ? Identifier.of(JsonHelper.getString(slot, "background")) : null;
                    boolean output = slot.has("output") && JsonHelper.getBoolean(slot, "output");
                    var ingredient = slot.has("ingredient") ? Ingredient.ALLOW_EMPTY_CODEC.parse(JsonOps.INSTANCE, slot.get("ingredient")).getOrThrow() : null;

                    slots[j] = new SlotProperties(x, y, output, ingredient, background);
                }

                BookInventory.Background background = null;
                if (json.has("background")) {
                    var backgroundJson = JsonHelper.asObject(json.get("background"), "background");
                    var texture = Identifier.of(JsonHelper.getString(backgroundJson, "texture"));
                    int x = JsonHelper.getInt(backgroundJson, "x");
                    int y = JsonHelper.getInt(backgroundJson, "y");
                    int u = JsonHelper.getInt(backgroundJson, "u");
                    int v = JsonHelper.getInt(backgroundJson, "v");
                    int width = JsonHelper.getInt(backgroundJson, "width");
                    int height = JsonHelper.getInt(backgroundJson, "height");
                    background = new BookInventory.Background(texture, x, y, u, v, width, height);
                }
                int height = JsonHelper.getInt(json, "height", 0);

                yield new BookInventory(height, background, slots);
            }
            default -> throw new JsonParseException("Unknown element type: " + type);
        };
    }

    public static SimpleBlock[] readSimpleElements(JsonArray elementsJson) {
        var elements = new SimpleBlock[elementsJson.size()];
        for (int i = 0; i < elementsJson.size(); i++) {
            var element = readElement(JsonHelper.asObject(elementsJson.get(i), "element"));
            if (element instanceof SimpleBlock simple) {
                elements[i] = simple;
            } else {
                throw new JsonParseException("Cannot use special element here: " + element);
            }
        }
        return elements;
    }
}
