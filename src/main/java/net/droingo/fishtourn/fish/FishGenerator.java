package net.droingo.fishtourn.fish;

import net.droingo.fishtourn.component.FishDataComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;

import java.util.List;

public final class FishGenerator {
    private static final List<FishSpeciesDefinition> COD_SPECIES = List.of(
            new FishSpeciesDefinition("Atlantic Cod", 35.0, 85.0, 1.2, 7.5, "Common", 1.00, 55),
            new FishSpeciesDefinition("Deep Water Cod", 55.0, 115.0, 3.5, 14.0, "Rare", 1.35, 25),
            new FishSpeciesDefinition("Stonefin Cod", 45.0, 95.0, 2.8, 10.5, "Uncommon", 1.15, 35),
            new FishSpeciesDefinition("Golden Cod", 65.0, 125.0, 4.5, 18.0, "Legendary", 2.00, 5)
    );

    private static final List<FishSpeciesDefinition> SALMON_SPECIES = List.of(
            new FishSpeciesDefinition("River Salmon", 40.0, 90.0, 1.5, 8.0, "Common", 1.00, 55),
            new FishSpeciesDefinition("Silverback Salmon", 65.0, 125.0, 4.0, 18.0, "Rare", 1.40, 25),
            new FishSpeciesDefinition("Crimson Salmon", 55.0, 110.0, 3.0, 13.5, "Uncommon", 1.20, 35),
            new FishSpeciesDefinition("King Salmon", 85.0, 160.0, 8.0, 32.0, "Legendary", 2.10, 5)
    );

    private static final List<FishSpeciesDefinition> TROPICAL_SPECIES = List.of(
            new FishSpeciesDefinition("Reef Dart", 8.0, 22.0, 0.05, 0.35, "Common", 1.00, 55),
            new FishSpeciesDefinition("Sunstripe Tropical Fish", 12.0, 30.0, 0.10, 0.65, "Uncommon", 1.20, 35),
            new FishSpeciesDefinition("Bluefin Angel", 16.0, 36.0, 0.20, 0.90, "Rare", 1.45, 20),
            new FishSpeciesDefinition("Ember Guppy", 10.0, 24.0, 0.08, 0.45, "Legendary", 2.00, 5)
    );

    private static final List<FishSpeciesDefinition> PUFFERFISH_SPECIES = List.of(
            new FishSpeciesDefinition("Spotted Pufferfish", 12.0, 30.0, 0.20, 1.10, "Common", 1.00, 55),
            new FishSpeciesDefinition("Balloon Pufferfish", 18.0, 42.0, 0.50, 2.20, "Uncommon", 1.20, 35),
            new FishSpeciesDefinition("Thornback Pufferfish", 24.0, 55.0, 0.90, 3.50, "Rare", 1.50, 18),
            new FishSpeciesDefinition("Royal Pufferfish", 28.0, 65.0, 1.20, 5.00, "Legendary", 2.15, 5)
    );

    private FishGenerator() {
    }

    public static FishDataComponent generate(Item baseFishItem, Random random) {
        FishSpeciesDefinition definition = pickDefinition(baseFishItem, random);

        double lengthCm = randomDouble(random, definition.minLengthCm(), definition.maxLengthCm());
        double weightKg = randomDouble(random, definition.minWeightKg(), definition.maxWeightKg());

        int score = calculateScore(lengthCm, weightKg, definition.scoreMultiplier());

        return new FishDataComponent(
                definition.species(),
                round(lengthCm, 1),
                round(weightKg, 2),
                definition.rarity(),
                score
        );
    }

    private static FishSpeciesDefinition pickDefinition(Item baseFishItem, Random random) {
        if (baseFishItem == Items.SALMON) {
            return pickWeighted(SALMON_SPECIES, random);
        }

        if (baseFishItem == Items.TROPICAL_FISH) {
            return pickWeighted(TROPICAL_SPECIES, random);
        }

        if (baseFishItem == Items.PUFFERFISH) {
            return pickWeighted(PUFFERFISH_SPECIES, random);
        }

        return pickWeighted(COD_SPECIES, random);
    }

    private static FishSpeciesDefinition pickWeighted(List<FishSpeciesDefinition> definitions, Random random) {
        int totalWeight = 0;

        for (FishSpeciesDefinition definition : definitions) {
            totalWeight += definition.selectionWeight();
        }

        int roll = random.nextInt(totalWeight);

        for (FishSpeciesDefinition definition : definitions) {
            roll -= definition.selectionWeight();

            if (roll < 0) {
                return definition;
            }
        }

        return definitions.getFirst();
    }

    private static int calculateScore(double lengthCm, double weightKg, double multiplier) {
        double baseScore = lengthCm * 2.0 + weightKg * 100.0;
        return Math.max(1, (int) Math.round(baseScore * multiplier));
    }

    private static double randomDouble(Random random, double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}