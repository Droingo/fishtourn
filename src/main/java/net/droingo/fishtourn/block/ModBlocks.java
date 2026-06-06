package net.droingo.fishtourn.block;

import net.droingo.fishtourn.FishingTournament;
import net.droingo.fishtourn.item.TrophyBlockItem;
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
    public static final Block TOURNAMENT_SUBMISSION_BARREL = registerBlockOnly(
            "tournament_submission_barrel",
            new TournamentSubmissionBlock(AbstractBlock.Settings.copy(Blocks.BARREL))
    );

    public static final Item TOURNAMENT_SUBMISSION_BARREL_ITEM = registerBlockItem(
            "tournament_submission_barrel",
            TOURNAMENT_SUBMISSION_BARREL
    );

    public static final Block DEEP_FISHING_ZONE = registerBlockOnly(
            "deep_fishing_zone",
            new FishingZoneBlock(AbstractBlock.Settings.copy(Blocks.GLASS).noCollision().nonOpaque().ticksRandomly())
    );

    public static final Item DEEP_FISHING_ZONE_ITEM = registerBlockItem(
            "deep_fishing_zone",
            DEEP_FISHING_ZONE
    );

    public static final Block TROPHY = registerBlockOnly(
            "trophy",
            new TrophyBlock(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).nonOpaque())
    );

    public static final Item TROPHY_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of(FishingTournament.MOD_ID, "trophy"),
            new TrophyBlockItem(TROPHY, new Item.Settings())
    );

    private ModBlocks() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(TOURNAMENT_SUBMISSION_BARREL_ITEM);
            entries.add(DEEP_FISHING_ZONE_ITEM);
            entries.add(TROPHY_ITEM);
        });

        FishingTournament.LOGGER.info("Registering {} blocks", FishingTournament.MOD_ID);
    }

    private static Block registerBlockOnly(String name, Block block) {
        return Registry.register(
                Registries.BLOCK,
                Identifier.of(FishingTournament.MOD_ID, name),
                block
        );
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(
                Registries.ITEM,
                Identifier.of(FishingTournament.MOD_ID, name),
                new BlockItem(block, new Item.Settings())
        );
    }
}