package net.droingo.fishtourn.tournament;

import java.util.UUID;

public record TournamentEntry(
        UUID playerUuid,
        String playerName,
        String species,
        String rarity,
        double lengthCm,
        double weightKg,
        int score
) {
}