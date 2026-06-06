package net.droingo.fishtourn.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FishOfThievesCompat {
    private static final String MOD_ID = "fishofthieves";

    private static final List<String> RAW_FISH_IDS = List.of(
            "splashtail",
            "pondie",
            "islehopper",
            "ancientscale",
            "plentifin",
            "wildsplash",
            "devilfish",
            "battlegill",
            "wrecker",
            "stormfish"
    );

    private static List<Item> cachedFishItems;

    private FishOfThievesCompat() {
    }

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(MOD_ID);
    }

    public static Optional<ItemStack> rollFishOfThievesCatch(Random random) {
        if (!isLoaded()) {
            return Optional.empty();
        }

        List<Item> fishItems = getAvailableFishItems();

        if (fishItems.isEmpty()) {
            return Optional.empty();
        }

        // First tuning pass: about 35% of tournament fish catches become Fish of Thieves fish.
        if (random.nextFloat() > 0.35F) {
            return Optional.empty();
        }

        Item item = fishItems.get(random.nextInt(fishItems.size()));
        return Optional.of(new ItemStack(item));
    }

    private static List<Item> getAvailableFishItems() {
        if (cachedFishItems != null) {
            return cachedFishItems;
        }

        List<Item> items = new ArrayList<>();

        for (String id : RAW_FISH_IDS) {
            Identifier identifier = Identifier.of(MOD_ID, id);

            if (Registries.ITEM.containsId(identifier)) {
                items.add(Registries.ITEM.get(identifier));
            }
        }

        cachedFishItems = List.copyOf(items);
        return cachedFishItems;
    }
}