package io.github.reoseah.magisterium.screen;

import com.mojang.datafixers.util.Pair;
import io.github.reoseah.magisterium.magisterium.page.SlotProperties;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class SpellBookSlot extends Slot {
    public SlotProperties properties;

    public SpellBookSlot(Inventory inventory, int index) {
        super(inventory, index, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public void setConfiguration(@Nullable SlotProperties config) {
        this.properties = config;
        if (config != null) {
            ((MutableSlot) this).magisterium$setPos(config.x, config.y);
        } else {
            ((MutableSlot) this).magisterium$setPos(Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return this.properties != null && !this.properties.output && (this.properties.ingredient.isEmpty() || this.properties.ingredient.get().test(stack));
    }

    @Nullable
    @Override
    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return this.properties != null ? this.properties.getBackgroundSprite() : null;
    }
}
