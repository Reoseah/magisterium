package io.github.reoseah.magisterium.item;

import com.mojang.serialization.Codec;
import net.minecraft.block.DispenserBlock;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.*;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlazeBladeItem extends SwordItem implements ProjectileItem {
    public static final ComponentType<Long> LAST_TICK = ComponentType.<Long>builder().codec(Codec.LONG).packetCodec(PacketCodecs.LONG).build();
    public static final RegistryKey<DamageType> FIRE_ATTACK = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("magisterium", "fire_attack"));

    public static final Item INSTANCE = create("blaze_blade");

    private static Item create(String name) {
        var id = Identifier.of("magisterium", name);
        var registryKey = RegistryKey.of(RegistryKeys.ITEM, id);
        var settings = new Item.Settings().registryKey(registryKey).useItemPrefixedTranslationKey().useCooldown(1.0F).rarity(Rarity.UNCOMMON);
        return new BlazeBladeItem(MagisteriumMaterials.BLAZE, 2F, -2.4F, settings);
    }

    public BlazeBladeItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClient) {
            return;
        }

        var lastTick = stack.get(LAST_TICK);
        if (lastTick == null) {
            stack.set(LAST_TICK, world.getTime());
            return;
        }
        var ticksPassed = world.getTime() - lastTick;
        if (ticksPassed >= 40) {
            stack.set(LAST_TICK, world.getTime());

            var damage = (int) Math.max(1, ticksPassed / 40);
            if (entity instanceof PlayerEntity player) {
                if (selected) {
                    stack.damage(damage, player, EquipmentSlot.MAINHAND);
                } else {
                    stack.damage(damage, player);
                }
            }
        }
    }

    @Override
    @Nullable
    public DamageSource getDamageSource(LivingEntity user) {
        return user.getDamageSources().create(FIRE_ATTACK, user);
    }

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postDamageEntity(stack, target, attacker);
        target.setOnFireFor(4);
    }

    @Override
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
        return true;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        if (world instanceof ServerWorld serverWorld) {
            ProjectileEntity.spawnWithVelocity( //
                    (world2, projectile, shooter) -> {
                        SmallFireballEntity entity = new SmallFireballEntity(world, projectile, Vec3d.ZERO);
                        entity.setPos(entity.getX(), user.getEyeY(), entity.getZ());
                        return entity;
                    }, //
                    serverWorld, //
                    stack, //
                    user, //
                    0.0F, //
                    1.5F, //
                    4.0F);
        }

        world.playSound( //
                null, //
                user.getX(), //
                user.getY(), //
                user.getZ(), //
                SoundEvents.ENTITY_GHAST_SHOOT, //
                SoundCategory.NEUTRAL, //
                0.5F, //
                0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        stack.damage(10, user, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

        return ActionResult.SUCCESS;
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        var random = world.getRandom();
        double vx = random.nextTriangular(direction.getOffsetX(), 0.11485000000000001);
        double vy = random.nextTriangular(direction.getOffsetY(), 0.11485000000000001);
        double vz = random.nextTriangular(direction.getOffsetZ(), 0.11485000000000001);
        var velocity = new Vec3d(vx, vy, vz);
        var entity = new SmallFireballEntity(world, pos.getX(), pos.getY(), pos.getZ(), velocity);
        entity.setVelocity(velocity);
        return entity;
    }

    @Override
    public void initializeProjectile(ProjectileEntity entity, double x, double y, double z, float power, float uncertainty) {
    }

    @Override
    public ProjectileItem.Settings getProjectileSettings() {
        return ProjectileItem.Settings.builder().positionFunction((pointer, facing) -> DispenserBlock.getOutputLocation(pointer, 1.0, Vec3d.ZERO)).uncertainty(6.6666665F).power(1.0F).build();
    }
}
