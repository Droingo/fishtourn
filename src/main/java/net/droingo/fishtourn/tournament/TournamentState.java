package net.droingo.fishtourn.tournament;

import net.droingo.fishtourn.FishingTournament;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TournamentState extends PersistentState {
    private static final String STATE_ID = FishingTournament.MOD_ID + "_tournament";

    public boolean active = false;
    public final Map<UUID, TournamentEntry> bestEntries = new HashMap<>();
    public final Map<UUID, Integer> submissionCounts = new HashMap<>();
    public long endWorldTime = 0L;

    private static final Type<TournamentState> TYPE = new Type<>(
            TournamentState::new,
            TournamentState::fromNbt,
            null
    );

    public static TournamentState get(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);

        if (overworld == null) {
            throw new IllegalStateException("Overworld is not available.");
        }

        TournamentState state = overworld.getPersistentStateManager().getOrCreate(TYPE, STATE_ID);
        state.markDirty();
        return state;
    }

    public static TournamentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        TournamentState state = new TournamentState();

        state.endWorldTime = nbt.getLong("EndWorldTime");

        state.active = nbt.getBoolean("Active");

        NbtList entriesList = nbt.getList("BestEntries", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < entriesList.size(); i++) {
            NbtCompound entryNbt = entriesList.getCompound(i);

            UUID playerUuid = entryNbt.getUuid("PlayerUuid");
            String playerName = entryNbt.getString("PlayerName");
            String species = entryNbt.getString("Species");
            String rarity = entryNbt.getString("Rarity");
            double lengthCm = entryNbt.getDouble("LengthCm");
            double weightKg = entryNbt.getDouble("WeightKg");
            int score = entryNbt.getInt("Score");

            TournamentEntry entry = new TournamentEntry(
                    playerUuid,
                    playerName,
                    species,
                    rarity,
                    lengthCm,
                    weightKg,
                    score
            );

            state.bestEntries.put(playerUuid, entry);
        }

        NbtList countsList = nbt.getList("SubmissionCounts", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < countsList.size(); i++) {
            NbtCompound countNbt = countsList.getCompound(i);

            UUID playerUuid = countNbt.getUuid("PlayerUuid");
            int count = countNbt.getInt("Count");

            state.submissionCounts.put(playerUuid, count);
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putBoolean("Active", active);
        nbt.putLong("EndWorldTime", endWorldTime);

        NbtList entriesList = new NbtList();

        for (TournamentEntry entry : bestEntries.values()) {
            NbtCompound entryNbt = new NbtCompound();

            entryNbt.putUuid("PlayerUuid", entry.playerUuid());
            entryNbt.putString("PlayerName", entry.playerName());
            entryNbt.putString("Species", entry.species());
            entryNbt.putString("Rarity", entry.rarity());
            entryNbt.putDouble("LengthCm", entry.lengthCm());
            entryNbt.putDouble("WeightKg", entry.weightKg());
            entryNbt.putInt("Score", entry.score());

            entriesList.add(entryNbt);
        }

        nbt.put("BestEntries", entriesList);

        NbtList countsList = new NbtList();

        for (Map.Entry<UUID, Integer> countEntry : submissionCounts.entrySet()) {
            NbtCompound countNbt = new NbtCompound();

            countNbt.putUuid("PlayerUuid", countEntry.getKey());
            countNbt.putInt("Count", countEntry.getValue());

            countsList.add(countNbt);
        }

        nbt.put("SubmissionCounts", countsList);

        return nbt;
    }
}