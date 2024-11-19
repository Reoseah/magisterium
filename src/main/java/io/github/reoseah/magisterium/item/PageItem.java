package io.github.reoseah.magisterium.item;

import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class PageItem extends Item {
    public static final ComponentType<Identifier> PAGE_ID = ComponentType.<Identifier>builder() //
            .codec(Identifier.CODEC) //
            .packetCodec(Identifier.PACKET_CODEC) //
            .build();

    // TODO: on 1.21.4, have just single item and change texture through the new item model overrides?
    public static final Item AWAKEN_THE_FLAME = create("awaken_the_flame");
    public static final Item QUENCH_THE_FLAME = create("quench_the_flame");
    public static final Item GLYPHIC_IGNITION = create("glyphic_ignition");
    public static final Item CONFLAGRATE = create("conflagrate");
    public static final Item ILLUSORY_WALL = create("illusory_wall");
    public static final Item COLD_SNAP = create("cold_snap");
    public static final Item ARCANE_LIFT = create("arcane_lift");
    public static final Item MAGIC_BARRIER = create("magic_barrier");
    public static final Item DISPEL_MAGIC = create("dispel_magic");

    private static Item create(String spell) {
        var id = Identifier.of("magisterium", spell + "_page");
        var pageId = Identifier.of("magisterium", spell);
        var registryKey = RegistryKey.of(RegistryKeys.ITEM, id);
        var settings = new Settings() //
                .registryKey(registryKey) //
                .maxCount(16) //
                .translationKey("item.magisterium.spell_page") //
                .component(PAGE_ID, pageId);
        return new PageItem(settings);
    }

    protected PageItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        var spell = stack.get(PAGE_ID);
        if (spell != null) {
            tooltip.add(Text.translatable("magisterium.page." + spell.getNamespace() + "." + spell.getPath()).formatted(Formatting.GRAY));
        }
    }
}
