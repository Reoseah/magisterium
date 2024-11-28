package io.github.reoseah.magisterium.screen;

import io.github.reoseah.magisterium.MagisteriumSounds;
import io.github.reoseah.magisterium.data.SpellEffectLoader;
import io.github.reoseah.magisterium.data.effect.EmptySpellEffect;
import io.github.reoseah.magisterium.data.effect.SpellEffect;
import io.github.reoseah.magisterium.data.element.SlotProperties;
import io.github.reoseah.magisterium.item.SpellBookItem;
import io.github.reoseah.magisterium.network.s2c.FinishSpellPayload;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SpellBookScreenHandler extends ScreenHandler {
    public static final ScreenHandlerType<SpellBookScreenHandler> TYPE = new ScreenHandlerType<>(SpellBookScreenHandler::new, FeatureFlags.DEFAULT_ENABLED_FEATURES);

    public static final int PREVIOUS_PAGE_BUTTON = 0;
    public static final int NEXT_PAGE_BUTTON = 1;

    public final Context context;
    public final Property currentPage;
    public final Inventory inventory = new SpellBookInventory(this);
    private final Inventory spellBook;

    private @Nullable SpellReadingState spellState;

    public SpellBookScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, new ClientContext());
    }

    public SpellBookScreenHandler(int syncId, PlayerInventory playerInv, SpellBookScreenHandler.Context context) {
        super(TYPE, syncId);
        this.context = context;
        this.currentPage = this.addProperty(context.getComponentAsProperty(SpellBookItem.CURRENT_PAGE));

        for (int i = 0; i < 16; i++) {
            this.addSlot(new SpellBookSlot(this.inventory, i, Integer.MIN_VALUE, Integer.MIN_VALUE));
        }

        for (int x = 0; x < 9; x++) {
            this.addSlot(new Slot(playerInv, x, 48 + x * 18, 185));
        }

        this.spellBook = new SimpleInventory(context.getStack()) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        };
        this.addSlot(new Slot(this.spellBook, 0, Integer.MIN_VALUE, Integer.MIN_VALUE) {
            @Override
            public boolean canTakeItems(PlayerEntity player) {
                return false;
            }

            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });
    }

    public void startUtterance(Identifier id, ServerPlayerEntity player) {
        var effect = SpellEffectLoader.getInstance().effects.entrySet() //
                .stream() //
                .filter(entry -> entry.getKey().equals(id)) //
                .findFirst() //
                .map(Map.Entry::getValue) //
                .orElse(EmptySpellEffect.INSTANCE);

        if (effect != EmptySpellEffect.INSTANCE) {
            this.spellState = SpellReadingState.start(effect, player);
        }
    }

    public void stopUtterance() {
        this.spellState = null;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        var slot = this.slots.get(index);
        var stack = slot.getStack();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var previous = stack.copy();
        if (index < 16) {
            if (!this.insertItem(stack, 16, 16 + 9, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(stack, previous);
        } else {
            var validSlotIndexes = new IntArraySet();
            int total = 0;
            for (int i = 0; i < 16; i++) {
                var definition = ((SpellBookSlot) this.getSlot(i)).config;
                if (definition != null && !definition.output && (definition.ingredient.isEmpty() || definition.ingredient.get().test(stack))) {
                    var slotStack = this.getSlot(i).getStack();
                    if (slotStack.isEmpty() || ItemStack.areItemsAndComponentsEqual(slotStack, stack)) {
                        validSlotIndexes.add(i);
                        total += slotStack.getCount();
                    }
                }
            }

            if (!validSlotIndexes.isEmpty()) {
                int targetCount = (total + stack.getCount()) / validSlotIndexes.size();
                for (int idx : validSlotIndexes) {
                    int slotCount = this.getSlot(idx).getStack().getCount();
                    int toAdd = Math.min(targetCount - slotCount, this.getSlot(idx).getMaxItemCount(stack) - slotCount);
                    if (toAdd > 0) {
                        stack = this.getSlot(idx).insertStack(stack, toAdd);
                    }
                }
                for (int idx : validSlotIndexes) {
                    if (stack.isEmpty()) {
                        break;
                    }
                    stack = this.getSlot(idx).insertStack(stack);
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
        return previous;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.dropInventory(player, this.inventory);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (!this.context.canUse(player)
                // some spell effects call player.closeHandledScreen() on server only,
                // which apparently doesn't close it correctly on the client?
                || player.currentScreenHandler != this) {
            return false;
        }

        if (player instanceof ServerPlayerEntity serverPlayer && this.spellState != null) {
            this.spellState.tick(serverPlayer, this);
        }

        return true;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        switch (id) {
            case PREVIOUS_PAGE_BUTTON -> {
                int page = this.currentPage.get();
                if (page < 2) {
                    return false;
                }
                this.changePage(page - 2, player);

                return true;
            }
            case NEXT_PAGE_BUTTON -> {
                int page = this.currentPage.get();
                this.changePage(page + 2, player);

                return true;
            }
        }
        return false;
    }

    private void changePage(int page, PlayerEntity player) {
        this.currentPage.set(page);
        this.dropInventory(player, this.inventory);
        for (int i = 0; i < 16; i++) {
            ((SpellBookSlot) this.slots.get(i)).setConfiguration(null);
        }
    }

    public void applySlotProperties(SlotProperties[] properties) {
        for (int i = 0; i < 16; i++) {
            ((SpellBookSlot) this.slots.get(i)).setConfiguration(i < properties.length ? properties[i] : null);
        }
    }

    public ItemStack getSpellBook() {
        return this.spellBook.getStack(0);
    }

    public static abstract class Context {
        public abstract ItemStack getStack();

        public abstract Property getComponentAsProperty(ComponentType<Integer> component);

        public abstract boolean canUse(PlayerEntity player);
    }

    public static class ClientContext extends Context {
        @Override
        public ItemStack getStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public Property getComponentAsProperty(ComponentType<Integer> component) {
            return Property.create();
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }

    public static class LecternContext extends Context {
        private final World world;
        private final BlockPos pos;
        private final ItemStack stack;

        public LecternContext(World world, BlockPos pos, ItemStack stack) {
            this.world = world;
            this.pos = pos;
            this.stack = stack;
        }

        @Override
        public ItemStack getStack() {
            return this.stack;
        }

        @Override
        public Property getComponentAsProperty(ComponentType<Integer> component) {
            return new Property() {
                @Override
                public int get() {
                    return stack.getOrDefault(component, 0);
                }

                @Override
                public void set(int value) {
                    stack.set(component, value);

                    var be = world.getBlockEntity(pos);
                    if (be != null) {
                        be.markDirty();
                    }
                }
            };
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return this.world.getBlockState(this.pos).getBlock() instanceof LecternBlock //
                    && this.world.getBlockEntity(this.pos) instanceof LecternBlockEntity lectern //
                    && lectern.getBook() == this.stack //
                    && player.squaredDistanceTo(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64;
        }
    }

    public static class HandContext extends Context {
        private final Hand hand;
        private final ItemStack stack;

        public HandContext(Hand hand, ItemStack stack) {
            this.hand = hand;
            this.stack = stack;
        }

        @Override
        public ItemStack getStack() {
            return this.stack;
        }

        @Override
        public Property getComponentAsProperty(ComponentType<Integer> component) {
            return new Property() {
                @Override
                public int get() {
                    return stack.getOrDefault(component, 0);
                }

                @Override
                public void set(int value) {
                    stack.set(component, value);
                }
            };
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return player.getStackInHand(this.hand) == this.stack;
        }
    }

    public static class SpellReadingState {
        private final SpellEffect effect;
        private final long startTime;
        private long lastSoundTime;

        private SpellReadingState(SpellEffect effect, long time) {
            this.effect = effect;
            this.lastSoundTime = this.startTime = time;
        }

        public static SpellReadingState start(SpellEffect effect, ServerPlayerEntity player) {
            var world = player.getWorld();
            var time = world.getTime();

            world.playSound(null, player.getX(), player.getY(), player.getZ(), MagisteriumSounds.CHANT, SoundCategory.PLAYERS, 0.25F, 1.0f);

            return new SpellReadingState(effect, time);
        }

        public void tick(ServerPlayerEntity player, SpellBookScreenHandler handler) {
            var world = player.getWorld();
            var recipeTicks = this.effect.duration * world.getTickManager().getTickRate();
            long time = world.getTime();
            if (time - this.startTime >= recipeTicks) {
                this.effect.finish(player, handler.inventory, handler.context);

                ServerPlayNetworking.send(player, FinishSpellPayload.INSTANCE);
                handler.stopUtterance();
            }

            if (time - this.lastSoundTime >= 25) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), MagisteriumSounds.CHANT, SoundCategory.PLAYERS, 0.25F, 1.0f);
                this.lastSoundTime = time;
            }
        }
    }
}