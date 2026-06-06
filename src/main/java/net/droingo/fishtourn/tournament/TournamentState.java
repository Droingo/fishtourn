package net.droingo.fishtourn.tournament;

import net.droingo.fishtourn.FishingTournament;
import net.minecraft.entity.player.PlayerEntity;
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
    public static final int MAX_SUBMISSIONS_PER_PLAYER = 5;

    public boolean active = false;
    public long endWorldTime = 0L;

    public final Map<UUID, TournamentEntry> bestEntries = new HashMap<>();
    public final Map<UUID, Integer> submissionCounts = new HashMap<>();

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

        return overworld.getPersistentStateManager().getOrCreate(TYPE, STATE_ID);
    }

    public boolean canSubmitFish(PlayerEntity player) {
        return getSubmissionCount(player) < MAX_SUBMISSIONS_PER_PLAYER;
    }

    public int getSubmissionCount(PlayerEntity player) {
        return submissionCounts.getOrDefault(player.getUuid(), 0);
    }

    public int getRemainingSubmissions(PlayerEntity player) {
        return Math.max(0, MAX_SUBMISSIONS_PER_PLAYER - getSubmissionCount(player));
    }

    public void recordSubmission(PlayerEntity player) {
        UUID playerUuid = player.getUuid();
        int currentCount = submissionCounts.getOrDefault(playerUuid, 0);

        submissionCounts.put(playerUuid, currentCount + 1);
        markDirty();
    }

    public void clearSubmissionCounts() {
        submissionCounts.clear();
        markDirty();
    }

    public void resetTournament() {
        active = false;
        endWorldTime = 0L;
        bestEntries.clear();
        submissionCounts.clear();
        markDirty();
    }

    public void startTournament(long endWorldTime) {
        this.active = true;
        this.endWorldTime = endWorldTime;

        bestEntries.clear();
        submissionCounts.clear();

        markDirty();
    }

    public void stopTournament() {
        this.active = false;
        this.endWorldTime = 0L;

        markDirty();
    }

    public void putBestEntry(UUID playerUuid, TournamentEntry entry) {
        bestEntries.put(playerUuid, entry);
        markDirty();
    }

    public static TournamentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        TournamentState state = new TournamentState();

        state.active = nbt.getBoolean("Active");
        state.endWorldTime = nbt.getLong("EndWorldTime");

        NbtList entriesList = nbt.getList("BestEntries", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < entriesList.size(); i++) {
            NbtCompound entryNbt = entriesList.getCompound(i);

            UUID playerUuid = entryNbt.getUuid("PlayerUuid");
            String playerName = entryNbt.getString("PlayerName");
            String species = entryNbt.getString("Species");
            String rarity = entryNbt.getString("Rarity");
            String catchZone = entryNbt.contains("CatchZone")
                    ? entryNbt.getString("CatchZone")
                    : "Open Water";
            double lengthCm = entryNbt.getDouble("LengthCm");
            double weightKg = entryNbt.getDouble("WeightKg");
            int score = entryNbt.getInt("Score");

            TournamentEntry entry = new TournamentEntry(
                    playerUuid,
                    playerName,
                    species,
                    rarity,
                    catchZone,
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
            entryNbt.putString("CatchZone", entry.catchZone());
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