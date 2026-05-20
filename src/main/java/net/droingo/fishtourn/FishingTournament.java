package net.droingo.fishtourn;

import net.droingo.fishtourn.command.FishingTournamentCommands;
import net.droingo.fishtourn.component.ModComponents;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import net.droingo.fishtourn.block.ModBlocks;
import org.slf4j.LoggerFactory;
import net.droingo.fishtourn.tournament.TournamentManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class FishingTournament implements ModInitializer {
    public static final String MOD_ID = "fishtourn";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModComponents.register();
        ModBlocks.register();
        FishingTournamentCommands.register();

        ServerTickEvents.END_SERVER_TICK.register(TournamentManager::tick);

        LOGGER.info("Fishing Tournament initialized.");
    }
}