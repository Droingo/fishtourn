package net.droingo.fishtourn.client;

import net.droingo.fishtourn.item.ModItems;
import net.droingo.fishtourn.network.ReelInputPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public final class ReelingClientInput {
    private static boolean hasLastAngle = false;
    private static double lastAngle = 0.0;
    private static float accumulatedAmount = 0.0F;
    private static int packetCooldown = 0;

    private ReelingClientInput() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ReelingClientInput::tick);
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            reset();
            return;
        }

        if (!isHoldingTournamentRod(client)) {
            reset();
            return;
        }

        if (packetCooldown > 0) {
            packetCooldown--;
        }

        double mouseX = client.mouse.getX();
        double mouseY = client.mouse.getY();

        double centerX = client.getWindow().getWidth() / 2.0;
        double centerY = client.getWindow().getHeight() / 2.0;

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;

        double radiusSquared = dx * dx + dy * dy;

        if (radiusSquared < 400.0) {
            hasLastAngle = false;
            return;
        }

        double angle = Math.atan2(dy, dx);

        if (!hasLastAngle) {
            lastAngle = angle;
            hasLastAngle = true;
            return;
        }

        double delta = wrapAngle(angle - lastAngle);
        lastAngle = angle;

        float movement = (float) Math.abs(delta);

        // Ignore tiny jitter and wild flicks. We want controlled circular movement.
        if (movement < 0.03F || movement > 0.85F) {
            return;
        }

        accumulatedAmount += movement * 8.0F;

        if (accumulatedAmount >= 1.0F && packetCooldown <= 0) {
            boolean clockwise = delta > 0.0;

            ClientPlayNetworking.send(new ReelInputPayload(
                    Math.min(accumulatedAmount, 3.0F),
                    clockwise
            ));

            accumulatedAmount = 0.0F;
            packetCooldown = 2;
        }
    }

    private static boolean isHoldingTournamentRod(MinecraftClient client) {
        ItemStack mainHand = client.player.getMainHandStack();
        ItemStack offHand = client.player.getOffHandStack();

        return mainHand.isOf(ModItems.TOURNAMENT_ROD)
                || offHand.isOf(ModItems.TOURNAMENT_ROD);
    }

    private static double wrapAngle(double angle) {
        while (angle <= -Math.PI) {
            angle += Math.PI * 2.0;
        }

        while (angle > Math.PI) {
            angle -= Math.PI * 2.0;
        }

        return angle;
    }

    private static void reset() {
        hasLastAngle = false;
        lastAngle = 0.0;
        accumulatedAmount = 0.0F;
        packetCooldown = 0;
    }
}