package io.github.reoseah.magisterium.screen;

import io.github.reoseah.magisterium.MagisteriumSounds;
import io.github.reoseah.magisterium.data.SpellEffectLoader;
import io.github.reoseah.magisterium.data.effect.EmptySpellEffect;
import io.github.reoseah.magisterium.data.effect.SpellEffect;
import io.github.reoseah.magisterium.data.element.SlotProperties;
import io.github.reoseah.magisterium.item.SpellBookItem;
import io.github.reoseah.magisterium.recipe.SpellRecipeInput;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.BlockEntity;
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

public class SpellBookScreenHandler extends ScreenHandler {
    public static final ScreenHandlerType<SpellBookScreenHandler> TYPE = new ScreenHandlerType<>(SpellBookScreenHandler::new, FeatureFlags.DEFAULT_ENABLED_FEATURES);

    public static final int PREVIOUS_PAGE_BUTTON = 0;
    public static final int NEXT_PAGE_BUTTON = 1;

    public final Context context;
    public final Property currentPage;
    public final Property isUttering;
    public final Inventory inventory = new SpellBookInventory(this);

    private long utteranceStart;

    // TODO change this to a list of effects
    private @Nullable SpellEffect spellEffect;

    public SpellBookScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, new ClientContext());
    }

    public SpellBookScreenHandler(int syncId, PlayerInventory playerInv, Context context) {
        super(TYPE, syncId);
        this.context = context;
        this.currentPage = this.addProperty(context.createProperty(SpellBookItem.CURRENT_PAGE));
        this.isUttering = this.addProperty(Property.create());

        for (int i = 0; i < 16; i++) {
            this.addSlot(new SpellBookSlot(this.inventory, i, Integer.MIN_VALUE, Integer.MIN_VALUE));
        }

        for (int x = 0; x < 9; x++) {
            this.addSlot(new Slot(playerInv, x, 48 + x * 18, 185));
        }

        this.addSlot(new Slot(new SimpleInventory(context.getStack()), 0, Integer.MIN_VALUE, Integer.MIN_VALUE) {
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
        this.spellEffect = SpellEffectLoader.getInstance().effects.values() //
                .stream() //
                .filter(effect -> effect.utterance.equals(id)) //
                .findFirst() //
                .orElse(EmptySpellEffect.INSTANCE);

        if (this.spellEffect != EmptySpellEffect.INSTANCE) {
            this.isUttering.set(1);
            this.utteranceStart = player.getWorld().getTime();
            this.lastSoundTime = null;
        }
    }

    public void stopUtterance() {
        this.isUttering.set(0);
        this.utteranceStart = 0;
        this.spellEffect = null;
        this.lastSoundTime = null;

        this.sendContentUpdates();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack previous = stack.copy();
        if (index < 16) {
            if (!this.insertItem(stack, 16, 16 + 9, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(stack, previous);
        } else {
            IntArraySet slotsToSpreadStackTo = new IntArraySet();
            int total = 0;
            for (int i = 0; i < 16; i++) {
                SlotProperties definition = ((SpellBookSlot) this.getSlot(i)).config;
                if (definition != null && !definition.output && (definition.ingredient.isEmpty() || definition.ingredient.get().test(stack))) {
                    ItemStack slotStack = this.getSlot(i).getStack();
                    if (slotStack.isEmpty() || ItemStack.areItemsAndComponentsEqual(slotStack, stack)) {
                        slotsToSpreadStackTo.add(i);
                        total += slotStack.getCount();
                    }
                }
            }

            if (!slotsToSpreadStackTo.isEmpty()) {
                int targetCount = (total + stack.getCount()) / slotsToSpreadStackTo.size();
                for (int idx : slotsToSpreadStackTo) {
                    ItemStack slotStack = this.getSlot(idx).getStack();
                    int slotCount = slotStack.getCount();
                    int toAdd = Math.min(targetCount - slotCount, this.getSlot(idx).getMaxItemCount(stack) - slotCount);
                    if (toAdd > 0) {
                        stack = this.getSlot(idx).insertStack(stack, toAdd);
                    }
                }
                for (int idx : slotsToSpreadStackTo) {
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

    private Long lastSoundTime = null;

    @Override
    public boolean canUse(PlayerEntity player) {
        if (player.currentScreenHandler != this) {
            return false;
        }

        // this gets called every tick, so it's a tick method effectively
        if (this.spellEffect != null) {
            var recipeTicks = this.spellEffect.duration * player.getWorld().getTickManager().getTickRate();
            long time = player.getWorld().getTime();
            if (time - this.utteranceStart >= recipeTicks) {
//                ItemStack result =
                this.spellEffect.finish(new SpellRecipeInput(this.inventory, player, this.context), player.getWorld().getRegistryManager());

//                if (!result.isEmpty()) {
//                    this.insertResult(result, player);
//                }
                this.stopUtterance();
            }

            if (this.lastSoundTime == null || time - this.lastSoundTime >= 25) {
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), MagisteriumSounds.CHANT, SoundCategory.PLAYERS, 0.25F, 1.0f);
                this.lastSoundTime = time;
            }
        }

        return this.context.canUse(player);
    }

    protected void insertResult(ItemStack result, PlayerEntity player) {
        boolean inserted = false;
        for (int i = 0; i < 16; i++) {
            SlotProperties configuration = ((SpellBookSlot) this.slots.get(i)).getConfiguration();
            if (configuration != null && configuration.output) {
                ItemStack excess = insertStack(i, result);
                if (!excess.isEmpty()) {
                    player.dropItem(excess, false);
                }
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            player.dropItem(result, false);
        }
    }

    protected ItemStack insertStack(int slot, ItemStack stack) {
        ItemStack current = this.inventory.getStack(slot);
        if (current.isEmpty()) {
            this.inventory.setStack(slot, stack);
            return ItemStack.EMPTY;
        } else if (ItemStack.areItemsAndComponentsEqual(current, stack)) {
            int amount = Math.min(stack.getCount(), current.getMaxCount() - current.getCount());
            if (amount > 0) {
                current.increment(amount);
                this.inventory.setStack(slot, current);
                stack.decrement(amount);
            }
            return stack;
        }
        return stack;
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
        for (int i = 0; i < properties.length; i++) {
            ((SpellBookSlot) this.slots.get(i)).setConfiguration(properties[i]);
        }
        for (int i = properties.length; i < 16; i++) {
            ((SpellBookSlot) this.slots.get(i)).setConfiguration(null);
        }
    }

    public ItemStack getSpellBook() {
        return this.slots.get(16 + 9).getStack();
    }

    public static abstract class Context {
        protected final ItemStack stack;

        public Context(ItemStack stack) {
            this.stack = stack;
        }

        public ItemStack getStack() {
            return this.stack;
        }

        public abstract Property createProperty(ComponentType<Integer> component);

        public abstract boolean canUse(PlayerEntity player);

        public abstract <T> void setStackComponent(ComponentType<T> component, T value);
    }

    public static class ClientContext extends Context {
        public ClientContext() {
            super(ItemStack.EMPTY);
        }

        @Override
        public Property createProperty(ComponentType<Integer> component) {
            return Property.create();
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }

        @Override
        public <T> void setStackComponent(ComponentType<T> component, T value) {
            // no-op
        }
    }

    public static class LecternContext extends Context {
        private final World world;
        private final BlockPos pos;

        public LecternContext(World world, BlockPos pos, ItemStack stack) {
            super(stack);
            this.world = world;
            this.pos = pos;
        }

        @Override
        public Property createProperty(ComponentType<Integer> component) {
            return new Property() {
                @Override
                public int get() {
                    return stack.getOrDefault(component, 0);
                }

                @Override
                public void set(int value) {
                    stack.set(component, value);

                    BlockEntity be = world.getBlockEntity(pos);
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

        @Override
        public <T> void setStackComponent(ComponentType<T> component, T value) {
            this.stack.set(component, value);
            BlockEntity be = this.world.getBlockEntity(this.pos);
            if (be != null) {
                be.markDirty();
            }
        }
    }

    public static class HandContext extends Context {
        private final Hand hand;
        private final ItemStack stack;

        public HandContext(Hand hand, ItemStack stack) {
            super(stack);
            this.hand = hand;
            this.stack = stack;
        }

        @Override
        public Property createProperty(ComponentType<Integer> component) {
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

        @Override
        public <T> void setStackComponent(ComponentType<T> component, T value) {
            this.stack.set(component, value);
        }
    }

}