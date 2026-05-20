package net.droingo.fishtourn.item;

import net.droingo.fishtourn.FishingTournament;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item TOURNAMENT_ROD = Registry.register(
            Registries.ITEM,
            Identifier.of(FishingTournament.MOD_ID, "tournament_rod"),
            new TournamentFishingRodItem(new Item.Settings()
                    .maxDamage(64))
    );

    private ModItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(TOURNAMENT_ROD);
        });

        FishingTournament.LOGGER.info("Registering {} items", FishingTournament.MOD_ID);
    }
}