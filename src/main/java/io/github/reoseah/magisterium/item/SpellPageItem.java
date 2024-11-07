package io.github.reoseah.magisterium.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class SpellPageItem extends Item {
    public static final Identifier AWAKEN_THE_FLAME_ID = Identifier.of("magisterium:awaken_the_flame_page");
    public static final RegistryKey<Item> AWAKEN_THE_FLAME_KEY = RegistryKey.of(RegistryKeys.ITEM, AWAKEN_THE_FLAME_ID);
    public static final Item AWAKEN_THE_FLAME = new SpellPageItem(createSettings("awaken_the_flame_page").registryKey(AWAKEN_THE_FLAME_KEY), Identifier.of("magisterium:awaken_the_flame"));

    public static final Identifier QUENCH_THE_FLAME_ID = Identifier.of("magisterium:quench_the_flame_page");
    public static final RegistryKey<Item> QUENCH_THE_FLAME_KEY = RegistryKey.of(RegistryKeys.ITEM, QUENCH_THE_FLAME_ID);
    public static final Item QUENCH_THE_FLAME = new SpellPageItem(createSettings("quench_the_flame_page").registryKey(QUENCH_THE_FLAME_KEY), Identifier.of("magisterium:quench_the_flame"));

    public static final Identifier GLYPHIC_IGNITION_ID = Identifier.of("magisterium:glyphic_ignition_page");
    public static final RegistryKey<Item> GLYPHIC_IGNITION_KEY = RegistryKey.of(RegistryKeys.ITEM, GLYPHIC_IGNITION_ID);
    public static final Item GLYPHIC_IGNITION = new SpellPageItem(createSettings("glyphic_ignition_page").registryKey(GLYPHIC_IGNITION_KEY), Identifier.of("magisterium:glyphic_ignition"));

    public static final Identifier CONFLAGRATE_ID = Identifier.of("magisterium:conflagrate_page");
    public static final RegistryKey<Item> CONFLAGRATE_KEY = RegistryKey.of(RegistryKeys.ITEM, CONFLAGRATE_ID);
    public static final Item CONFLAGRATE = new SpellPageItem(createSettings("conflagrate_page").registryKey(CONFLAGRATE_KEY), Identifier.of("magisterium:conflagrate"));

    public static final Identifier ILLUSORY_WALL_ID = Identifier.of("magisterium:illusory_wall_page");
    public static final RegistryKey<Item> ILLUSORY_WALL_KEY = RegistryKey.of(RegistryKeys.ITEM, ILLUSORY_WALL_ID);
    public static final Item ILLUSORY_WALL = new SpellPageItem(createSettings("illusory_wall_page").registryKey(ILLUSORY_WALL_KEY), Identifier.of("magisterium:illusory_wall"));

    public static final Identifier COLD_SNAP_ID = Identifier.of("magisterium:cold_snap_page");
    public static final RegistryKey<Item> COLD_SNAP_KEY = RegistryKey.of(RegistryKeys.ITEM, COLD_SNAP_ID);
    public static final Item COLD_SNAP = new SpellPageItem(createSettings("cold_snap_page").registryKey(COLD_SNAP_KEY), Identifier.of("magisterium:cold_snap"));

    public static final Identifier ARCANE_LIFT_ID = Identifier.of("magisterium:arcane_lift_page");
    public static final RegistryKey<Item> ARCANE_LIFT_KEY = RegistryKey.of(RegistryKeys.ITEM, ARCANE_LIFT_ID);
    public static final Item ARCANE_LIFT = new SpellPageItem(createSettings("arcane_lift_page").registryKey(ARCANE_LIFT_KEY), Identifier.of("magisterium:arcane_lift"));

    public static final Identifier DISPEL_MAGIC_ID = Identifier.of("magisterium:dispel_magic_page");
    public static final RegistryKey<Item> DISPEL_MAGIC_KEY = RegistryKey.of(RegistryKeys.ITEM, DISPEL_MAGIC_ID);
    public static final Item DISPEL_MAGIC = new SpellPageItem(createSettings("dispel_magic_page").registryKey(DISPEL_MAGIC_KEY), Identifier.of("magisterium:dispel_magic"));

    private static Item.Settings createSettings(String name) {
        return new Item.Settings().maxCount(16) //
                .component(DataComponentTypes.ITEM_NAME, Text.translatable("item.magisterium." + name));
    }

    public final Identifier spell;
    protected final Text tooltip;

    protected SpellPageItem(net.minecraft.item.Item.Settings settings, Identifier spell) {
        super(settings);
        this.spell = spell;
        this.tooltip = Text.translatable("magisterium.spell." + spell.getNamespace() + "." + spell.getPath()).formatted(Formatting.GRAY);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        tooltip.add(this.tooltip);
    }
}
