package io.github.reoseah.magisterium.item;

import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.List;

public class SpellBookItem extends Item {
    public static final ComponentType<Integer> CURRENT_PAGE = ComponentType.<Integer>builder() //
            .codec(Codecs.NONNEGATIVE_INT) //
            .packetCodec(PacketCodecs.VAR_INT) //
            .build();
    public static final ComponentType<List<ItemStack>> PAGES = ComponentType.<List<ItemStack>>builder() //
            .codec(ItemStack.OPTIONAL_CODEC.listOf()) //
            .packetCodec(ItemStack.OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toList())) //
            .build();

    public static final Item INSTANCE = new SpellBookItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE).component(CURRENT_PAGE, 0));

    protected SpellBookItem(Settings settings) {
        super(settings);
    }

    public static ItemStack createTestBook() {
        ItemStack book = new ItemStack(INSTANCE);

        book.set(PAGES, Util.make(DefaultedList.ofSize(18, ItemStack.EMPTY), list -> {
            list.set(0, BookmarkItem.INSTANCE.getDefaultStack());
            list.set(1, SpellPageItem.createSpellPage(Identifier.of("magisterium:awaken_the_flame")));
            list.set(2, SpellPageItem.createSpellPage(Identifier.of("magisterium:quench_the_flame")));
            list.set(3, SpellPageItem.createSpellPage(Identifier.of("magisterium:glyphic_ignition")));
            list.set(4, SpellPageItem.createSpellPage(Identifier.of("magisterium:conflagrate")));
            list.set(5, SpellPageItem.createSpellPage(Identifier.of("magisterium:illusory_wall")));
        }));

        return book;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack book = player.getStackInHand(hand);

        if (!book.contains(PAGES)) {
            return TypedActionResult.fail(book);
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
        return TypedActionResult.success(book, false);
    }
}
