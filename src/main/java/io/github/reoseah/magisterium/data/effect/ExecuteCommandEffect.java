package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.screen.SpellBookScreenHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class ExecuteCommandEffect extends SpellEffect {
    public static final MapCodec<ExecuteCommandEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("duration").forGetter(effect -> effect.duration),
            Codec.STRING.fieldOf("command").forGetter(effect -> effect.command)
    ).apply(instance, ExecuteCommandEffect::new));

    public final String command;

    public ExecuteCommandEffect(int duration, String command) {
        super(duration);
        this.command = command;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(ServerPlayerEntity player, Inventory inventory, SpellBookScreenHandler.Context screenContext) {
        var formattedCommand = this.getFormattedCommand(player);

        var server = player.getServerWorld().getServer();
        var commandSource = server.getCommandSource();
        var parsedCommand = server.getCommandSource().getDispatcher().parse(formattedCommand, commandSource);
        server.getCommandManager().execute(parsedCommand, formattedCommand);
    }

    private @NotNull String getFormattedCommand(ServerPlayerEntity player) {
        var formattedCommand = this.command.replace("{player}", player.getName().getString()) //
                .replace("{player_pos}", player.getBlockPos().getX() + " " + player.getBlockPos().getY() + " " + player.getBlockPos().getZ());
        formattedCommand = "execute as " + player.getName().getString() + //
                " in " + player.getWorld().getDimensionEntry().getIdAsString() + //
                " run " + formattedCommand;
        return formattedCommand;
    }
}
