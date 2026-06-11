package net.droingo.fishtourn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.droingo.fishtourn.block.ModBlocks;
import net.droingo.fishtourn.component.FishDataComponent;
import net.droingo.fishtourn.component.ModComponents;
import net.droingo.fishtourn.fish.FishItemFactory;
import net.droingo.fishtourn.item.ModItems;
import net.droingo.fishtourn.tournament.TournamentManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;

public final class FishingTournamentCommands {
    private FishingTournamentCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(FishingTournamentCommands::registerCommands);
    }

    private static void registerCommands(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
    ) {
        dispatcher.register(CommandManager.literal("fishtourn")
                .then(CommandManager.literal("testfish")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("cod")
                                .executes(context -> giveGeneratedFish(context.getSource(), Items.COD)))
                        .then(CommandManager.literal("salmon")
                                .executes(context -> giveGeneratedFish(context.getSource(), Items.SALMON)))
                        .then(CommandManager.literal("tropical")
                                .executes(context -> giveGeneratedFish(context.getSource(), Items.TROPICAL_FISH)))
                        .then(CommandManager.literal("pufferfish")
                                .executes(context -> giveGeneratedFish(context.getSource(), Items.PUFFERFISH)))
                )

                .then(CommandManager.literal("give_submission_barrel")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> giveSubmissionBarrel(context.getSource()))
                )

                .then(CommandManager.literal("give_rod")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> giveTournamentRod(context.getSource()))
                )

                .then(CommandManager.literal("give_deep_zone")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> giveDeepFishingZone(context.getSource()))
                )

                .then(CommandManager.literal("tournament")
                        .then(CommandManager.literal("start")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> startTournament(context.getSource(), 10))
                                .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1, 120))
                                        .executes(context -> startTournament(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "minutes")
                                        ))
                                )
                        )

                        .then(CommandManager.literal("status")
                                .executes(context -> tournamentStatus(context.getSource()))
                        )

                        .then(CommandManager.literal("reveal")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> revealTournament(context.getSource()))
                        )

                        .then(CommandManager.literal("reset")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> resetTournament(context.getSource()))
                        )

                        .then(CommandManager.literal("clear_fish")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> clearTournamentFish(context.getSource()))
                        )
                )
        );
    }

    private static int giveDeepFishingZone(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(Text.literal("This command must be run by a player."));
            return 0;
        }

        ItemStack stack = new ItemStack(ModBlocks.DEEP_FISHING_ZONE_ITEM);
        boolean inserted = player.getInventory().insertStack(stack);

        if (!inserted) {
            player.dropItem(stack, false);
        }

        source.sendFeedback(
                () -> Text.literal("Gave Deep Fishing Zone Marker."),
                false
        );

        return 1;
    }

    private static int giveGeneratedFish(ServerCommandSource source, Item fishItem) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(Text.literal("This command must be run by a player."));
            return 0;
        }

        ItemStack stack = FishItemFactory.createGeneratedFish(fishItem, player.getWorld().getRandom());
        FishDataComponent fishData = stack.get(ModComponents.FISH_DATA);

        boolean inserted = player.getInventory().insertStack(stack);

        if (!inserted) {
            player.dropItem(stack, false);
        }

        if (fishData != null) {
            source.sendFeedback(
                    () -> Text.literal("Created fish: "
                            + fishData.species()
                            + " | "
                            + fishData.formattedWeightBoth()
                            + " | Score "
                            + fishData.score()),
                    false
            );
        }

        return 1;
    }

    private static int giveTournamentRod(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(Text.literal("This command must be run by a player."));
            return 0;
        }

        ItemStack stack = new ItemStack(ModItems.TOURNAMENT_ROD);
        boolean inserted = player.getInventory().insertStack(stack);

        if (!inserted) {
            player.dropItem(stack, false);
        }

        source.sendFeedback(
                () -> Text.literal("Gave Tournament Rod."),
                false
        );

        return 1;
    }

    private static int giveSubmissionBarrel(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(Text.literal("This command must be run by a player."));
            return 0;
        }

        ItemStack stack = new ItemStack(ModBlocks.TOURNAMENT_SUBMISSION_BARREL_ITEM);
        boolean inserted = player.getInventory().insertStack(stack);

        if (!inserted) {
            player.dropItem(stack, false);
        }

        source.sendFeedback(
                () -> Text.literal("Gave Tournament Submission Barrel."),
                false
        );

        return 1;
    }

    private static int startTournament(ServerCommandSource source, int minutes) {
        TournamentManager.start(source.getServer(), minutes);
        return 1;
    }

    private static int tournamentStatus(ServerCommandSource source) {
        if (TournamentManager.isActive(source.getServer())) {
            source.sendFeedback(
                    () -> Text.literal("Tournament is active.\n"
                                    + "Time remaining: "
                                    + TournamentManager.getRemainingTimeText(source.getServer())
                                    + ".\n"
                                    + "Submissions received: "
                                    + TournamentManager.getSubmissionCount(source.getServer())
                                    + " fish from "
                                    + TournamentManager.getUniqueSubmitterCount(source.getServer())
                                    + " players.")
                            .formatted(Formatting.AQUA),
                    false
            );
        } else {
            source.sendFeedback(
                    () -> Text.literal("No tournament is currently active.")
                            .formatted(Formatting.YELLOW),
                    false
            );
        }

        return 1;
    }

    private static int revealTournament(ServerCommandSource source) {
        TournamentManager.reveal(source.getServer());
        return 1;
    }

    private static int resetTournament(ServerCommandSource source) {
        TournamentManager.reset(source.getServer());
        return 1;
    }

    private static int clearTournamentFish(ServerCommandSource source) {
        int removedFish = 0;
        int affectedPlayers = 0;

        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            int removedFromPlayer = removeTournamentFishFromPlayer(player);

            if (removedFromPlayer > 0) {
                affectedPlayers++;
                removedFish += removedFromPlayer;

                player.sendMessage(
                        Text.literal("Tournament fish were cleared before the next round.")
                                .formatted(Formatting.YELLOW),
                        false
                );
            }
        }

        int finalRemovedFish = removedFish;
        int finalAffectedPlayers = affectedPlayers;

        source.sendFeedback(
                () -> Text.literal("Cleared "
                                + finalRemovedFish
                                + " tournament fish from "
                                + finalAffectedPlayers
                                + " online players.")
                        .formatted(Formatting.GREEN),
                true
        );

        return removedFish;
    }

    private static int removeTournamentFishFromPlayer(ServerPlayerEntity player) {
        int removed = 0;

        removed += removeTournamentFishFromList(player.getInventory().main);
        removed += removeTournamentFishFromList(player.getInventory().offHand);
        removed += removeTournamentFishFromList(player.getInventory().armor);

        player.getInventory().markDirty();

        return removed;
    }

    private static int removeTournamentFishFromList(DefaultedList<ItemStack> stacks) {
        int removed = 0;

        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (!stack.contains(ModComponents.FISH_DATA)) {
                continue;
            }

            removed += stack.getCount();
            stacks.set(i, ItemStack.EMPTY);
        }

        return removed;
    }
}