package net.droingo.fishtourn.tournament;

import net.droingo.fishtourn.component.FishDataComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class TournamentManager {
    private TournamentManager() {
    }

    public static boolean isActive(MinecraftServer server) {
        return TournamentState.get(server).active;
    }

    public static int getSubmissionCount(MinecraftServer server) {
        return TournamentState.get(server).submissionCounts.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public static int getUniqueSubmitterCount(MinecraftServer server) {
        return TournamentState.get(server).bestEntries.size();
    }

    public static int getPlayerSubmissionCount(MinecraftServer server, UUID playerUuid) {
        return TournamentState.get(server).submissionCounts.getOrDefault(playerUuid, 0);
    }

    public static int getPlayerRemainingSubmissionCount(MinecraftServer server, UUID playerUuid) {
        return Math.max(
                0,
                TournamentState.MAX_SUBMISSIONS_PER_PLAYER - getPlayerSubmissionCount(server, playerUuid)
        );
    }

    public static Optional<TournamentEntry> getPlayerBestEntry(MinecraftServer server, UUID playerUuid) {
        return Optional.ofNullable(TournamentState.get(server).bestEntries.get(playerUuid));
    }

    public static void start(MinecraftServer server) {
        start(server, 10);
    }

    public static void start(MinecraftServer server, int minutes) {
        TournamentState state = TournamentState.get(server);

        long durationTicks = minutes * 60L * 20L;
        long currentTime = server.getOverworld().getTime();

        state.active = true;
        state.endWorldTime = currentTime + durationTicks;

        state.warnedFiveMinutes = false;
        state.warnedOneMinute = false;
        state.warnedThirtySeconds = false;
        state.warnedTenSeconds = false;

        state.bestEntries.clear();
        state.submissionCounts.clear();

        state.markDirty();

        server.getPlayerManager().broadcast(
                Text.literal("Fishing tournament started! Time limit: " + minutes + " minutes.")
                        .formatted(Formatting.AQUA, Formatting.BOLD),
                false
        );

        server.getPlayerManager().broadcast(
                Text.literal("Submit your best fish at the Tournament Submission Barrel before time runs out.")
                        .formatted(Formatting.GRAY),
                false
        );

        server.getPlayerManager().broadcast(
                Text.literal("Each player may submit up to "
                                + TournamentState.MAX_SUBMISSIONS_PER_PLAYER
                                + " fish this round.")
                        .formatted(Formatting.GOLD),
                false
        );
    }

    public static void reset(MinecraftServer server) {
        TournamentState state = TournamentState.get(server);

        state.active = false;
        state.endWorldTime = 0L;

        state.warnedFiveMinutes = false;
        state.warnedOneMinute = false;
        state.warnedThirtySeconds = false;
        state.warnedTenSeconds = false;

        state.bestEntries.clear();
        state.submissionCounts.clear();

        state.markDirty();

        server.getPlayerManager().broadcast(
                Text.literal("Fishing tournament reset.")
                        .formatted(Formatting.YELLOW),
                false
        );
    }

    public static SubmissionResult submitFish(ServerPlayerEntity player, FishDataComponent fishData) {
        MinecraftServer server = player.getServer();

        if (server == null) {
            return SubmissionResult.notActive();
        }

        TournamentState state = TournamentState.get(server);

        if (!state.active) {
            return SubmissionResult.notActive();
        }

        UUID uuid = player.getUuid();

        int currentSubmissions = state.submissionCounts.getOrDefault(uuid, 0);

        if (currentSubmissions >= TournamentState.MAX_SUBMISSIONS_PER_PLAYER) {
            return SubmissionResult.reachedLimit();
        }

        state.submissionCounts.put(uuid, currentSubmissions + 1);

        TournamentEntry newEntry = new TournamentEntry(
                uuid,
                player.getName().getString(),
                fishData.species(),
                fishData.rarity(),
                fishData.catchZone(),
                fishData.lengthCm(),
                fishData.weightKg(),
                fishData.score()
        );

        TournamentEntry oldEntry = state.bestEntries.get(uuid);

        boolean newPersonalBest = oldEntry == null || newEntry.score() > oldEntry.score();

        if (newPersonalBest) {
            state.bestEntries.put(uuid, newEntry);
        }

        state.markDirty();

        int remainingSubmissions = Math.max(
                0,
                TournamentState.MAX_SUBMISSIONS_PER_PLAYER - currentSubmissions - 1
        );

        return SubmissionResult.accepted(newPersonalBest, remainingSubmissions);
    }

    public static List<TournamentEntry> getRankedEntries(MinecraftServer server) {
        return TournamentState.get(server).bestEntries.values()
                .stream()
                .sorted(Comparator.comparingInt(TournamentEntry::score).reversed())
                .toList();
    }

    public static long getRemainingTicks(MinecraftServer server) {
        TournamentState state = TournamentState.get(server);

        if (!state.active || state.endWorldTime <= 0L) {
            return 0L;
        }

        long currentTime = server.getOverworld().getTime();

        return Math.max(0L, state.endWorldTime - currentTime);
    }

    public static String getRemainingTimeText(MinecraftServer server) {
        long remainingTicks = getRemainingTicks(server);
        long totalSeconds = remainingTicks / 20L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;

        return String.format(Locale.ROOT, "%d:%02d", minutes, seconds);
    }

    public static void tick(MinecraftServer server) {
        TournamentState state = TournamentState.get(server);

        if (!state.active || state.endWorldTime <= 0L) {
            return;
        }

        long remainingTicks = getRemainingTicks(server);

        if (remainingTicks <= 0L) {
            server.getPlayerManager().broadcast(
                    Text.literal("Time is up!")
                            .formatted(Formatting.RED, Formatting.BOLD),
                    false
            );

            reveal(server);
            return;
        }

        if (!state.warnedFiveMinutes && remainingTicks <= 5L * 60L * 20L) {
            state.warnedFiveMinutes = true;
            state.markDirty();

            server.getPlayerManager().broadcast(
                    Text.literal("5 minutes remaining in the fishing tournament!")
                            .formatted(Formatting.YELLOW, Formatting.BOLD),
                    false
            );
        }

        if (!state.warnedOneMinute && remainingTicks <= 60L * 20L) {
            state.warnedOneMinute = true;
            state.markDirty();

            server.getPlayerManager().broadcast(
                    Text.literal("1 minute remaining! Submit your best fish soon!")
                            .formatted(Formatting.GOLD, Formatting.BOLD),
                    false
            );
        }

        if (!state.warnedThirtySeconds && remainingTicks <= 30L * 20L) {
            state.warnedThirtySeconds = true;
            state.markDirty();

            server.getPlayerManager().broadcast(
                    Text.literal("30 seconds remaining!")
                            .formatted(Formatting.RED, Formatting.BOLD),
                    false
            );
        }

        if (!state.warnedTenSeconds && remainingTicks <= 10L * 20L) {
            state.warnedTenSeconds = true;
            state.markDirty();

            server.getPlayerManager().broadcast(
                    Text.literal("10 seconds left! Final submissions!")
                            .formatted(Formatting.DARK_RED, Formatting.BOLD),
                    false
            );
        }
    }

    public static void reveal(MinecraftServer server) {
        TournamentState state = TournamentState.get(server);

        if (state.bestEntries.isEmpty()) {
            server.getPlayerManager().broadcast(
                    Text.literal("Tournament ended. No fish were submitted.")
                            .formatted(Formatting.YELLOW),
                    false
            );

            state.active = false;
            state.endWorldTime = 0L;

            state.warnedFiveMinutes = false;
            state.warnedOneMinute = false;
            state.warnedThirtySeconds = false;
            state.warnedTenSeconds = false;

            state.markDirty();
            return;
        }

        List<TournamentEntry> rankedEntries = getRankedEntries(server);

        server.getPlayerManager().broadcast(Text.literal(""), false);

        server.getPlayerManager().broadcast(
                Text.literal("Fishing Tournament Results")
                        .formatted(Formatting.GOLD, Formatting.BOLD),
                false
        );

        int shownEntries = Math.min(3, rankedEntries.size());

        for (int i = 0; i < shownEntries; i++) {
            TournamentEntry entry = rankedEntries.get(i);
            int rank = i + 1;
            int submitted = getPlayerSubmissionCount(server, entry.playerUuid());

            server.getPlayerManager().broadcast(
                    Text.empty()
                            .append(Text.literal("#" + rank + " ")
                                    .formatted(getRankFormatting(rank), Formatting.BOLD))
                            .append(Text.literal(entry.playerName())
                                    .formatted(Formatting.WHITE, Formatting.BOLD))
                            .append(Text.literal(" submitted ")
                                    .formatted(Formatting.GRAY))
                            .append(Text.literal(String.valueOf(submitted))
                                    .formatted(Formatting.AQUA))
                            .append(Text.literal(" fish")
                                    .formatted(Formatting.GRAY))
                            .append(Text.literal(" | Best: ")
                                    .formatted(Formatting.DARK_GRAY))
                            .append(Text.literal(entry.species())
                                    .formatted(getRarityFormatting(entry.rarity()), Formatting.BOLD))
                            .append(Text.literal(" | ")
                                    .formatted(Formatting.DARK_GRAY))
                            .append(Text.literal(entry.catchZone())
                                    .formatted(getZoneFormatting(entry.catchZone())))
                            .append(Text.literal(" | ")
                                    .formatted(Formatting.DARK_GRAY))
                            .append(Text.literal(entry.rarity())
                                    .formatted(getRarityFormatting(entry.rarity())))
                            .append(Text.literal(" | ")
                                    .formatted(Formatting.DARK_GRAY))
                            .append(Text.literal(formatKg(entry.weightKg()))
                                    .formatted(Formatting.AQUA))
                            .append(Text.literal(" / ")
                                    .formatted(Formatting.GRAY))
                            .append(Text.literal(formatLb(entry.weightKg()))
                                    .formatted(Formatting.AQUA))
                            .append(Text.literal(" | ")
                                    .formatted(Formatting.DARK_GRAY))
                            .append(Text.literal(String.format(Locale.ROOT, "%.1f cm", entry.lengthCm()))
                                    .formatted(Formatting.AQUA))
                            .append(Text.literal(" | Score ")
                                    .formatted(Formatting.GRAY))
                            .append(Text.literal(String.valueOf(entry.score()))
                                    .formatted(Formatting.GREEN, Formatting.BOLD)),
                    false
            );
        }

        if (rankedEntries.size() > 3) {
            server.getPlayerManager().broadcast(
                    Text.literal("+" + (rankedEntries.size() - 3) + " more players submitted fish.")
                            .formatted(Formatting.GRAY),
                    false
            );
        }

        state.active = false;
        state.endWorldTime = 0L;

        state.warnedFiveMinutes = false;
        state.warnedOneMinute = false;
        state.warnedThirtySeconds = false;
        state.warnedTenSeconds = false;

        state.markDirty();
    }

    private static String formatKg(double weightKg) {
        return String.format(Locale.ROOT, "%.2f kg", weightKg);
    }

    private static String formatLb(double weightKg) {
        return String.format(Locale.ROOT, "%.2f lb", weightKg * FishDataComponent.KG_TO_LB);
    }

    private static Formatting getRankFormatting(int rank) {
        return switch (rank) {
            case 1 -> Formatting.GOLD;
            case 2 -> Formatting.GRAY;
            case 3 -> Formatting.DARK_AQUA;
            default -> Formatting.WHITE;
        };
    }

    private static Formatting getZoneFormatting(String catchZone) {
        return switch (catchZone.toLowerCase(Locale.ROOT)) {
            case "deep zone" -> Formatting.BLUE;
            default -> Formatting.GRAY;
        };
    }

    private static Formatting getRarityFormatting(String rarity) {
        return switch (rarity.toLowerCase(Locale.ROOT)) {
            case "uncommon" -> Formatting.GREEN;
            case "rare" -> Formatting.AQUA;
            case "legendary" -> Formatting.LIGHT_PURPLE;
            default -> Formatting.WHITE;
        };
    }

    public record SubmissionResult(
            boolean tournamentActive,
            boolean accepted,
            boolean newPersonalBest,
            boolean limitReached,
            int remainingSubmissions
    ) {
        public static SubmissionResult notActive() {
            return new SubmissionResult(false, false, false, false, 0);
        }

        public static SubmissionResult reachedLimit() {
            return new SubmissionResult(true, false, false, true, 0);
        }

        public static SubmissionResult accepted(boolean newPersonalBest, int remainingSubmissions) {
            return new SubmissionResult(true, true, newPersonalBest, false, remainingSubmissions);
        }
    }
}