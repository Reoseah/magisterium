package io.github.reoseah.magisterium.item;

import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class SpellPageItem extends Item {
    public static final ComponentType<Identifier> SPELL = ComponentType.<Identifier>builder().codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC).build();

    public static final Item INSTANCE = new SpellPageItem(new Item.Settings().maxCount(16));

    protected SpellPageItem(Settings settings) {
        super(settings);
    }

    public static ItemStack createSpellPage(Identifier spell) {
        ItemStack page = new ItemStack(INSTANCE);

        page.set(SPELL, spell);

        return page;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        Identifier spell = stack.get(SPELL);
        if (spell != null) {
            tooltip.add(Text.translatable("magisterium.spell." + spell.getNamespace() + "." + spell.getPath()).formatted(Formatting.GRAY));
        }
    }
}
