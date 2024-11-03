package io.github.reoseah.magisterium.item;

import io.github.reoseah.magisterium.data.effect.SpellEffect;
import io.github.reoseah.magisterium.data.element.BookElement;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;

import java.util.List;

public class DataDrivenPageItem extends Item {
    public static final Item INSTANCE = new DataDrivenPageItem(new Item.Settings().maxCount(16));

    public static final ComponentType<List<BookElement>> ELEMENTS = ComponentType.<List<BookElement>>builder() //
            .codec(BookElement.CODEC.listOf()) //
            .build();

    public static final ComponentType<List<SpellEffect>> EFFECTS = ComponentType.<List<SpellEffect>>builder()
            .codec(SpellEffect.CODEC.listOf())
            .build();

    public DataDrivenPageItem(Settings settings) {
        super(settings);
    }

    @Override
    protected String getOrCreateTranslationKey() {
        return "item.magisterium.spell_page";
    }
}
