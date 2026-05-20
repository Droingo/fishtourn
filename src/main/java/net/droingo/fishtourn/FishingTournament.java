package net.droingo.fishtourn;

import net.droingo.fishtourn.block.ModBlocks;
import net.droingo.fishtourn.command.FishingTournamentCommands;
import net.droingo.fishtourn.component.ModComponents;
import net.droingo.fishtourn.tournament.TournamentManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import net.droingo.fishtourn.item.ModItems;
import org.slf4j.LoggerFactory;

public class FishingTournament implements ModInitializer {
    public static final String MOD_ID = "fishtourn";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModComponents.register();
        ModItems.register();
        ModBlocks.register();
        FishingTournamentCommands.register();

        ServerTickEvents.END_SERVER_TICK.register(TournamentManager::tick);

        LOGGER.info("Fishing Tournament initialized.");
    }
}