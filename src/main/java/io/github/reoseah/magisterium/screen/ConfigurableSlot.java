package io.github.reoseah.magisterium.screen;

import com.mojang.datafixers.util.Pair;
import io.github.reoseah.magisterium.spellbook.element.SlotConfiguration;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

// TODO: rename to SpellBookSlot
public class ConfigurableSlot extends Slot {
    protected SlotConfiguration config;

    public ConfigurableSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    public void setConfiguration(@Nullable SlotConfiguration config) {
        this.config = config;
        if (config != null) {
            ((MutableSlot) this).magisterium$setPos(config.x, config.y);
        } else {
            ((MutableSlot) this).magisterium$setPos(Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
    }

    public SlotConfiguration getConfiguration() {
        return this.config;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return this.config != null && !this.config.output;
    }

    @Nullable
    @Override
    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return this.config != null ? this.config.getBackgroundSprite() : null;
    }
}
