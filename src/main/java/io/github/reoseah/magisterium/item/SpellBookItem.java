package io.github.reoseah.magisterium.item;

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

    public static final Identifier ID = Identifier.of("magisterium:spell_book");
    public static final RegistryKey<Item> KEY = RegistryKey.of(RegistryKeys.ITEM, ID);
    public static final Item INSTANCE = new SpellBookItem(new Item.Settings() //
            .registryKey(KEY) //
            .useItemPrefixedTranslationKey() //
            .maxCount(1) //
            .rarity(Rarity.RARE) //
            .component(CURRENT_PAGE, 0) //
            .modelId(Identifier.of("magisterium", "spell_book_in_hand")));

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
                    return Text.translatable("container.magisterium.spell_book");
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
