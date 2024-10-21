package io.github.reoseah.magisterium.item;

import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class SpellPageItem extends Item {
    public static final ComponentType<Identifier> SPELL = ComponentType.<Identifier>builder().codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC).build();

    public static final Item INSTANCE = new SpellBookItem(new Item.Settings().maxCount(1).component(SPELL, Identifier.of("magisterium:test_spell")));

    public SpellPageItem(Settings settings) {
        super(settings);
    }
}
