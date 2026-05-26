package net.droingo.fishtourn.client.screen;

import net.droingo.fishtourn.network.ReelCompletePayload;
import net.droingo.fishtourn.network.ReelInputPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ReelScreen extends Screen {
    private static final int REEL_RADIUS = 42;
    private static final int INNER_RADIUS = 14;

    private final int bobberId;

    private float serverProgress = 0.0F;
    private float serverTension = 55.0F;
    private String serverDifficulty = "Common";

    private boolean hasLastAngle = false;
    private double lastAngle = 0.0D;
    private float accumulatedAmount = 0.0F;
    private int packetCooldown = 0;
    private boolean completionSent = false;

    public ReelScreen(int bobberId) {
        super(Text.literal("Reeling"));
        this.bobberId = bobberId;
    }

    @Override
    protected void init() {
        super.init();
        hasLastAngle = false;
        lastAngle = 0.0D;
        accumulatedAmount = 0.0F;
        packetCooldown = 0;
        completionSent = false;
    }

    @Override
    public void tick() {
        super.tick();

        if (packetCooldown > 0) {
            packetCooldown--;
        }

        if (!completionSent && serverProgress >= 100.0F) {
            completionSent = true;
            ClientPlayNetworking.send(new ReelCompletePayload(bobberId));
            close();
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);

        int centerX = width / 2;
        int centerY = height / 2;

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double radiusSquared = dx * dx + dy * dy;

        if (radiusSquared < INNER_RADIUS * INNER_RADIUS) {
            hasLastAngle = false;
            return;
        }

        if (radiusSquared > REEL_RADIUS * REEL_RADIUS * 3.5D) {
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

        if (movement < 0.025F || movement > 0.95F) {
            return;
        }

        accumulatedAmount += movement * 4.5F;

        if (accumulatedAmount >= 1.0F && packetCooldown <= 0) {
            boolean clockwise = delta > 0.0D;
            float sentAmount = Math.min(accumulatedAmount, 3.0F);

            ClientPlayNetworking.send(new ReelInputPayload(sentAmount, clockwise));

            accumulatedAmount = 0.0F;
            packetCooldown = 2;
        }
    }

    public void updateFromServer(int bobberId, float progress, float tension, String difficulty) {
        if (this.bobberId != bobberId) {
            return;
        }

        this.serverProgress = Math.max(0.0F, Math.min(100.0F, progress));
        this.serverTension = Math.max(0.0F, Math.min(100.0F, tension));
        this.serverDifficulty = difficulty;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Keep the world visible behind the reel GUI.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int centerX = width / 2;
        int centerY = height / 2;

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Spin the Reel").formatted(Formatting.AQUA, Formatting.BOLD),
                centerX,
                centerY - 96,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Fight: " + serverDifficulty).formatted(getDifficultyFormatting()),
                centerX,
                centerY - 80,
                0xFFFFFF
        );

        drawReel(context, centerX, centerY);

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Progress: " + Math.round(serverProgress) + "%").formatted(Formatting.GREEN),
                centerX,
                centerY + 84,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Tension: " + Math.round(serverTension) + "%").formatted(getTensionFormatting()),
                centerX,
                centerY + 100,
                0xFFFFFF
        );

        String warning = getWarningText();

        if (!warning.isEmpty()) {
            context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.literal(warning).formatted(getWarningFormatting(), Formatting.BOLD),
                    centerX,
                    centerY + 118,
                    0xFFFFFF
            );
        } else {
            context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.literal("Spin steadily. Stop briefly to lower tension.").formatted(Formatting.GRAY),
                    centerX,
                    centerY + 118,
                    0xFFFFFF
            );
        }
    }

    private void drawReel(DrawContext context, int centerX, int centerY) {
        int outerColor = 0xFF1E90FF;
        int innerColor = 0xFF0B3D5C;
        int progressColor = 0xFF55FF55;
        int tensionColor = getTensionBarColor();

        context.fill(
                centerX - REEL_RADIUS,
                centerY - REEL_RADIUS,
                centerX + REEL_RADIUS,
                centerY + REEL_RADIUS,
                0xAA061A2A
        );

        context.drawBorder(
                centerX - REEL_RADIUS,
                centerY - REEL_RADIUS,
                REEL_RADIUS * 2,
                REEL_RADIUS * 2,
                outerColor
        );

        context.drawBorder(
                centerX - INNER_RADIUS,
                centerY - INNER_RADIUS,
                INNER_RADIUS * 2,
                INNER_RADIUS * 2,
                innerColor
        );

        int progressWidth = (int) ((REEL_RADIUS * 2) * (serverProgress / 100.0F));
        int tensionWidth = (int) ((REEL_RADIUS * 2) * (serverTension / 100.0F));

        int barX = centerX - REEL_RADIUS;
        int barY = centerY + REEL_RADIUS + 18;
        int barWidth = REEL_RADIUS * 2;

        context.fill(barX, barY, barX + progressWidth, barY + 6, progressColor);
        context.drawBorder(barX, barY, barWidth, 6, 0xFFFFFFFF);

        context.fill(barX, barY + 13, barX + tensionWidth, barY + 19, tensionColor);
        context.drawBorder(barX, barY + 13, barWidth, 6, 0xFFFFFFFF);
    }

    private Formatting getDifficultyFormatting() {
        return switch (serverDifficulty.toLowerCase()) {
            case "uncommon" -> Formatting.GREEN;
            case "rare" -> Formatting.AQUA;
            case "legendary" -> Formatting.LIGHT_PURPLE;
            default -> Formatting.WHITE;
        };
    }

    private Formatting getTensionFormatting() {
        if (serverTension >= 90.0F) {
            return Formatting.RED;
        }

        if (serverTension >= 75.0F) {
            return Formatting.GOLD;
        }

        if (serverTension < 25.0F) {
            return Formatting.YELLOW;
        }

        return Formatting.GREEN;
    }

    private int getTensionBarColor() {
        if (serverTension >= 90.0F) {
            return 0xFFFF3333;
        }

        if (serverTension >= 75.0F) {
            return 0xFFFFAA00;
        }

        if (serverTension < 25.0F) {
            return 0xFFFFFF55;
        }

        return 0xFF55FF55;
    }

    private String getWarningText() {
        if (serverTension >= 98.0F) {
            return "LINE ABOUT TO SNAP!";
        }

        if (serverTension >= 90.0F) {
            return "Ease up!";
        }

        if (serverTension < 20.0F) {
            return "Line is slack!";
        }

        return "";
    }

    private Formatting getWarningFormatting() {
        if (serverTension >= 90.0F) {
            return Formatting.RED;
        }

        if (serverTension < 20.0F) {
            return Formatting.YELLOW;
        }

        return Formatting.GRAY;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static double wrapAngle(double angle) {
        while (angle <= -Math.PI) {
            angle += Math.PI * 2.0D;
        }

        while (angle > Math.PI) {
            angle -= Math.PI * 2.0D;
        }

        return angle;
    }
}