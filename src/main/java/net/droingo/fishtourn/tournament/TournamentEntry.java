package net.droingo.fishtourn.tournament;

import java.util.UUID;

public record TournamentEntry(
        UUID playerUuid,
        String playerName,
        String species,
        String rarity,
        String catchZone,
        double lengthCm,
        double weightKg,
        int score
) {
}