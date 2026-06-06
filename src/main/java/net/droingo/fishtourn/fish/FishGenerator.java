package net.droingo.fishtourn.fish;

import net.droingo.fishtourn.component.FishDataComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.Locale;

public final class FishGenerator {
    private static final List<FishSpeciesDefinition> COD_SPECIES = List.of(
            new FishSpeciesDefinition("Atlantic Cod", 35.0, 85.0, 1.2, 7.5, "Common", 1.00, 55),
            new FishSpeciesDefinition("Stonefin Cod", 45.0, 95.0, 2.8, 10.5, "Uncommon", 1.15, 35),
            new FishSpeciesDefinition("Deep Water Cod", 55.0, 115.0, 3.5, 14.0, "Rare", 1.35, 25),
            new FishSpeciesDefinition("Golden Cod", 65.0, 125.0, 4.5, 18.0, "Legendary", 2.00, 5)
    );

    private static final List<FishSpeciesDefinition> SALMON_SPECIES = List.of(
            new FishSpeciesDefinition("River Salmon", 40.0, 90.0, 1.5, 8.0, "Common", 1.00, 55),
            new FishSpeciesDefinition("Crimson Salmon", 55.0, 110.0, 3.0, 13.5, "Uncommon", 1.20, 35),
            new FishSpeciesDefinition("Silverback Salmon", 65.0, 125.0, 4.0, 18.0, "Rare", 1.40, 25),
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

    private static final List<FishSpeciesDefinition> SPLASHTAIL_SPECIES = List.of(
            new FishSpeciesDefinition("Ruby Splashtail", 25.0, 60.0, 0.7, 3.0, "Common", 1.00, 55),
            new FishSpeciesDefinition("Seafoam Splashtail", 30.0, 70.0, 1.0, 4.0, "Uncommon", 1.15, 35),
            new FishSpeciesDefinition("Sunny Splashtail", 38.0, 82.0, 1.5, 5.5, "Rare", 1.35, 18),
            new FishSpeciesDefinition("Umber Splashtail", 45.0, 95.0, 2.0, 7.0, "Legendary", 1.85, 5)
    );

    private static final List<FishSpeciesDefinition> PONDIE_SPECIES = List.of(
            new FishSpeciesDefinition("Charcoal Pondie", 12.0, 28.0, 0.12, 0.65, "Common", 1.00, 55),
            new FishSpeciesDefinition("Orchid Pondie", 15.0, 34.0, 0.18, 0.85, "Uncommon", 1.18, 35),
            new FishSpeciesDefinition("Bronze Pondie", 18.0, 42.0, 0.25, 1.15, "Rare", 1.38, 18),
            new FishSpeciesDefinition("Bright Pondie", 22.0, 50.0, 0.35, 1.55, "Legendary", 1.95, 5)
    );

    private static final List<FishSpeciesDefinition> ISLEHOPPER_SPECIES = List.of(
            new FishSpeciesDefinition("Stone Islehopper", 18.0, 42.0, 0.35, 1.4, "Common", 1.00, 55),
            new FishSpeciesDefinition("Moss Islehopper", 22.0, 50.0, 0.55, 1.9, "Uncommon", 1.18, 35),
            new FishSpeciesDefinition("Honey Islehopper", 27.0, 60.0, 0.8, 2.6, "Rare", 1.40, 18),
            new FishSpeciesDefinition("Raven Islehopper", 32.0, 72.0, 1.1, 3.4, "Legendary", 2.00, 5)
    );

    private static final List<FishSpeciesDefinition> ANCIENTSCALE_SPECIES = List.of(
            new FishSpeciesDefinition("Almond Ancientscale", 32.0, 78.0, 1.2, 5.0, "Common", 1.00, 55),
            new FishSpeciesDefinition("Sapphire Ancientscale", 40.0, 92.0, 1.8, 7.0, "Uncommon", 1.20, 35),
            new FishSpeciesDefinition("Smoke Ancientscale", 50.0, 110.0, 2.6, 9.5, "Rare", 1.45, 18),
            new FishSpeciesDefinition("Bone Ancientscale", 62.0, 130.0, 3.6, 13.0, "Legendary", 2.10, 5)
    );

    private static final List<FishSpeciesDefinition> PLENTIFIN_SPECIES = List.of(
            new FishSpeciesDefinition("Olive Plentifin", 28.0, 68.0, 0.9, 3.8, "Common", 1.00, 55),
            new FishSpeciesDefinition("Amber Plentifin", 34.0, 82.0, 1.3, 5.0, "Uncommon", 1.18, 35),
            new FishSpeciesDefinition("Cloudy Plentifin", 42.0, 98.0, 1.9, 6.8, "Rare", 1.42, 18),
            new FishSpeciesDefinition("Watery Plentifin", 52.0, 116.0, 2.8, 9.5, "Legendary", 2.05, 5)
    );

    private static final List<FishSpeciesDefinition> WILDSPLASH_SPECIES = List.of(
            new FishSpeciesDefinition("Russet Wildsplash", 30.0, 70.0, 1.0, 4.2, "Common", 1.00, 55),
            new FishSpeciesDefinition("Sandy Wildsplash", 38.0, 86.0, 1.5, 5.8, "Uncommon", 1.20, 35),
            new FishSpeciesDefinition("Ocean Wildsplash", 48.0, 106.0, 2.3, 8.5, "Rare", 1.45, 18),
            new FishSpeciesDefinition("Muddy Wildsplash", 58.0, 128.0, 3.4, 12.0, "Legendary", 2.10, 5)
    );

    private static final List<FishSpeciesDefinition> DEVILFISH_SPECIES = List.of(
            new FishSpeciesDefinition("Ashen Devilfish", 26.0, 65.0, 0.9, 4.0, "Common", 1.00, 55),
            new FishSpeciesDefinition("Seashell Devilfish", 34.0, 82.0, 1.5, 6.2, "Uncommon", 1.25, 35),
            new FishSpeciesDefinition("Lava Devilfish", 46.0, 105.0, 2.4, 9.4, "Rare", 1.55, 18),
            new FishSpeciesDefinition("Forsaken Devilfish", 60.0, 135.0, 3.8, 14.0, "Legendary", 2.25, 5)
    );

    private static final List<FishSpeciesDefinition> BATTLEGILL_SPECIES = List.of(
            new FishSpeciesDefinition("Jade Battlegill", 30.0, 72.0, 1.1, 4.8, "Common", 1.00, 55),
            new FishSpeciesDefinition("Sky Battlegill", 38.0, 90.0, 1.7, 6.8, "Uncommon", 1.22, 35),
            new FishSpeciesDefinition("Rum Battlegill", 50.0, 112.0, 2.7, 10.2, "Rare", 1.50, 18),
            new FishSpeciesDefinition("Sand Battlegill", 64.0, 145.0, 4.2, 15.8, "Legendary", 2.20, 5)
    );

    private static final List<FishSpeciesDefinition> WRECKER_SPECIES = List.of(
            new FishSpeciesDefinition("Rose Wrecker", 45.0, 100.0, 2.2, 9.0, "Common", 1.00, 55),
            new FishSpeciesDefinition("Sun Wrecker", 55.0, 120.0, 3.2, 13.0, "Uncommon", 1.25, 35),
            new FishSpeciesDefinition("Blackcloud Wrecker", 70.0, 150.0, 5.0, 20.0, "Rare", 1.60, 18),
            new FishSpeciesDefinition("Snow Wrecker", 88.0, 180.0, 7.0, 28.0, "Legendary", 2.35, 5)
    );

    private static final List<FishSpeciesDefinition> STORMFISH_SPECIES = List.of(
            new FishSpeciesDefinition("Ancient Stormfish", 50.0, 115.0, 2.5, 11.0, "Common", 1.00, 55),
            new FishSpeciesDefinition("Shores Stormfish", 62.0, 140.0, 3.8, 16.0, "Uncommon", 1.25, 35),
            new FishSpeciesDefinition("Wild Stormfish", 78.0, 168.0, 5.8, 24.0, "Rare", 1.60, 18),
            new FishSpeciesDefinition("Shadow Stormfish", 95.0, 205.0, 8.2, 34.0, "Legendary", 2.40, 5)
    );

    private FishGenerator() {
    }

    public static FishDataComponent generate(Item baseFishItem, Random random) {
        return generate(baseFishItem, random, CastZone.NONE);
    }

    public static FishDataComponent generate(Item baseFishItem, Random random, CastZone zone) {
        FishSpeciesDefinition definition = pickDefinition(baseFishItem, random, zone);

        double lengthCm = randomDouble(random, definition.minLengthCm(), definition.maxLengthCm());
        double weightKg = randomDouble(random, definition.minWeightKg(), definition.maxWeightKg());

        if (zone == CastZone.DEEP) {
            lengthCm *= 1.08D;
            weightKg *= 1.12D;
        }

        int score = calculateScore(lengthCm, weightKg, definition.scoreMultiplier());

        return new FishDataComponent(
                definition.species(),
                round(lengthCm, 1),
                round(weightKg, 2),
                definition.rarity(),
                score,
                zone.displayName()
        );
    }

    private static FishSpeciesDefinition pickDefinition(Item baseFishItem, Random random, CastZone zone) {
        if (baseFishItem == Items.SALMON) {
            return pickWeighted(SALMON_SPECIES, random, zone);
        }

        if (baseFishItem == Items.TROPICAL_FISH) {
            return pickWeighted(TROPICAL_SPECIES, random, zone);
        }

        if (baseFishItem == Items.PUFFERFISH) {
            return pickWeighted(PUFFERFISH_SPECIES, random, zone);
        }

        List<FishSpeciesDefinition> fishOfThievesSpecies = getFishOfThievesSpecies(baseFishItem);
        if (fishOfThievesSpecies != null) {
            return pickWeighted(fishOfThievesSpecies, random, zone);
        }

        return pickWeighted(COD_SPECIES, random, zone);
    }

    private static List<FishSpeciesDefinition> getFishOfThievesSpecies(Item item) {
        Identifier id = Registries.ITEM.getId(item);

        if (!id.getNamespace().equals("fishofthieves")) {
            return null;
        }

        return switch (id.getPath()) {
            case "splashtail" -> SPLASHTAIL_SPECIES;
            case "pondie" -> PONDIE_SPECIES;
            case "islehopper" -> ISLEHOPPER_SPECIES;
            case "ancientscale" -> ANCIENTSCALE_SPECIES;
            case "plentifin" -> PLENTIFIN_SPECIES;
            case "wildsplash" -> WILDSPLASH_SPECIES;
            case "devilfish" -> DEVILFISH_SPECIES;
            case "battlegill" -> BATTLEGILL_SPECIES;
            case "wrecker" -> WRECKER_SPECIES;
            case "stormfish" -> STORMFISH_SPECIES;
            default -> null;
        };
    }

    private static FishSpeciesDefinition pickWeighted(List<FishSpeciesDefinition> definitions, Random random, CastZone zone) {
        int totalWeight = 0;

        for (FishSpeciesDefinition definition : definitions) {
            totalWeight += getAdjustedSelectionWeight(definition, zone);
        }

        int roll = random.nextInt(totalWeight);

        for (FishSpeciesDefinition definition : definitions) {
            roll -= getAdjustedSelectionWeight(definition, zone);

            if (roll < 0) {
                return definition;
            }
        }

        return definitions.getFirst();
    }

    private static int getAdjustedSelectionWeight(FishSpeciesDefinition definition, CastZone zone) {
        int baseWeight = definition.selectionWeight();

        if (zone != CastZone.DEEP) {
            return baseWeight;
        }

        return switch (definition.rarity().toLowerCase(Locale.ROOT)) {
            case "common" -> Math.max(1, (int) (baseWeight * 0.45D));
            case "uncommon" -> Math.max(1, (int) (baseWeight * 0.85D));
            case "rare" -> Math.max(1, (int) (baseWeight * 1.8D));
            case "legendary" -> Math.max(1, (int) (baseWeight * 2.5D));
            default -> baseWeight;
        };
    }

    private static int calculateScore(double lengthCm, double weightKg, double multiplier) {
        double baseScore = lengthCm * 2.0D + weightKg * 100.0D;
        return Math.max(1, (int) Math.round(baseScore * multiplier));
    }

    private static double randomDouble(Random random, double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static double round(double value, int decimals) {
        double scale = Math.pow(10.0D, decimals);
        return Math.round(value * scale) / scale;
    }
}