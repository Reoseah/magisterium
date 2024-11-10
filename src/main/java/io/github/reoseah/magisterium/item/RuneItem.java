package io.github.reoseah.magisterium.item;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.AdvancedExplosionBehavior;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class RuneItem extends Item {
    public static final int MAX_CHARGE = 60;
    public static final ComponentType<Integer> CHARGE = ComponentType.<Integer>builder().codec(Codecs.NON_NEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT).build();

    public static final Text CHARGED_TOOLTIP = Text.translatable("item.magisterium.runes.charged").formatted(Formatting.GRAY);

    public static final Item BLAZE = create("blaze_rune");
    public static final Item WIND = create("wind_rune");

    public static Item create(String name) {
        var id = Identifier.of("magisterium", name);
        var registryKey = RegistryKey.of(RegistryKeys.ITEM, id);
        var settings = new Item.Settings() //
                .registryKey(registryKey) //
                .useItemPrefixedTranslationKey() //
                .rarity(Rarity.UNCOMMON) //
                .component(CHARGE, 0) //
                .maxCount(1);

        return new RuneItem(settings);
    }

    public RuneItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        var charge = stack.get(CHARGE);
        return charge != null && charge != 0 && charge != MAX_CHARGE;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        var charge = stack.get(CHARGE);
        return charge != null ? Math.min(13, 13 * charge / MAX_CHARGE) : 0;
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0x8080FF;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.getTime() % 20 == 0) {
            var charge = stack.get(CHARGE);
            if (charge == null) {
                stack.set(CHARGE, 0);
                charge = 0;
            }
            if (charge < MAX_CHARGE) {
                stack.set(CHARGE, charge + 1);
            }
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        var charge = stack.get(CHARGE);
        return charge != null && charge == MAX_CHARGE;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        var charge = stack.get(CHARGE);
        if (charge != null && charge == MAX_CHARGE) {
            tooltip.add(CHARGED_TOOLTIP);
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var stack = context.getStack();
        var charge = stack.get(CHARGE);

        if (charge != null && charge == MAX_CHARGE) {
            var success = false;
            if (stack.isOf(RuneItem.BLAZE)) {
                var world = context.getWorld();
                var pos = context.getBlockPos();
                var state = world.getBlockState(pos);

                if (CampfireBlock.canBeLit(state) || CandleBlock.canBeLit(state) || CandleCakeBlock.canBeLit(state)) {
                    world.setBlockState(pos, state.with(Properties.LIT, true));
                    success = true;
                } else {
                    var offset = pos.offset(context.getSide());
                    if (AbstractFireBlock.canPlaceAt(world, offset, context.getHorizontalPlayerFacing())) {
                        var random = world.getRandom();
                        world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
                        world.setBlockState(offset, AbstractFireBlock.getState(world, offset));
                        world.emitGameEvent(context.getPlayer(), GameEvent.BLOCK_PLACE, offset);
                        success = true;
                    }
                }
            } else if (stack.isOf(RuneItem.WIND)) {
                var world = context.getWorld();
                var hitPos = context.getHitPos();
                world.createExplosion(null, //
                        null, //
                        new AdvancedExplosionBehavior( //
                                true, false, Optional.of(1.22F), Registries.BLOCK.getOptional(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())), //
                        hitPos.getX(), //
                        hitPos.getY(), //
                        hitPos.getZ(), //
                        1.2F, //
                        false, //
                        World.ExplosionSourceType.TRIGGER, //
                        ParticleTypes.GUST_EMITTER_SMALL, //
                        ParticleTypes.GUST_EMITTER_LARGE, //
                        SoundEvents.ENTITY_WIND_CHARGE_WIND_BURST);
                success = true;
            }
            if (success) {
                stack.set(CHARGE, 0);
                return ActionResult.SUCCESS;
            }
        }

        return super.useOnBlock(context);
    }
}

