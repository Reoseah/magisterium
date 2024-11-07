package io.github.reoseah.magisterium.data.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class ExecuteCommandEffect extends SpellEffect {
    public static final MapCodec<ExecuteCommandEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("utterance").forGetter(effect -> effect.utterance),
            Codec.INT.fieldOf("duration").forGetter(effect -> effect.duration),
            Codec.STRING.fieldOf("command").forGetter(effect -> effect.command)
    ).apply(instance, ExecuteCommandEffect::new));

    public final String command;

    public ExecuteCommandEffect(Identifier utterance, int duration, String command) {
        super(utterance, duration);
        this.command = command;
    }

    @Override
    public MapCodec<? extends SpellEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void finish(SpellEffectContext input, RegistryWrapper.WrapperLookup lookup) {
        var formattedCommand = this.getFormattedCommand(input);

        var server = ((ServerWorld) (input.getPlayer().getWorld())).getServer();
        var commandSource = server.getCommandSource();
        var parsedCommand = server.getCommandSource().getDispatcher().parse(formattedCommand, commandSource);
        server.getCommandManager().execute(parsedCommand, formattedCommand);
    }

    private @NotNull String getFormattedCommand(SpellEffectContext input) {
        var formattedCommand = this.command.replace("{player}", input.getPlayer().getName().getString()) //
                .replace("{player_pos}", input.getPlayer().getBlockPos().getX() + " " + input.getPlayer().getBlockPos().getY() + " " + input.getPlayer().getBlockPos().getZ());
        formattedCommand = "execute as " + input.getPlayer().getName().getString() + //
                " in " + input.getPlayer().getWorld().getDimensionEntry().getIdAsString() + //
                " run " + formattedCommand;
        return formattedCommand;
    }
}
