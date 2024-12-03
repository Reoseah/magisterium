package io.github.reoseah.magisterium.world.state;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.github.reoseah.magisterium.block.SpellActivityListener;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class ActiveSpellTracker extends PersistentState {
    public static final String ID = "magisterium:active_spells";
    private static final Logger LOGGER = LogManager.getLogger();

    public final ServerWorld world;

    private final Multimap<UUID, BlockPos> spellEndListeners;
    private boolean firstTick;

    private ActiveSpellTracker(ServerWorld world) {
        this.world = world;
        this.spellEndListeners = ArrayListMultimap.create();
    }

    private ActiveSpellTracker(ServerWorld world, NbtCompound data) {
        this(world);
        this.firstTick = true;

        var listenersNbt = data.getList("spell_end_listeners", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < listenersNbt.size(); i++) {
            var listenerNbt = listenersNbt.getCompound(i);
            var source = listenerNbt.getUuid("player");
            var pos = BlockPos.fromLong(listenerNbt.getLong("pos"));
            this.spellEndListeners.put(source, pos);
        }
    }

    public static ActiveSpellTracker get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(getType(world), ID);
    }

    private static PersistentState.Type<ActiveSpellTracker> getType(ServerWorld world) {
        return new Type<>(() -> new ActiveSpellTracker(world), (nbt, registryLookup) -> new ActiveSpellTracker(world, nbt), null);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        var listenersNbt = nbt.getList("spell_end_listeners", NbtElement.COMPOUND_TYPE);
        for (var entry : this.spellEndListeners.entries()) {
            var listenerNbt = new NbtCompound();
            listenerNbt.putUuid("player", entry.getKey());
            listenerNbt.putLong("pos", entry.getValue().asLong());
            listenersNbt.add(listenerNbt);
        }

        return nbt;
    }

    public void onSpellStart(ServerPlayerEntity player, BlockPos origin) {
        for (var pos : BlockPos.iterate(origin.add(16, 16, 16), origin.add(-16, -16, -16))) {
            if (this.world.getBlockState(pos).getBlock() instanceof SpellActivityListener listener) {
                if (listener.onSpellStart(this.world, pos, player.getUuid())) {
                    this.spellEndListeners.put(player.getUuid(), pos.toImmutable());
                }
            }
        }
    }

    public void onSpellEnd(ServerPlayerEntity player) {
        this.spellEndListeners.get(player.getUuid()).removeIf(listener -> {
            if (this.world.getBlockState(listener).getBlock() instanceof SpellActivityListener spellListener) {
                return spellListener.onSpellEnd(this.world, listener, player.getUuid());
            }
            return false;
        });
    }

    public void onTick() {
        // maybe chunk load event would be better?
        if (this.firstTick) {
            this.firstTick = false;
            for (var entry : this.spellEndListeners.entries()) {
                if (this.world.getBlockState(entry.getValue()).getBlock() instanceof SpellActivityListener listener) {
                    listener.onSpellEnd(this.world, entry.getValue(), entry.getKey());
                }
            }
            this.spellEndListeners.clear();
        }
    }
}
