package net.droingo.fishtourn.block;

import net.droingo.fishtourn.component.FishDataComponent;
import net.droingo.fishtourn.component.ModComponents;
import net.droingo.fishtourn.tournament.TournamentEntry;
import net.droingo.fishtourn.tournament.TournamentManager;
import net.droingo.fishtourn.tournament.TournamentState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.Locale;
import java.util.Optional;

public class TournamentSubmissionBlock extends Block {
    public TournamentSubmissionBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ItemActionResult onUseWithItem(
            ItemStack stack,
            BlockState state,
            World world,
            net.minecraft.util.math.BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit
    ) {
        if (world.isClient()) {
            return ItemActionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.isEmpty()) {
            sendPlayerSubmissionSummary(serverPlayer);
            return ItemActionResult.SUCCESS;
        }

        FishDataComponent fishData = stack.get(ModComponents.FISH_DATA);

        if (fishData == null) {
            serverPlayer.sendMessage(
                    Text.literal("Submit a tournament fish here, or use an empty hand to check your submissions.")
                            .formatted(Formatting.GRAY),
                    true
            );

            return ItemActionResult.SUCCESS;
        }

        TournamentManager.SubmissionResult result = TournamentManager.submitFish(serverPlayer, fishData);

        if (!result.tournamentActive()) {
            serverPlayer.sendMessage(
                    Text.literal("There is no active fishing tournament.")
                            .formatted(Formatting.RED),
                    true
            );

            return ItemActionResult.SUCCESS;
        }

        if (!result.accepted()) {
            if (result.limitReached()) {
                serverPlayer.sendMessage(
                        Text.literal("You have already submitted "
                                        + TournamentState.MAX_SUBMISSIONS_PER_PLAYER
                                        + " fish this round.")
                                .formatted(Formatting.RED),
                        true
                );
            } else {
                serverPlayer.sendMessage(
                        Text.literal("This fish could not be submitted.")
                                .formatted(Formatting.RED),
                        true
                );
            }

            return ItemActionResult.SUCCESS;
        }

        stack.decrement(1);

        if (result.newPersonalBest()) {
            serverPlayer.sendMessage(
                    Text.literal("Fish submitted. This is your best submission so far. ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal(result.remainingSubmissions()
                                            + " of "
                                            + TournamentState.MAX_SUBMISSIONS_PER_PLAYER
                                            + " submissions remaining.")
                                    .formatted(Formatting.GOLD)),
                    true
            );
        } else {
            serverPlayer.sendMessage(
                    Text.literal("Fish submitted, but your previous submission was better. ")
                            .formatted(Formatting.YELLOW)
                            .append(Text.literal(result.remainingSubmissions()
                                            + " of "
                                            + TournamentState.MAX_SUBMISSIONS_PER_PLAYER
                                            + " submissions remaining.")
                                    .formatted(Formatting.GOLD)),
                    true
            );
        }

        return ItemActionResult.SUCCESS;
    }

    private static void sendPlayerSubmissionSummary(ServerPlayerEntity player) {
        if (!TournamentManager.isActive(player.getServer())) {
            player.sendMessage(
                    Text.literal("There is no active fishing tournament.")
                            .formatted(Formatting.RED),
                    false
            );

            return;
        }

        int submissionCount = TournamentManager.getPlayerSubmissionCount(player.getServer(), player.getUuid());
        int remaining = TournamentManager.getPlayerRemainingSubmissionCount(player.getServer(), player.getUuid());

        Optional<TournamentEntry> bestEntry = TournamentManager.getPlayerBestEntry(
                player.getServer(),
                player.getUuid()
        );

        if (submissionCount <= 0 || bestEntry.isEmpty()) {
            player.sendMessage(
                    Text.literal("You have not submitted any fish this round. ")
                            .formatted(Formatting.YELLOW)
                            .append(Text.literal(TournamentState.MAX_SUBMISSIONS_PER_PLAYER + " submissions available.")
                                    .formatted(Formatting.GOLD)),
                    false
            );

            return;
        }

        TournamentEntry entry = bestEntry.get();

        player.sendMessage(
                Text.literal("Your tournament submissions: "
                                + submissionCount
                                + "/"
                                + TournamentState.MAX_SUBMISSIONS_PER_PLAYER)
                        .formatted(Formatting.AQUA)
                        .append(Text.literal(" | " + remaining + " remaining")
                                .formatted(Formatting.GOLD)),
                false
        );

        player.sendMessage(
                Text.literal("Best so far: "
                                + entry.species()
                                + " | "
                                + entry.catchZone()
                                + " | "
                                + entry.rarity()
                                + " | "
                                + formatKg(entry.weightKg())
                                + " / "
                                + formatLb(entry.weightKg())
                                + " | Score "
                                + entry.score())
                        .formatted(getRarityFormatting(entry.rarity())),
                false
        );
    }

    private static String formatKg(double weightKg) {
        return String.format(Locale.ROOT, "%.2f kg", weightKg);
    }

    private static String formatLb(double weightKg) {
        return String.format(Locale.ROOT, "%.2f lb", weightKg * FishDataComponent.KG_TO_LB);
    }

    private static Formatting getRarityFormatting(String rarity) {
        return switch (rarity.toLowerCase(Locale.ROOT)) {
            case "uncommon" -> Formatting.GREEN;
            case "rare" -> Formatting.AQUA;
            case "legendary" -> Formatting.LIGHT_PURPLE;
            default -> Formatting.WHITE;
        };
    }
}