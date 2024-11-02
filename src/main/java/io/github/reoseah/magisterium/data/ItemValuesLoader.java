package io.github.reoseah.magisterium.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class ItemValuesLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    public static final Identifier ID = Identifier.of("magisterium", "item_values");

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();

    // TODO: support item tags (for more flexibility)
    public static Object2IntMap<Identifier> ITEM_VALUES = Object2IntMaps.emptyMap();

    public static int getValue(ItemStack stack) {
        return ITEM_VALUES.getInt(Registries.ITEM.getId(stack.getItem()));
    }

    public ItemValuesLoader() {
        super(GSON, "magisterium/item_values");
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        boolean errors = false;

        var data = new Object2IntOpenHashMap<Identifier>();

        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            try {
                var json = JsonHelper.asObject(entry.getValue(), "item value entry");
                var item = JsonHelper.getString(json, "item");
                var value = JsonHelper.getInt(json, "value");
                data.put(Identifier.of(item), value);
            } catch (Exception e) {
                LOGGER.error("Error loading item value from {}", entry.getKey(), e);
                errors = true;
            }
        }
        if (errors) {
            throw new IllegalStateException("Failed to load item values");
        }

        ITEM_VALUES = Object2IntMaps.unmodifiable(data);
        LOGGER.debug("Loaded {} item values", ITEM_VALUES.size());
    }
}
