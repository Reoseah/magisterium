package io.github.reoseah.magisterium;

import com.google.common.collect.ImmutableSet;
import io.github.reoseah.magisterium.block.*;
import io.github.reoseah.magisterium.block.entity.ArcaneDetectorBlockEntity;
import io.github.reoseah.magisterium.block.entity.IllusoryWallBlockEntity;
import io.github.reoseah.magisterium.block.entity.MagicBarrierBlockEntity;
import io.github.reoseah.magisterium.data.BookLoader;
import io.github.reoseah.magisterium.data.SpellEffectLoader;
import io.github.reoseah.magisterium.data.SpellPageLoader;
import io.github.reoseah.magisterium.data.effect.*;
import io.github.reoseah.magisterium.data.element.*;
import io.github.reoseah.magisterium.item.*;
import io.github.reoseah.magisterium.network.c2s.SpellBookScreenStatePayload;
import io.github.reoseah.magisterium.network.c2s.StartSpellPayload;
import io.github.reoseah.magisterium.network.c2s.StopSpellPayload;
import io.github.reoseah.magisterium.network.c2s.UseBookmarkPayload;
import io.github.reoseah.magisterium.network.s2c.FinishSpellPayload;
import io.github.reoseah.magisterium.network.s2c.SpellParticlePayload;
import io.github.reoseah.magisterium.network.s2c.SyncronizeBookDataPayload;
import io.github.reoseah.magisterium.network.s2c.SyncronizePageDataPayload;
import io.github.reoseah.magisterium.particle.MagisteriumParticles;
import io.github.reoseah.magisterium.screen.ArcaneTableScreenHandler;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import io.github.reoseah.magisterium.world.state.ActiveSpellTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.*;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
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
        Registry.register(Registries.BLOCK, "magisterium:magic_barrier", MagicBarrierBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magisterium:arcane_detector", ArcaneDetectorBlock.INSTANCE);

        Registry.register(Registries.BLOCK_ENTITY_TYPE, "magisterium:illusory_wall", IllusoryWallBlockEntity.TYPE);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, "magisterium:magic_barrier", MagicBarrierBlockEntity.TYPE);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, "magisterium:arcane_detector", ArcaneDetectorBlockEntity.TYPE);

        Registry.register(Registries.ITEM, "magisterium:arcane_table", ArcaneTableBlock.ITEM);
        Registry.register(Registries.ITEM, "magisterium:arcane_detector", ArcaneDetectorBlock.ITEM);
        Registry.register(Registries.ITEM, "magisterium:spell_book", SpellBookItem.SPELL_BOOK);
        Registry.register(Registries.ITEM, "magisterium:elements_of_pyromancy", SpellBookItem.ELEMENTS_OF_PYROMANCY);
        Registry.register(Registries.ITEM, "magisterium:lesser_arcanum", SpellBookItem.LESSER_ARCANUM);
        Registry.register(Registries.ITEM, "magisterium:awaken_the_flame_page", PageItem.AWAKEN_THE_FLAME);
        Registry.register(Registries.ITEM, "magisterium:quench_the_flame_page", PageItem.QUENCH_THE_FLAME);
        Registry.register(Registries.ITEM, "magisterium:glyphic_ignition_page", PageItem.GLYPHIC_IGNITION);
        Registry.register(Registries.ITEM, "magisterium:conflagrate_page", PageItem.CONFLAGRATE);
        Registry.register(Registries.ITEM, "magisterium:cold_snap_page", PageItem.COLD_SNAP);
        Registry.register(Registries.ITEM, "magisterium:illusory_wall_page", PageItem.ILLUSORY_WALL);
        Registry.register(Registries.ITEM, "magisterium:arcane_lift_page", PageItem.ARCANE_LIFT);
        Registry.register(Registries.ITEM, "magisterium:magic_barrier_page", PageItem.MAGIC_BARRIER);
        Registry.register(Registries.ITEM, "magisterium:dispel_magic_page", PageItem.DISPEL_MAGIC);
        Registry.register(Registries.ITEM, "magisterium:elements_of_pyromancy_pages", PageItem.ELEMENTS_OF_PYROMANCY);
        Registry.register(Registries.ITEM, "magisterium:lesser_arcanum_pages", PageItem.LESSER_ARCANUM);
        Registry.register(Registries.ITEM, "magisterium:bookmark", BookmarkItem.INSTANCE);
        Registry.register(Registries.ITEM, "magisterium:blaze_rune", RuneItem.BLAZE);
        Registry.register(Registries.ITEM, "magisterium:wind_rune", RuneItem.WIND);
        Registry.register(Registries.ITEM, "magisterium:blaze_blade", BlazeBladeItem.INSTANCE);

        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:current_page", SpellBookItem.CURRENT_PAGE);
        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:contents", SpellBookItem.CONTENTS);
        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:charge", RuneItem.CHARGE);
        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:last_tick", BlazeBladeItem.LAST_TICK);

        var group = FabricItemGroup.builder() //
                .icon(SpellBookItem.SPELL_BOOK::getDefaultStack) //
                .displayName(Text.translatable("itemGroup.magisterium")) //
                .entries((displayContext, entries) -> {
                    entries.add(ArcaneTableBlock.INSTANCE);
                    entries.add(ArcaneDetectorBlock.INSTANCE);
                    entries.add(SpellBookItem.SPELL_BOOK);

                    var filledBook = new ItemStack(SpellBookItem.SPELL_BOOK);
                    filledBook.set(SpellBookItem.CONTENTS, Util.make(DefaultedList.ofSize(18, ItemStack.EMPTY), list -> {
                        list.set(0, BookmarkItem.INSTANCE.getDefaultStack());
                        list.set(1, PageItem.AWAKEN_THE_FLAME.getDefaultStack());
                        list.set(2, PageItem.QUENCH_THE_FLAME.getDefaultStack());
                        list.set(3, PageItem.GLYPHIC_IGNITION.getDefaultStack());
                        list.set(4, PageItem.CONFLAGRATE.getDefaultStack());
                        list.set(5, PageItem.COLD_SNAP.getDefaultStack());
                        list.set(6, PageItem.ILLUSORY_WALL.getDefaultStack());
                        list.set(7, PageItem.ARCANE_LIFT.getDefaultStack());
                        list.set(8, PageItem.MAGIC_BARRIER.getDefaultStack());
                        list.set(9, PageItem.DISPEL_MAGIC.getDefaultStack());
                    }));
                    entries.add(filledBook);

                    entries.add(SpellBookItem.ELEMENTS_OF_PYROMANCY);
                    entries.add(SpellBookItem.LESSER_ARCANUM);

                    entries.add(PageItem.AWAKEN_THE_FLAME);
                    entries.add(PageItem.QUENCH_THE_FLAME);
                    entries.add(PageItem.GLYPHIC_IGNITION);
                    entries.add(PageItem.CONFLAGRATE);
                    entries.add(PageItem.COLD_SNAP);
                    entries.add(PageItem.ILLUSORY_WALL);
                    entries.add(PageItem.ARCANE_LIFT);
                    entries.add(PageItem.MAGIC_BARRIER);
                    entries.add(PageItem.DISPEL_MAGIC);
                    entries.add(PageItem.ELEMENTS_OF_PYROMANCY);
                    entries.add(PageItem.LESSER_ARCANUM);
                    entries.add(BookmarkItem.INSTANCE);

                    entries.add(RuneItem.BLAZE);
                    var chargedBlazeRune = new ItemStack(RuneItem.BLAZE);
                    chargedBlazeRune.set(RuneItem.CHARGE, RuneItem.MAX_CHARGE);
                    entries.add(chargedBlazeRune);
                    entries.add(RuneItem.WIND);
                    var chargedWindRune = new ItemStack(RuneItem.WIND);
                    chargedWindRune.set(RuneItem.CHARGE, RuneItem.MAX_CHARGE);
                    entries.add(chargedWindRune);

                    entries.add(BlazeBladeItem.INSTANCE);
                }) //
                .build();
        Registry.register(Registries.ITEM_GROUP, "magisterium", group);

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
        Registry.register(Registries.PARTICLE_TYPE, "magisterium:barrier_spark", MagisteriumParticles.BARRIER_SPARK);
        Registry.register(Registries.PARTICLE_TYPE, "magisterium:barrier_energy", MagisteriumParticles.BARRIER_ENERGY);

        Registry.register(Registries.SOUND_EVENT, "magisterium:chant", MagisteriumSounds.CHANT);
        Registry.register(Registries.SOUND_EVENT, "magisterium:magic_hum", MagisteriumSounds.MAGIC_HUM);

        Registry.register(PageElement.REGISTRY, "magisterium:empty", EmptyPageElement.CODEC);
        Registry.register(PageElement.REGISTRY, "magisterium:heading", Heading.CODEC);
        Registry.register(PageElement.REGISTRY, "magisterium:paragraph", Paragraph.CODEC);
        Registry.register(PageElement.REGISTRY, "magisterium:page_break", PageBreak.CODEC);
        Registry.register(PageElement.REGISTRY, "magisterium:inventory", PageInventory.CODEC);
        Registry.register(PageElement.REGISTRY, "magisterium:spell", Spell.CODEC);
        Registry.register(PageElement.REGISTRY, "magisterium:fold", Fold.CODEC);
        Registry.register(PageElement.REGISTRY, "magisterium:bookmark", BookmarkPage.CODEC);
        Registry.register(PageElement.REGISTRY, "magisterium:title_page", TitlePage.CODEC);
        Registry.register(PageElement.REGISTRY, "magisterium:group", Group.CODEC);

        Registry.register(SpellEffect.REGISTRY, "magisterium:empty", EmptySpellEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:awaken_the_flame", AwakenFlameEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:quench_the_flame", QuenchFlameEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:glyphic_ignition", GlyphicIgnitionEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:conflagrate", ConflagrateEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:cold_snap", ColdSnapEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:illusory_wall", IllusoryWallEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:arcane_lift", ArcaneLiftEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:dispel_magic", DispelMagicEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:magic_barrier", MagicBarrierEffect.CODEC);
        Registry.register(SpellEffect.REGISTRY, "magisterium:command", ExecuteCommandEffect.CODEC);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(SpellPageLoader.ID, SpellPageLoader::new);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(SpellEffectLoader.ID, SpellEffectLoader::new);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(BookLoader.ID, BookLoader::new);

        UseBlockCallback.EVENT.register(Magisterium::interact);
        LootTableEvents.MODIFY.register(Magisterium::modifyLootTable);
        ServerTickEvents.END_WORLD_TICK.register(world -> ActiveSpellTracker.get(world).onTick());

        PayloadTypeRegistry.playS2C().register(SyncronizePageDataPayload.ID, SyncronizePageDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncronizeBookDataPayload.ID, SyncronizeBookDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FinishSpellPayload.ID, FinishSpellPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SpellParticlePayload.ID, SpellParticlePayload.CODEC);

        PayloadTypeRegistry.playC2S().register(SpellBookScreenStatePayload.ID, SpellBookScreenStatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StartSpellPayload.ID, StartSpellPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StopSpellPayload.ID, StopSpellPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UseBookmarkPayload.ID, UseBookmarkPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SpellBookScreenStatePayload.ID, SpellBookScreenStatePayload::receive);
        ServerPlayNetworking.registerGlobalReceiver(StartSpellPayload.ID, StartSpellPayload::receive);
        ServerPlayNetworking.registerGlobalReceiver(StopSpellPayload.ID, StopSpellPayload::receive);
        ServerPlayNetworking.registerGlobalReceiver(UseBookmarkPayload.ID, UseBookmarkPayload::receive);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var pages = SpellPageLoader.getInstance().pages;
            var pagePayload = new SyncronizePageDataPayload(pages);
            sender.sendPacket(pagePayload);

            var books = BookLoader.getInstance().books;
            var booksPayload = new SyncronizeBookDataPayload(books);
            sender.sendPacket(booksPayload);
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
            if (book.isEmpty() && stack.isOf(SpellBookItem.SPELL_BOOK)) {
                if (WorldUtil.canModifyWorld(world, pos, player)) {
                    return LecternBlock.putBookIfAbsent(player, world, pos, state, stack) ? ActionResult.SUCCESS : ActionResult.PASS;
                }
                return ActionResult.PASS;
            } else if (book.isOf(SpellBookItem.SPELL_BOOK)) {
                if (player.isSneaking() && WorldUtil.canModifyWorld(world, pos, player)) {
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
            if (!WorldUtil.trySetBlockState(world, placementPos, placementState, player)) {
                player.sendMessage(Text.translatable("magisterium.failed_all_world_changes"), true);
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