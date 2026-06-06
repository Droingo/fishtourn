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
import net.droingo.fishtourn.network.ModNetworking;
import net.droingo.fishtourn.reel.ReelingManager;
import net.droingo.fishtourn.entity.ModEntities;

public class FishingTournament implements ModInitializer {
    public static final String MOD_ID = "fishtourn";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModComponents.register();
        ModItems.register();
        ModBlocks.register();
        ModNetworking.register();
        FishingTournamentCommands.register();
        ModEntities.register();


        ServerTickEvents.END_SERVER_TICK.register(TournamentManager::tick);
        ServerTickEvents.END_SERVER_TICK.register(ReelingManager::tick);

        LOGGER.info("Fishing Tournament initialized.");
    }
}