package io.github.reoseah.magisterium.world;

import io.github.reoseah.magisterium.MagisteriumGameRules;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Limits world-modifying spells to specially designated areas,
 * specially for our booth(s) in ModFest showcase world.
 */
public class MagisteriumPlaygrounds extends PersistentState {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ID = "MagisteriumPlaygrounds";

    public final ServerWorld world;
    //    private final List<Pair<String, BlockBox>> playgrounds = new ArrayList<>();
    private final Map<String, BlockBox> playgrounds = new HashMap<>();

    public static MagisteriumPlaygrounds get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(getType(world), ID);
    }

    private static PersistentState.Type<MagisteriumPlaygrounds> getType(ServerWorld world) {
        return new Type<>(() -> new MagisteriumPlaygrounds(world), (nbt, registryLookup) -> new MagisteriumPlaygrounds(world, nbt), null);
    }

    public static boolean isInsidePlayground(ServerWorld world, BlockPos pos) {
        var playgrounds = get(world).playgrounds;
        for (var playground : playgrounds.values()) {
            if (playground.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canModifyWorld(World world, BlockPos pos, PlayerEntity player) {
        if (world instanceof ServerWorld) {
            if (world.getGameRules().getBoolean(MagisteriumGameRules.ENABLE_MAGISTERIUM_PLAYGROUNDS) //
                    && !player.isCreative()) {
                return isInsidePlayground((ServerWorld) world, pos);
            }
        }
        return player.canModifyBlocks() && player.canModifyAt(world, pos);
    }

    public static boolean trySetBlockState(ServerWorld world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (canModifyWorld(world, pos, player)) {
            return world.setBlockState(pos, state);
        }
        return false;
    }

    private MagisteriumPlaygrounds(ServerWorld world) {
        this.world = world;
    }

    private MagisteriumPlaygrounds(ServerWorld world, NbtCompound data) {
        this(world);

        var playgroundNbt = data.getList("playgrounds", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < playgroundNbt.size(); i++) {
            var playground = playgroundNbt.getCompound(i);
            this.playgrounds.put( //
                    playground.getString("id"), //
                    new BlockBox( //
                            playground.getIntArray("box")[0], playground.getIntArray("box")[1], playground.getIntArray("box")[2], //
                            playground.getIntArray("box")[3], playground.getIntArray("box")[4], playground.getIntArray("box")[5] //
                    ) //
            );
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        var playgroundsNbt = new NbtList();
        for (var entry : this.playgrounds.entrySet()) {
            var playgroundNbt = new NbtCompound();
            playgroundNbt.putString("id", entry.getKey());
            playgroundNbt.putIntArray("box", new int[]{ //
                    entry.getValue().getMinX(), entry.getValue().getMinY(), entry.getValue().getMinZ(), //
                    entry.getValue().getMaxX(), entry.getValue().getMaxY(), entry.getValue().getMaxZ() //
            });
            playgroundsNbt.add(playgroundNbt);
        }
        nbt.put("playgrounds", playgroundsNbt);

        return nbt;
    }

    public Map<String, BlockBox> getPlaygrounds() {
        return this.playgrounds;
    }
}
