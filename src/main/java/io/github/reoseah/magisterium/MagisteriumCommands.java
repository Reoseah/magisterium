package io.github.reoseah.magisterium;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.reoseah.magisterium.world.MagisteriumPlaygrounds;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;

public class MagisteriumCommands {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("magisterium") //
                    .requires(source -> source.hasPermissionLevel(1)) //
                    .then(CommandManager.literal("playgrounds") //
                            .then(CommandManager.literal("list") //
                                    .executes(ctx -> {
                                        var world = ctx.getSource().getWorld();

                                        var playgrounds = MagisteriumPlaygrounds.get(world);

                                        var names = playgrounds.getPlaygrounds().entrySet().stream().map(e -> e.getKey() + " (" + e.getValue().getMinX() + ", " + e.getValue().getMinY() + ", " + e.getValue().getMinZ() + " - " + e.getValue().getMaxX() + ", " + e.getValue().getMaxY() + ", " + e.getValue().getMaxZ() + ")").reduce((a, b) -> a + ",\n" + b).orElse("");

                                        ctx.getSource().sendFeedback(() -> Text.translatable("commands.magisterium.playgrounds.list.success", names), false);
                                        return 0;
                                    })) //
                            .then(CommandManager.literal("get") //
                                    .then(CommandManager.argument("name", StringArgumentType.word()) //
                                            .executes(ctx -> {
                                                var name = StringArgumentType.getString(ctx, "name");
                                                var world = ctx.getSource().getWorld();

                                                var playgrounds = MagisteriumPlaygrounds.get(world);

                                                var playground = playgrounds.getPlaygrounds().get(name);
                                                if (playground == null) {
                                                    ctx.getSource().sendFeedback(() -> Text.translatable("commands.magisterium.playgrounds.get.not_found", name), false);
                                                    return 0;
                                                }

                                                ctx.getSource().sendFeedback(() -> Text.translatable("commands.magisterium.playgrounds.get.success", name, playground.getMinX(), playground.getMinY(), playground.getMinZ(), playground.getMaxX(), playground.getMaxY(), playground.getMaxZ()), false);
                                                return 0;
                                            }))) //
                            .then(CommandManager.literal("delete") //
                                    .then(CommandManager.argument("name", StringArgumentType.word()) //
                                            .executes(ctx -> {
                                                var name = StringArgumentType.getString(ctx, "name");
                                                var world = ctx.getSource().getWorld();

                                                var playgrounds = MagisteriumPlaygrounds.get(world);

                                                if (playgrounds.getPlaygrounds().remove(name) == null) {
                                                    ctx.getSource().sendFeedback(() -> Text.translatable("commands.magisterium.playgrounds.delete.not_found", name), false);
                                                    return 0;
                                                }

                                                ctx.getSource().sendFeedback(() -> Text.translatable("commands.magisterium.playgrounds.delete.success", name), false);
                                                playgrounds.markDirty();

                                                return 0;
                                            }))) //
                            .then(CommandManager.literal("set") //
                                    .then(CommandManager.argument("name", StringArgumentType.word()) //
                                            .then(CommandManager.argument("pos1", BlockPosArgumentType.blockPos()) //
                                                    .then(CommandManager.argument("pos2", BlockPosArgumentType.blockPos()) //
                                                            .executes(ctx -> {
                                                                var name = StringArgumentType.getString(ctx, "name");
                                                                var pos1 = BlockPosArgumentType.getBlockPos(ctx, "pos1");
                                                                var pos2 = BlockPosArgumentType.getBlockPos(ctx, "pos2");
                                                                var world = ctx.getSource().getWorld();

                                                                var playgrounds = MagisteriumPlaygrounds.get(world);
                                                                playgrounds.getPlaygrounds().put(name, BlockBox.create(pos1, pos2));
                                                                playgrounds.markDirty();

                                                                ctx.getSource().sendFeedback(() -> Text.translatable("commands.magisterium.playgrounds.set.success", name, pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ()), false);

                                                                return 0;
                                                            })))))));
        });
    }
}
