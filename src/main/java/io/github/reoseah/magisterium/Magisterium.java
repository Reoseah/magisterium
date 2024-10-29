package io.github.reoseah.magisterium;

import io.github.reoseah.magisterium.block.ArcaneTableBlock;
import io.github.reoseah.magisterium.block.GlyphBlock;
import io.github.reoseah.magisterium.block.IllusoryWallBlock;
import io.github.reoseah.magisterium.block.IllusoryWallBlockEntity;
import io.github.reoseah.magisterium.item.BookmarkItem;
import io.github.reoseah.magisterium.item.SpellBookItem;
import io.github.reoseah.magisterium.item.SpellPageItem;
import io.github.reoseah.magisterium.network.SlotLayoutPayload;
import io.github.reoseah.magisterium.network.StartUtterancePayload;
import io.github.reoseah.magisterium.network.StopUtterancePayload;
import io.github.reoseah.magisterium.network.UseBookmarkPayload;
import io.github.reoseah.magisterium.recipe.*;
import io.github.reoseah.magisterium.screen.ArcaneTableScreenHandler;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Magisterium implements ModInitializer {
    public static final String MOD_ID = "magisterium";

    public static final Logger LOGGER = LoggerFactory.getLogger(Magisterium.class);

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK, "magisterium:arcane_table", ArcaneTableBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magisterium:glyph", GlyphBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magisterium:illusory_wall", IllusoryWallBlock.INSTANCE);

        Registry.register(Registries.BLOCK_ENTITY_TYPE, "magisterium:illusory_wall", IllusoryWallBlockEntity.TYPE);

        Registry.register(Registries.ITEM, "magisterium:arcane_table", new BlockItem(ArcaneTableBlock.INSTANCE, new Item.Settings()));
        Registry.register(Registries.ITEM, "magisterium:spell_book", SpellBookItem.INSTANCE);
        Registry.register(Registries.ITEM, "magisterium:spell_page", SpellPageItem.INSTANCE);
        Registry.register(Registries.ITEM, "magisterium:bookmark", BookmarkItem.INSTANCE);

        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:current_page", SpellBookItem.CURRENT_PAGE);
        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:page_data", SpellBookItem.PAGES);
        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:unstable_charge", SpellBookItem.UNSTABLE_CHARGE);
        Registry.register(Registries.DATA_COMPONENT_TYPE, "magisterium:spell", SpellPageItem.SPELL);

        var group = FabricItemGroup.builder() //
                .icon(SpellBookItem.INSTANCE::getDefaultStack) //
                .displayName(Text.translatable("itemGroup.magisterium")) //
                .entries((displayContext, entries) -> {
                    entries.add(ArcaneTableBlock.INSTANCE);
                    entries.add(SpellBookItem.createTestBook());
                    entries.add(SpellPageItem.createSpellPage(Identifier.of("magisterium:awaken_the_flame")));
                    entries.add(SpellPageItem.createSpellPage(Identifier.of("magisterium:quench_the_flame")));
                    entries.add(SpellPageItem.createSpellPage(Identifier.of("magisterium:glyphic_ignition")));
                    entries.add(SpellPageItem.createSpellPage(Identifier.of("magisterium:conflagrate")));
                    entries.add(SpellPageItem.createSpellPage(Identifier.of("magisterium:illusory_wall")));
                    entries.add(SpellPageItem.createSpellPage(Identifier.of("magisterium:unstable_charge")));
                    entries.add(BookmarkItem.INSTANCE);
                }) //
                .build();
        Registry.register(Registries.ITEM_GROUP, "magisterium", group);

        // TODO if spell data is server loaded, won't need these recipes anymore
        //      currently, they load parts of the spell data that the server needs to know
        //      and they have to match the client data...
        Registry.register(Registries.RECIPE_TYPE, "magisterium:spell_book", SpellBookRecipe.TYPE);

        Registry.register(Registries.RECIPE_SERIALIZER, "magisterium:spell_crafting", SpellBookCraftingRecipe.Serializer.INSTANCE);
        Registry.register(Registries.RECIPE_SERIALIZER, "magisterium:awaken_the_flame", AwakenFlameRecipe.SERIALIZER);
        Registry.register(Registries.RECIPE_SERIALIZER, "magisterium:quench_the_flame", QuenchFlameRecipe.SERIALIZER);
        Registry.register(Registries.RECIPE_SERIALIZER, "magisterium:glyphic_ignition", GlyphicIgnitionRecipe.SERIALIZER);
        Registry.register(Registries.RECIPE_SERIALIZER, "magisterium:conflagrate", ConflagrateRecipe.SERIALIZER);
        Registry.register(Registries.RECIPE_SERIALIZER, "magisterium:illusory_wall", IllusoryWallRecipe.SERIALIZER);
        Registry.register(Registries.RECIPE_SERIALIZER, "magisterium:unstable_charge", UnstableChargeRecipe.SERIALIZER);

        Registry.register(Registries.SCREEN_HANDLER, "magisterium:spell_book", SpellBookScreenHandler.TYPE);
        Registry.register(Registries.SCREEN_HANDLER, "magisterium:arcane_table", ArcaneTableScreenHandler.TYPE);

        MagisteriumGameRules.initialize();
        MagisteriumCommands.initialize();

        UseBlockCallback.EVENT.register(Magisterium::interact);

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
            if (context.player().currentScreenHandler instanceof SpellBookScreenHandler hemonomiconScreen) {
                hemonomiconScreen.currentPage.set(payload.page());
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

        if (state.getBlock() instanceof LecternBlock
                && be instanceof LecternBlockEntity lectern) {
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
        world.playSound(player, placementPos, sounds.getPlaceSound(), SoundCategory.BLOCKS, (sounds.getVolume() + 1.0F) / 2.0F,
                sounds.getPitch());
        world.emitGameEvent(player, GameEvent.BLOCK_PLACE, placementPos);

        return true;
    }

}