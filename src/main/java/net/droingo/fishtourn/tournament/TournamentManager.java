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
import net.minecraft.server.MinecraftServer;

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

    public static Optional<TournamentEntry> getPlayerBestEntry(MinecraftServer server, UUID playerUuid) {
        return Optional.ofNullable(TournamentState.get(server).bestEntries.get(playerUuid));
    }

    public static void start(MinecraftServer server) {
        start(server, 10);
        TournamentState state = TournamentState.get(server);

        state.active = true;
        state.bestEntries.clear();
        state.submissionCounts.clear();
        state.markDirty();

        server.getPlayerManager().broadcast(
                Text.literal("Fishing tournament started! Submit your best fish at the Tournament Submission Barrel.")
                        .formatted(Formatting.AQUA),
                false
        );
    }

    public static void reset(MinecraftServer server) {
        TournamentState state = TournamentState.get(server);
        state.endWorldTime = 0L;
        state.active = false;
        state.bestEntries.clear();
        state.submissionCounts.clear();
        state.markDirty();

        server.getPlayerManager().broadcast(
                Text.literal("Fishing tournament reset.")
                        .formatted(Formatting.YELLOW),
                false
        );
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

        state.submissionCounts.merge(uuid, 1, Integer::sum);

        TournamentEntry newEntry = new TournamentEntry(
                uuid,
                player.getName().getString(),
                fishData.species(),
                fishData.rarity(),
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

        return SubmissionResult.accepted(newPersonalBest);
    }

    public static List<TournamentEntry> getRankedEntries(MinecraftServer server) {
        return TournamentState.get(server).bestEntries.values()
                .stream()
                .sorted(Comparator.comparingInt(TournamentEntry::score).reversed())
                .toList();
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
                            .append(Text.literal("#" + rank + " ").formatted(getRankFormatting(rank), Formatting.BOLD))
                            .append(Text.literal(entry.playerName()).formatted(Formatting.WHITE, Formatting.BOLD))
                            .append(Text.literal(" submitted ").formatted(Formatting.GRAY))
                            .append(Text.literal(String.valueOf(submitted)).formatted(Formatting.AQUA))
                            .append(Text.literal(" fish").formatted(Formatting.GRAY))
                            .append(Text.literal(" | Best: ").formatted(Formatting.DARK_GRAY))
                            .append(Text.literal(entry.species()).formatted(getRarityFormatting(entry.rarity()), Formatting.BOLD))
                            .append(Text.literal(" | ").formatted(Formatting.DARK_GRAY))
                            .append(Text.literal(entry.rarity()).formatted(getRarityFormatting(entry.rarity())))
                            .append(Text.literal(" | ").formatted(Formatting.DARK_GRAY))
                            .append(Text.literal(formatKg(entry.weightKg())).formatted(Formatting.AQUA))
                            .append(Text.literal(" / ").formatted(Formatting.GRAY))
                            .append(Text.literal(formatLb(entry.weightKg())).formatted(Formatting.AQUA))
                            .append(Text.literal(" | ").formatted(Formatting.DARK_GRAY))
                            .append(Text.literal(String.format(Locale.ROOT, "%.1f cm", entry.lengthCm())).formatted(Formatting.AQUA))
                            .append(Text.literal(" | Score ").formatted(Formatting.GRAY))
                            .append(Text.literal(String.valueOf(entry.score())).formatted(Formatting.GREEN, Formatting.BOLD)),
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
        state.markDirty();
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

        long currentTime = server.getOverworld().getTime();

        if (currentTime >= state.endWorldTime) {
            server.getPlayerManager().broadcast(
                    Text.literal("Time is up!")
                            .formatted(Formatting.RED, Formatting.BOLD),
                    false
            );

            reveal(server);
        }
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

    private static Formatting getRarityFormatting(String rarity) {
        return switch (rarity.toLowerCase(Locale.ROOT)) {
            case "uncommon" -> Formatting.GREEN;
            case "rare" -> Formatting.AQUA;
            case "legendary" -> Formatting.LIGHT_PURPLE;
            default -> Formatting.WHITE;
        };
    }

    public record SubmissionResult(boolean tournamentActive, boolean accepted, boolean newPersonalBest) {
        public static SubmissionResult notActive() {
            return new SubmissionResult(false, false, false);
        }

        public static void start(MinecraftServer server, int minutes) {
            TournamentState state = TournamentState.get(server);

            long durationTicks = minutes * 60L * 20L;
            long currentTime = server.getOverworld().getTime();

            state.active = true;
            state.endWorldTime = currentTime + durationTicks;
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

            long currentTime = server.getOverworld().getTime();

            if (currentTime >= state.endWorldTime) {
                server.getPlayerManager().broadcast(
                        Text.literal("Time is up!")
                                .formatted(Formatting.RED, Formatting.BOLD),
                        false
                );

                reveal(server);
            }
        }

        public static SubmissionResult accepted(boolean newPersonalBest) {
            return new SubmissionResult(true, true, newPersonalBest);
        }
    }
}