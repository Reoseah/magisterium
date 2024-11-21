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
    public static final Item AWAKEN_THE_FLAME = createSpellPage("awaken_the_flame");
    public static final Item QUENCH_THE_FLAME = createSpellPage("quench_the_flame");
    public static final Item GLYPHIC_IGNITION = createSpellPage("glyphic_ignition");
    public static final Item CONFLAGRATE = createSpellPage("conflagrate");
    public static final Item ILLUSORY_WALL = createSpellPage("illusory_wall");
    public static final Item COLD_SNAP = createSpellPage("cold_snap");
    public static final Item ARCANE_LIFT = createSpellPage("arcane_lift");
    public static final Item MAGIC_BARRIER = createSpellPage("magic_barrier");
    public static final Item DISPEL_MAGIC = createSpellPage("dispel_magic");

    public static final Item ELEMENTS_OF_PYROMANCY = createPageStack("elements_of_pyromancy");
    public static final Item LESSER_ARCANUM = createPageStack("lesser_arcanum");

    private static Item createSpellPage(String name) {
        var id = Identifier.of("magisterium", name + "_page");
        var pageId = Identifier.of("magisterium", name);
        var registryKey = RegistryKey.of(RegistryKeys.ITEM, id);
        var settings = new Settings() //
                .registryKey(registryKey) //
                .maxCount(16) //
                .translationKey("item.magisterium.spell_page") //
                .component(PAGE_ID, pageId);
        return new PageItem(settings);
    }

    private static Item createPageStack(String name) {
        var id = Identifier.of("magisterium", name + "_pages");
        var pageId = Identifier.of("magisterium", name);
        var registryKey = RegistryKey.of(RegistryKeys.ITEM, id);
        var settings = new Settings() //
                .registryKey(registryKey) //
                .maxCount(1) //
                .translationKey("item.magisterium.stack_of_pages") //
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
