package io.github.reoseah.magisterium.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class SpellPageItem extends Item {
    public static final Item AWAKEN_THE_FLAME = new SpellPageItem(new Item.Settings().maxCount(16), Identifier.of("magisterium:awaken_the_flame"));
    public static final Item QUENCH_THE_FLAME = new SpellPageItem(new Item.Settings().maxCount(16), Identifier.of("magisterium:quench_the_flame"));
    public static final Item GLYPHIC_IGNITION = new SpellPageItem(new Item.Settings().maxCount(16), Identifier.of("magisterium:glyphic_ignition"));
    public static final Item CONFLAGRATE = new SpellPageItem(new Item.Settings().maxCount(16), Identifier.of("magisterium:conflagrate"));
    public static final Item ILLUSORY_WALL = new SpellPageItem(new Item.Settings().maxCount(16), Identifier.of("magisterium:illusory_wall"));
    public static final Item COLD_SNAP = new SpellPageItem(new Item.Settings().maxCount(16), Identifier.of("magisterium:cold_snap"));
    public static final Item ARCANE_LIFT = new SpellPageItem(new Item.Settings().maxCount(16), Identifier.of("magisterium:arcane_lift"));
    public static final Item DISPEL_MAGIC = new SpellPageItem(new Item.Settings().maxCount(16), Identifier.of("magisterium:dispel_magic"));

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
