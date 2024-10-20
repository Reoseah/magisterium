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
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

public class SpellBookItem extends Item {
    public static final ComponentType<Integer> CURRENT_PAGE = ComponentType.<Integer>builder().codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT).build();

    public static final Item INSTANCE = new SpellBookItem(new Item.Settings().maxCount(1).component(CURRENT_PAGE, 0));

    public SpellBookItem(Settings settings) {
        super(settings);
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
