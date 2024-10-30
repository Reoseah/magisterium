package io.github.reoseah.magisterium.item;

import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.List;

public class SpellBookItem extends Item {
    public static final ComponentType<Integer> CURRENT_PAGE = ComponentType.<Integer>builder() //
            .codec(Codecs.NONNEGATIVE_INT) //
            .packetCodec(PacketCodecs.VAR_INT) //
            .build();
    public static final ComponentType<List<ItemStack>> CONTENTS = ComponentType.<List<ItemStack>>builder() //
            .codec(ItemStack.OPTIONAL_CODEC.listOf()) //
            .packetCodec(ItemStack.OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toList())) //
            .build();
    public static final ComponentType<Unit> UNSTABLE_CHARGE = ComponentType.<Unit>builder() //
            .codec(Unit.CODEC) //
            .build();

    public static final Item INSTANCE = new SpellBookItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE).component(CURRENT_PAGE, 0));

    protected SpellBookItem(Settings settings) {
        super(settings);
    }

    public static ItemStack createTestBook() {
        ItemStack book = new ItemStack(INSTANCE);

        book.set(CONTENTS, Util.make(DefaultedList.ofSize(18, ItemStack.EMPTY), list -> {
            list.set(0, BookmarkItem.INSTANCE.getDefaultStack());
            list.set(1, SpellPageItem.AWAKEN_THE_FLAME.getDefaultStack());
            list.set(2, SpellPageItem.QUENCH_THE_FLAME.getDefaultStack());
            list.set(3, SpellPageItem.GLYPHIC_IGNITION.getDefaultStack());
            list.set(4, SpellPageItem.CONFLAGRATE.getDefaultStack());
            list.set(5, SpellPageItem.ILLUSORY_WALL.getDefaultStack());
//            list.set(6, SpellPageItem.UNSTABLE_CHARGE.getDefaultStack());
            list.set(7, SpellPageItem.COLD_SNAP.getDefaultStack());
        }));

        return book;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        var book = player.getStackInHand(hand);

        if (!book.contains(CONTENTS)) {
            return TypedActionResult.fail(book);
        }

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
        if (stack.contains(UNSTABLE_CHARGE)) {
            tooltip.add(Text.translatable("item.magisterium.spell_book.unstable_charge").formatted(Formatting.GRAY));
        }
    }

    @Override
    public float getBonusAttackDamage(Entity target, float baseAttackDamage, DamageSource damageSource) {
        var stack = damageSource.getWeaponStack();
        if (stack != null && stack.contains(UNSTABLE_CHARGE)) {
            return baseAttackDamage + 9;
        }
        return super.getBonusAttackDamage(target, baseAttackDamage, damageSource);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (stack.contains(UNSTABLE_CHARGE)) {
            stack.remove(UNSTABLE_CHARGE);
            if (target.isDead() && target.getWorld().getRandom().nextFloat() < .5) {
                if (target.getType() == EntityType.ZOMBIE) {
                    target.dropItem(Items.ZOMBIE_HEAD);
                } else if (target.getType() == EntityType.SKELETON) {
                    target.dropItem(Items.SKELETON_SKULL);
                } else if (target.getType() == EntityType.CREEPER) {
                    target.dropItem(Items.CREEPER_HEAD);
                } else if (target.getType() == EntityType.PLAYER) {
                    var player = (PlayerEntity) target;
                    var head = new ItemStack(Items.PLAYER_HEAD);
                    head.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
                    target.dropStack(head);
                }
            }
            // TODO sent a packet to spawn particles and play a sound
        }
        return super.postHit(stack, target, attacker);
    }
}
