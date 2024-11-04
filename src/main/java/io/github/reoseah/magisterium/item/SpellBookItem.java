package io.github.reoseah.magisterium.item;

import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
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
            .codec(Codecs.NON_NEGATIVE_INT) //
            .packetCodec(PacketCodecs.VAR_INT) //
            .build();
    public static final ComponentType<List<ItemStack>> CONTENTS = ComponentType.<List<ItemStack>>builder() //
            .codec(ItemStack.OPTIONAL_CODEC.listOf()) //
            .packetCodec(ItemStack.OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toList())) //
            .build();

    public static final Item INSTANCE = new SpellBookItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE).component(CURRENT_PAGE, 0));

    protected SpellBookItem(net.minecraft.item.Item.Settings settings) {
        super(settings);
    }

    public static ItemStack createTestBook() {
        ItemStack book = new ItemStack(INSTANCE);

        book.set(CONTENTS, Util.make(DefaultedList.ofSize(18, ItemStack.EMPTY), list -> {
            list.set(0, BookmarkItem.INSTANCE.getDefaultStack());
            list.set(1, SpellPageItem.AWAKEN_THE_FLAME.getDefaultStack());
            list.set(2, SpellPageItem.QUENCH_THE_FLAME.getDefaultStack());
            list.set(3, SpellPageItem.GLYPHIC_IGNITION.getDefaultStack());
            list.set(4, SpellPageItem.CONFLAGRATE.getDefaultStack());
            list.set(5, SpellPageItem.COLD_SNAP.getDefaultStack());
            list.set(6, SpellPageItem.ILLUSORY_WALL.getDefaultStack());
            list.set(7, SpellPageItem.ARCANE_LIFT.getDefaultStack());
            list.set(8, SpellPageItem.DISPEL_MAGIC.getDefaultStack());
        }));

        return book;
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
        var pages = stack.get(CONTENTS);
        if (pages != null && !pages.isEmpty()) {
            var nonEmptyCount = pages.stream().filter(page -> !page.isEmpty()).count();
            tooltip.add(Text.translatable("item.magisterium.spell_book.pages", nonEmptyCount).formatted(Formatting.GRAY));
        } else {
            tooltip.add(Text.translatable("item.magisterium.spell_book.empty").formatted(Formatting.GRAY));
        }
    }
}
