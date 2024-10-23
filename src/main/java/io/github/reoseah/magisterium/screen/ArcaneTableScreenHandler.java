package io.github.reoseah.magisterium.screen;

import com.mojang.datafixers.util.Pair;
import io.github.reoseah.magisterium.block.ArcaneTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ArcaneTableScreenHandler extends ScreenHandler {
    public static final ScreenHandlerType<ArcaneTableScreenHandler> TYPE = new ScreenHandlerType<>(ArcaneTableScreenHandler::new, FeatureFlags.DEFAULT_ENABLED_FEATURES);

    public static final Identifier EMPTY_BOOK_SLOT_TEXTURE = Identifier.of("magisterium:item/empty_slot_spell_book");

    private final ScreenHandlerContext context;
    protected final Inventory spellBookInventory;
    protected final Inventory inventory;

    protected ArcaneTableScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, Inventory spellBookInventory, Inventory inventory) {
        super(TYPE, syncId);
        this.context = context;
        this.spellBookInventory = spellBookInventory;
        this.inventory = inventory;

        this.addSlot(new Slot(this.spellBookInventory, 0, 26, 36) {
            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_BOOK_SLOT_TEXTURE);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 6; column++) {
                this.addSlot(new Slot(this.inventory, column + row * 6, 62 + column * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    public ArcaneTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY, new SimpleInventory(1), new SimpleInventory(18));
    }

    public ArcaneTableScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        this(syncId, playerInventory, context, new SimpleInventory(1), new SimpleInventory(18));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, ArcaneTableBlock.INSTANCE);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> {
            this.dropInventory(player, this.spellBookInventory);
        });
    }

}
