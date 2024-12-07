package io.github.reoseah.magisterium.magisterium.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.screen.SpellBookInventory;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.List;

public class CraftItemEffect extends SpellEffect {
    public static final MapCodec<CraftItemEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(effect -> effect.duration), //
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(effect -> effect.ingredients), //
            ItemStack.CODEC.fieldOf("result").forGetter(effect -> effect.result) //
    ).apply(instance, CraftItemEffect::new));

    public final List<Ingredient> ingredients;
    public final ItemStack result;

    public CraftItemEffect(int duration, List<Ingredient> ingredients, ItemStack result) {
        super(duration);
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    public boolean matches(Inventory inventory) {
        for (int i = 0; i < this.ingredients.size(); i++) {
            if (!this.ingredients.get(i).test(inventory.getStack(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void finish(ServerPlayerEntity player, SpellBookInventory inventory, SpellBookScreenHandler.Context screenContext) {
        if (!this.matches(inventory)) {
            player.sendMessage(Text.translatable("magisterium.missing_spell_ingredients"), true);
            player.closeHandledScreen();
            return;
        }

        int count = this.result.getMaxCount();
        for (int i = 0; i < this.ingredients.size(); i++) {
            var stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                count = Math.min(count, stack.getCount());
            }
        }
        for (int i = 0; i < this.ingredients.size(); i++) {
            inventory.removeStack(i, count);
        }

        inventory.insertResult(this.result.copyWithCount(count), player);
    }
}
