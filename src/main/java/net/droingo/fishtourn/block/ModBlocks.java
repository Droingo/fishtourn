package net.droingo.fishtourn.block;

import net.droingo.fishtourn.FishingTournament;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    public static final Block TOURNAMENT_SUBMISSION_BARREL = Registry.register(
            Registries.BLOCK,
            Identifier.of(FishingTournament.MOD_ID, "tournament_submission_barrel"),
            new TournamentSubmissionBlock(AbstractBlock.Settings.copy(Blocks.BARREL))
    );

    public static final Item TOURNAMENT_SUBMISSION_BARREL_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of(FishingTournament.MOD_ID, "tournament_submission_barrel"),
            new BlockItem(TOURNAMENT_SUBMISSION_BARREL, new Item.Settings())
    );
    public static final Block DEEP_FISHING_ZONE = Registry.register(
            Registries.BLOCK,
            Identifier.of(FishingTournament.MOD_ID, "deep_fishing_zone"),
            new FishingZoneBlock(AbstractBlock.Settings.copy(Blocks.GLASS).noCollision().nonOpaque())
    );

    public static final Item DEEP_FISHING_ZONE_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of(FishingTournament.MOD_ID, "deep_fishing_zone"),
            new BlockItem(DEEP_FISHING_ZONE, new Item.Settings())
    );

    private ModBlocks() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(TOURNAMENT_SUBMISSION_BARREL_ITEM);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(TOURNAMENT_SUBMISSION_BARREL_ITEM);
            entries.add(DEEP_FISHING_ZONE_ITEM);
        });

        FishingTournament.LOGGER.info("Registering {} blocks", FishingTournament.MOD_ID);
    }
}