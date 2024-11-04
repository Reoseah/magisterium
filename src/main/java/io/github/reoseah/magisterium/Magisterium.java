package io.github.reoseah.magisterium;

import com.google.common.collect.ImmutableSet;
import io.github.reoseah.magisterium.block.*;
import io.github.reoseah.magisterium.data.ItemValuesLoader;
import io.github.reoseah.magisterium.data.effect.*;
import io.github.reoseah.magisterium.data.element.*;
import io.github.reoseah.magisterium.item.*;
import io.github.reoseah.magisterium.network.*;
import io.github.reoseah.magisterium.particle.MagisteriumParticles;
import io.github.reoseah.magisterium.recipe.SpellRecipe;
import io.github.reoseah.magisterium.screen.ArcaneTableScreenHandler;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.*;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class Magisterium implements ModInitializer {
    public static final String MOD_ID = "magisterium";

    public static final Logger LOGGER = LoggerFactory.getLogger(Magisterium.class);

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK, "magisterium:arcane_table", ArcaneTableBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magisterium:glyph", GlyphBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magisterium:illusory_wall", IllusoryWallBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magisterium:arcane_lift", ArcaneLiftBlock.INSTANCE);

        Registry.register(Registries.BLOCK_ENTITY_TYPE, "magisterium:illusory_wall", IllusoryWallBlockEntity.TYPE);

        Registry.register(Registries.ITEM, "magisterium:arcane_table", new BlockItem(ArcaneTableBlock.INSTANCE, new Item.Settings()));
        Registry.register(Registries.ITEM, "magisterium:spell_book", SpellBookItem.INSTANCE);
        Registry.register(Registries.ITEM, "magisterium:awaken_the_flame_page", SpellPageItem.AWAKEN_THE_FLAME);
        Registry.register(Registries.ITEM, "magisterium:quench_the_flame_page", SpellPageItem.QUENCH_THE_FLAME);
        Registry.register(Registries.ITEM, "magisterium:glyphic_ignition_page", SpellPageItem.GLYPHIC_IGNITION);
        Registry.register(Registries.ITEM, "magisterium:conflagrate_page", SpellPageItem.CONFLAGRATE);
        Registry.register(Registries.ITEM, "magisterium:illusory_wall_page", SpellPageItem.ILLUSORY_WALL);
        Registry.register(Registries.ITEM, "magisterium:cold_snap_page", SpellPageItem.COLD_SNAP);
        Registry.register(Registries.ITEM, "magisterium:arcane_lift_page", SpellPageItem.ARCANE_LIFT);
        Registry.register(Registries.ITEM, "magisterium:dispel_magic_page", SpellPageItem.DISPEL_MAGIC);
        Registry.register(Registries.ITEM, "magisterium:data_driven_page", DataDrivenPageItem.INSTANCE);
        Registry.register(Registries.ITEM, "magisterium:bookmark", BookmarkItem.INSTANCE);
        Registry.register(Registries.ITEM, "magisterium:fire_rune", RuneItem.FIRE);
        Registry.register(Registries.ITEM, "magisterium:wind_rune", RuneItem.WIND);

        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:current_page", SpellBookItem.CURRENT_PAGE);
        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:contents", SpellBookItem.CONTENTS);
        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:elements", DataDrivenPageItem.ELEMENTS);
        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:effects", DataDrivenPageItem.EFFECTS);

        var group = FabricItemGroup.builder() //
                .icon(SpellBookItem.INSTANCE::getDefaultStack) //
                .displayName(Text.translatable("itemGroup.magisterium")) //
                .entries((displayContext, entries) -> {
                    entries.add(ArcaneTableBlock.INSTANCE);
                    entries.add(SpellBookItem.INSTANCE);
                    entries.add(SpellBookItem.createTestBook());
                    entries.add(SpellPageItem.AWAKEN_THE_FLAME);
                    entries.add(SpellPageItem.QUENCH_THE_FLAME);
                    entries.add(SpellPageItem.GLYPHIC_IGNITION);
                    entries.add(SpellPageItem.CONFLAGRATE);
                    entries.add(SpellPageItem.ILLUSORY_WALL);
                    entries.add(SpellPageItem.COLD_SNAP);
                    entries.add(SpellPageItem.ARCANE_LIFT);
                    entries.add(SpellPageItem.DISPEL_MAGIC);
                    entries.add(BookmarkItem.INSTANCE);
//                    entries.add(RuneItem.FIRE);
//                    entries.add(RuneItem.WIND);
                }) //
                .build();
        Registry.register(Registries.ITEM_GROUP, "magisterium", group);

        Registry.register(Registries.RECIPE_TYPE, "magisterium:spell", SpellRecipe.TYPE);

        Registry.register(Registries.RECIPE_SERIALIZER, "magisterium:spell", SpellRecipe.SERIALIZER);

        Registry.register(Registries.SCREEN_HANDLER, "magisterium:spell_book", SpellBookScreenHandler.TYPE);
        Registry.register(Registries.SCREEN_HANDLER, "magisterium:arcane_table", ArcaneTableScreenHandler.TYPE);

        Registry.register(Registries.PARTICLE_TYPE, "magisterium:energy", MagisteriumParticles.ENERGY);
        Registry.register(Registries.PARTICLE_TYPE, "magisterium:glyph_a", MagisteriumParticles.GLYPH_A);
        Registry.register(Registries.PARTICLE_TYPE, "magisterium:glyph_b", MagisteriumParticles.GLYPH_B);
        Registry.register(Registries.PARTICLE_TYPE, "magisterium:glyph_c", MagisteriumParticles.GLYPH_C);
        Registry.register(Registries.PARTICLE_TYPE, "magisterium:glyph_d", MagisteriumParticles.GLYPH_D);
        Registry.register(Registries.PARTICLE_TYPE, "magisterium:glyph_e", MagisteriumParticles.GLYPH_E);
        Registry.register(Registries.PARTICLE_TYPE, "magisterium:glyph_f", MagisteriumParticles.GLYPH_F);
        Registry.register(Registries.PARTICLE_TYPE, "magisterium:glyph_g", MagisteriumParticles.GLYPH_G);

        Registry.register(Registries.SOUND_EVENT, "magisterium:chant", MagisteriumSounds.CHANT);
        Registry.register(Registries.SOUND_EVENT, "magisterium:arcane_lift_loop", MagisteriumSounds.ARCANE_LIFT_LOOP);

        Registry.register(BookElement.REGISTRY, "magisterium:heading", Heading.CODEC);
        Registry.register(BookElement.REGISTRY, "magisterium:paragraph", Paragraph.CODEC);
        Registry.register(BookElement.REGISTRY, "magisterium:page_break", PageBreak.CODEC);
        Registry.register(BookElement.REGISTRY, "magisterium:inventory", BookInventory.CODEC);
        Registry.register(BookElement.REGISTRY, "magisterium:utterance", Utterance.CODEC);
        Registry.register(BookElement.REGISTRY, "magisterium:fold", Fold.CODEC);
        Registry.register(BookElement.REGISTRY, "magisterium:bookmark", BookmarkElement.CODEC);
        Registry.register(BookElement.REGISTRY, "magisterium:vertically_centered", VerticallyCenteredElement.CODEC);

        Registry.register(SpellEffect.REGISTRY, "magisterium:awaken_the_flame", AwakenFlameEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:quench_the_flame", QuenchFlameEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:glyphic_ignition", GlyphicIgnitionEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:conflagrate", ConflagrateEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:cold_snap", ColdSnapEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:illusory_wall", IllusoryWallEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:arcane_lift", ArcaneLiftEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:dispel_magic", DispelMagicEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:command", ExecuteCommandEffect.CODEC);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ItemValuesLoader());

        MagisteriumGameRules.initialize();
        MagisteriumCommands.initialize();

        UseBlockCallback.EVENT.register(Magisterium::interact);
        LootTableEvents.MODIFY.register(Magisterium::modifyLootTable);

        PayloadTypeRegistry.playS2C().register(SpellParticlePayload.ID, SpellParticlePayload.CODEC);

        PayloadTypeRegistry.playC2S().register(StartUtterancePayload.ID, StartUtterancePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StopUtterancePayload.ID, StopUtterancePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UseBookmarkPayload.ID, UseBookmarkPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SlotLayoutPayload.ID, SlotLayoutPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(StartUtterancePayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof SpellBookScreenHandler handler) {
                handler.startUtterance(payload.id(), context.player());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(StopUtterancePayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof SpellBookScreenHandler handler) {
                handler.stopUtterance();
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(SlotLayoutPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof SpellBookScreenHandler handler) {
                handler.applySlotProperties(payload.layout());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(UseBookmarkPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof SpellBookScreenHandler handler) {
                handler.currentPage.set(payload.page());
            }
        });
    }

    private static ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        var pos = hitResult.getBlockPos();
        if (player.isSpectator()) {
            return ActionResult.PASS;
        }

        var stack = player.getStackInHand(hand);
        var state = world.getBlockState(pos);
        var be = world.getBlockEntity(pos);

        if (state.getBlock() instanceof LecternBlock && be instanceof LecternBlockEntity lectern) {
            var book = lectern.getBook();
            if (book.isEmpty() && stack.isOf(SpellBookItem.INSTANCE)) {
                if (MagisteriumPlaygrounds.canModifyWorld(world, pos, player)) {
                    return LecternBlock.putBookIfAbsent(player, world, pos, state, stack) ? ActionResult.SUCCESS : ActionResult.PASS;
                }
                return ActionResult.PASS;
            } else if (book.isOf(SpellBookItem.INSTANCE)) {
                if (player.isSneaking() && MagisteriumPlaygrounds.canModifyWorld(world, pos, player)) {
                    lectern.setBook(ItemStack.EMPTY);
                    LecternBlock.setHasBook(player, world, pos, state, false);
                    if (!player.getInventory().insertStack(book)) {
                        player.dropItem(book, false);
                    }
                    return ActionResult.SUCCESS;
                }
                player.openHandledScreen(new NamedScreenHandlerFactory() {
                    @Override
                    public ScreenHandler createMenu(int syncId, PlayerInventory playerInv, PlayerEntity player1) {
                        return new SpellBookScreenHandler(syncId, playerInv, new SpellBookScreenHandler.LecternContext(world, pos, book));
                    }

                    @Override
                    public Text getDisplayName() {
                        return book.getName();
                    }
                });
                return ActionResult.SUCCESS;
            }
        }

        if (stack.isOf(Items.LAPIS_LAZULI)) {
            if (tryPlaceGlyph(player, world, hand, hitResult, stack)) {
                if (!world.isClient && !player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    private static boolean tryPlaceGlyph(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult, ItemStack stack) {
        var context = new ItemPlacementContext(player, hand, stack, hitResult);
        if (!context.canPlace()) {
            return false;
        }
        var placementState = GlyphBlock.INSTANCE.getPlacementState(context);
        var placementPos = context.getBlockPos();
        if (!placementState.canPlaceAt(world, placementPos)) {
            return false;
        }
        if (!world.isClient) {
            if (!MagisteriumPlaygrounds.trySetBlockState(world, placementPos, placementState, player)) {
                player.sendMessage(Text.translatable("magisterium.gui.no_success"), true);
                return false;
            }
        }
        var sounds = placementState.getSoundGroup();
        world.playSound(player, placementPos, sounds.getPlaceSound(), SoundCategory.BLOCKS, (sounds.getVolume() + 1.0F) / 2.0F, sounds.getPitch());
        world.emitGameEvent(player, GameEvent.BLOCK_PLACE, placementPos);

        return true;
    }

    public static final RegistryKey<LootTable> COMMON_STRUCTURES_LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, //
            Identifier.of("magisterium:chests/parts/common_loot"));
    public static final RegistryKey<LootTable> RARE_STRUCTURES_LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, //
            Identifier.of("magisterium:chests/parts/rare_loot"));

    public static final Set<Identifier> STRUCTURES_TO_SPAWN_COMMON_LOOT = ImmutableSet.<Identifier>builder() //
            .add(Identifier.ofVanilla("chests/simple_dungeon")) //
            .add(Identifier.ofVanilla("chests/desert_pyramid")) //
            .add(Identifier.ofVanilla("chests/jungle_temple")) //
            .add(Identifier.ofVanilla("chests/stronghold_corridor")) //
            .add(Identifier.ofVanilla("chests/stronghold_crossing")) //
            .add(Identifier.ofVanilla("chests/stronghold_library")) //
            .build();

    public static final Set<Identifier> STRUCTURES_TO_SPAWN_RARE_LOOT = ImmutableSet.<Identifier>builder() //
            .add(Identifier.ofVanilla("chests/desert_pyramid")) //
            .add(Identifier.ofVanilla("chests/jungle_temple")) //
            .add(Identifier.ofVanilla("chests/stronghold_library")) //
            .build();

    private static void modifyLootTable(RegistryKey<LootTable> key, LootTable.Builder tableBuilder, LootTableSource source, RegistryWrapper.WrapperLookup registries) {
        if (STRUCTURES_TO_SPAWN_COMMON_LOOT.contains(key.getValue())) {
            tableBuilder.pool(LootPool.builder() //
                    .with(LootTableEntry.builder(COMMON_STRUCTURES_LOOT)) //
                    .rolls(UniformLootNumberProvider.create(0, 2)));
        }
        if (STRUCTURES_TO_SPAWN_RARE_LOOT.contains(key.getValue())) {
            tableBuilder.pool(LootPool.builder() //
                    .with(LootTableEntry.builder(RARE_STRUCTURES_LOOT)) //
                    .rolls(UniformLootNumberProvider.create(0, 1)));
        }
    }
}