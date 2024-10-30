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
    public static final Item UNSTABLE_CHARGE = new SpellPageItem(new Item.Settings().maxCount(16), Identifier.of("magisterium:unstable_charge"));
    public static final Item COLD_SNAP = new SpellPageItem(new Item.Settings().maxCount(16), Identifier.of("magisterium:cold_snap"));

    public final Identifier spell;
    protected final Text tooltip;

    protected SpellPageItem(Settings settings, Identifier spell) {
        super(settings);
        this.spell = spell;
        this.tooltip = Text.translatable("magisterium.spell." + spell.getNamespace() + "." + spell.getPath()).formatted(Formatting.GRAY);
    }

    @Override
    protected String getOrCreateTranslationKey() {
        return "item.magisterium.spell_page";
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        tooltip.add(this.tooltip);
    }
}
