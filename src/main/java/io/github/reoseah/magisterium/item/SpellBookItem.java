package io.github.reoseah.magisterium.item;

import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

public class SpellBookItem extends Item {
    public static final ComponentType<NbtCompound> PAGE_DATA = ComponentType.<NbtCompound>builder().codec(NbtCompound.CODEC).packetCodec(PacketCodecs.NBT_COMPOUND).build();
    public static final ComponentType<Integer> CURRENT_PAGE = ComponentType.<Integer>builder().codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT).build();

    public static final Item INSTANCE = new SpellBookItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE).component(CURRENT_PAGE, 0));

    protected SpellBookItem(Settings settings) {
        super(settings);
    }

    public static ItemStack createTestBook(RegistryWrapper.WrapperLookup registryLookup) {
        ItemStack book = new ItemStack(INSTANCE);

        book.set(PAGE_DATA, Util.make(new NbtCompound(), nbt -> {
            var inventory = new SimpleInventory(18);

            inventory.setStack(0, SpellPageItem.createSpellPage(Identifier.of("magisterium:awaken_the_flame")));
            inventory.setStack(1, SpellPageItem.createSpellPage(Identifier.of("magisterium:quench_the_flame")));
            inventory.setStack(2, SpellPageItem.createSpellPage(Identifier.of("magisterium:conflagrate")));

            nbt.put("Inventory", inventory.toNbtList(registryLookup));
        }));

        return book;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack book = player.getStackInHand(hand);
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
