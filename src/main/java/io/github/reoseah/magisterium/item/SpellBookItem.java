package io.github.reoseah.magisterium.item;

import io.github.reoseah.magisterium.data.BookLoader;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Function;

public class SpellBookItem extends Item {
    public static final ComponentType<Identifier> BOOK_PROPERTIES = ComponentType.<Identifier>builder() //
            .codec(Identifier.CODEC) //
            .packetCodec(Identifier.PACKET_CODEC) //
            .build();
    public static final ComponentType<List<ItemStack>> CONTENTS = ComponentType.<List<ItemStack>>builder() //
            .codec(ItemStack.OPTIONAL_CODEC.listOf()) //
            .packetCodec(ItemStack.OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toList())) //
            .build();
    public static final ComponentType<Integer> CURRENT_PAGE = ComponentType.<Integer>builder() //
            .codec(Codecs.NON_NEGATIVE_INT) //
            .packetCodec(PacketCodecs.VAR_INT) //
            .build();

    public static final Item SPELL_BOOK = create("spell_book", settings -> new SpellBookItem(settings.rarity(Rarity.RARE).modelId(Identifier.of("magisterium", "spell_book_in_hand"))));
    public static final Item ELEMENTS_OF_PYROMANCY = create("elements_of_pyromancy", settings -> {
        var pages = DefaultedList.ofSize(18, ItemStack.EMPTY);
        pages.set(0, PageItem.ELEMENTS_OF_PYROMANCY.getDefaultStack());
        pages.set(1, PageItem.ENCHANTED_CANDLESTICK.getDefaultStack());
//        pages.set(2, PageItem.QUENCH_THE_FLAME.getDefaultStack());
        pages.set(2, PageItem.GLYPHIC_IGNITION.getDefaultStack());
        pages.set(3, PageItem.CONFLAGRATE.getDefaultStack());

        return new SpellBookItem(settings.rarity(Rarity.UNCOMMON).component(CONTENTS, pages));
    });
    public static final Item LESSER_ARCANUM = create("lesser_arcanum", settings -> {
        var pages = DefaultedList.ofSize(18, ItemStack.EMPTY);
        pages.set(0, PageItem.LESSER_ARCANUM.getDefaultStack());
        pages.set(1, PageItem.MAGIC_BARRIER.getDefaultStack());
        pages.set(2, PageItem.ARCANE_LIFT.getDefaultStack());
        pages.set(3, PageItem.ILLUSORY_WALL.getDefaultStack());
        pages.set(4, PageItem.DISPEL_MAGIC.getDefaultStack());

        return new SpellBookItem(settings.rarity(Rarity.UNCOMMON).component(CONTENTS, pages));
    });

    public static Item create(String name, Function<Settings, Item> constructor) {
        var id = Identifier.of("magisterium", name);
        var registryKey = RegistryKey.of(RegistryKeys.ITEM, id);
        var settings = new Item.Settings() //
                .registryKey(registryKey) //
                .useItemPrefixedTranslationKey() //
                .maxCount(1) //
                .component(BOOK_PROPERTIES, id) //
                .component(CONTENTS, DefaultedList.ofSize(18, ItemStack.EMPTY)) //
                .component(CURRENT_PAGE, 0);
        return constructor.apply(settings);
    }

    public SpellBookItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        var book = player.getStackInHand(hand);

        if (!book.contains(CONTENTS)) {
            return ActionResult.FAIL;
        }

        if (!world.isClient) {
            player.openHandledScreen(new NamedScreenHandlerFactory() {
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new SpellBookScreenHandler(syncId, inv, new SpellBookScreenHandler.HandContext(hand, book));
                }

                @Override
                public Text getDisplayName() {
                    return book.getName();
                }
            });
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        var bookId = stack.get(BOOK_PROPERTIES);
        var bookData = BookLoader.getInstance().books.get(bookId);
        if (bookData != null && bookData.supportInsertion) {
            var pages = stack.get(CONTENTS);
            if (pages != null) {
                var nonEmptyCount = pages.stream().filter(page -> !page.isEmpty()).count();
                if (nonEmptyCount > 0) {
                    tooltip.add(Text.translatable("item.magisterium.spell_book.pages", nonEmptyCount).formatted(Formatting.GRAY));
                } else {
                    tooltip.add(Text.translatable("item.magisterium.spell_book.empty").formatted(Formatting.GRAY));
                }
            } else {
                tooltip.add(Text.translatable("item.magisterium.spell_book.empty").formatted(Formatting.GRAY));
            }
        }
    }
}
