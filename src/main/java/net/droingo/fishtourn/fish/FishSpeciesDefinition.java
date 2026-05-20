package net.droingo.fishtourn.fish;

public record FishSpeciesDefinition(
        String species,
        double minLengthCm,
        double maxLengthCm,
        double minWeightKg,
        double maxWeightKg,
        String rarity,
        double scoreMultiplier,
        int selectionWeight
) {
}