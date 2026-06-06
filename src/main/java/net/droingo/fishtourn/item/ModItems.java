package net.droingo.fishtourn.item;

import net.droingo.fishtourn.FishingTournament;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item TOURNAMENT_ROD = register(
            "tournament_rod",
            new TournamentFishingRodItem(new Item.Settings().maxDamage(64))
    );

    public static final Item MEMORIAL_FIREWORK = register(
            "memorial_firework",
            new MemorialFireworkItem(new Item.Settings())
    );

    private ModItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(TOURNAMENT_ROD);
            entries.add(MEMORIAL_FIREWORK);
        });

        FishingTournament.LOGGER.info("Registering {} items", FishingTournament.MOD_ID);
    }

    private static Item register(String name, Item item) {
        return Registry.register(
                Registries.ITEM,
                Identifier.of(FishingTournament.MOD_ID, name),
                item
        );
    }
}