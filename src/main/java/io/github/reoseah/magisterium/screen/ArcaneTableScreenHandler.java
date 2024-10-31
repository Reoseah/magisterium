package io.github.reoseah.magisterium.screen;

import com.mojang.datafixers.util.Pair;
import io.github.reoseah.magisterium.item.MagisteriumItemTags;
import io.github.reoseah.magisterium.block.ArcaneTableBlock;
import io.github.reoseah.magisterium.item.SpellBookItem;
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

import java.util.ArrayList;

public class ArcaneTableScreenHandler extends ScreenHandler {
    public static final ScreenHandlerType<ArcaneTableScreenHandler> TYPE = new ScreenHandlerType<>(ArcaneTableScreenHandler::new, FeatureFlags.DEFAULT_ENABLED_FEATURES);

    public static final Identifier EMPTY_BOOK_SLOT_TEXTURE = Identifier.of("magisterium:item/empty_slot_spell_book");

    private final ScreenHandlerContext context;
    protected final Inventory bookInventory;
    protected final Inventory bookContentsInventory;

    protected ArcaneTableScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, Inventory bookInventory, Inventory bookContentsInventory) {
        super(TYPE, syncId);
        this.context = context;
        this.bookInventory = bookInventory;
        this.bookContentsInventory = bookContentsInventory;

        this.addSlot(new Slot(this.bookInventory, 0, 22, 36) {
            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_BOOK_SLOT_TEXTURE);
            }

            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(SpellBookItem.INSTANCE);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 6; column++) {
                this.addSlot(new Slot(this.bookContentsInventory, column + row * 6, 81 + column * 18, 18 + row * 18) {
                    @Override
                    public boolean canTakeItems(PlayerEntity playerEntity) {
                        return bookInventory.getStack(0).contains(SpellBookItem.CONTENTS);
                    }

                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return bookInventory.getStack(0).contains(SpellBookItem.CONTENTS) //
                                && stack.isIn(MagisteriumItemTags.SPELL_BOOK_COMPONENTS);
                    }
                });
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 22 + column * 18, 111 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 22 + column * 18, 169));
        }
    }

    public ArcaneTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY, new SimpleInventory(1), new SimpleInventory(18));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack previous = stack.copy();

        if (index < 19) {
            if (!this.insertItem(stack, 19, 19 + 9 * 4, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(stack, previous);
        } else {
            if (stack.isOf(SpellBookItem.INSTANCE)) {
                if (!this.insertItem(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.isIn(MagisteriumItemTags.SPELL_BOOK_COMPONENTS) && bookInventory.getStack(0).contains(SpellBookItem.CONTENTS)) {
                if (!this.insertItem(stack, 1, 1 + 18, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }


        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (stack.getCount() == previous.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, stack);
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
            this.dropInventory(player, this.bookInventory);
        });
    }

    public static ArcaneTableScreenHandler createServerSide(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        var bookInventory = new SimpleInventory(1);
        var bookContentsInventory = new BookContentsInventory(bookInventory);

        return new ArcaneTableScreenHandler(syncId, playerInventory, context, bookInventory, bookContentsInventory);
    }

    private static class BookContentsInventory extends SimpleInventory {
        private final SimpleInventory bookInventory;
        private ItemStack book;

        public BookContentsInventory(SimpleInventory bookInventory) {
            super(18);
            this.bookInventory = bookInventory;
            this.bookInventory.addListener(sender -> {
                var book = sender.getStack(0);
                if (book != this.book) {
                    this.clearWithoutNotifyingListeners();
                    if (book.isOf(SpellBookItem.INSTANCE)) {
                        var bookPages = book.get(SpellBookItem.CONTENTS);
                        if (bookPages != null) {
                            for (int i = 0; i < bookPages.size(); i++) {
                                this.heldStacks.set(i, bookPages.get(i));
                            }
                        }
                    }
                    this.book = book;
                }
            });
            this.addListener(sender -> {
                var book = this.bookInventory.getStack(0);
                if (book.isOf(SpellBookItem.INSTANCE)) {
                    book.set(SpellBookItem.CURRENT_PAGE, 0);
                    book.set(SpellBookItem.CONTENTS, new ArrayList<>(this.getHeldStacks()));
                }
            });
        }

        private void clearWithoutNotifyingListeners() {
            this.heldStacks.clear();
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return super.canInsert(stack) && stack.isIn(MagisteriumItemTags.SPELL_BOOK_COMPONENTS);
        }
    }
}
