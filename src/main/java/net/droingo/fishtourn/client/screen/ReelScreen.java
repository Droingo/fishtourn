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

    private boolean hasLastAngle = false;
    private double lastAngle = 0.0;
    private float localProgress = 0.0F;
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
        lastAngle = 0.0;
        localProgress = 0.0F;
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

        if (!completionSent && localProgress >= 100.0F) {
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

        if (radiusSquared > REEL_RADIUS * REEL_RADIUS * 3.5) {
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

        accumulatedAmount += movement * 3.5F;
        localProgress = Math.min(100.0F, localProgress + movement * 3.0F);

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

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Intentionally empty.
        // This prevents the normal screen darkening/blur background.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Do not render the dark/blurred menu background.
// Fishing should stay visually connected to the world behind the GUI.

        int centerX = width / 2;
        int centerY = height / 2;

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Spin the Reel").formatted(Formatting.AQUA, Formatting.BOLD),
                centerX,
                centerY - 82,
                0xFFFFFF
        );

        drawReel(context, centerX, centerY);

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Progress: " + Math.round(localProgress) + "%").formatted(Formatting.GREEN),
                centerX,
                centerY + 62,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Move your mouse in circles inside the reel").formatted(Formatting.GRAY),
                centerX,
                centerY + 78,
                0xFFFFFF
        );


    }

    private void drawReel(DrawContext context, int centerX, int centerY) {
        int outerColor = 0xFF1E90FF;
        int innerColor = 0xFF0B3D5C;
        int progressColor = 0xFF55FF55;

        context.fill(centerX - REEL_RADIUS, centerY - REEL_RADIUS, centerX + REEL_RADIUS, centerY + REEL_RADIUS, 0xAA061A2A);

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

        int progressWidth = (int) ((REEL_RADIUS * 2) * (localProgress / 100.0F));

        context.fill(
                centerX - REEL_RADIUS,
                centerY + REEL_RADIUS + 12,
                centerX - REEL_RADIUS + progressWidth,
                centerY + REEL_RADIUS + 18,
                progressColor
        );

        context.drawBorder(
                centerX - REEL_RADIUS,
                centerY + REEL_RADIUS + 12,
                REEL_RADIUS * 2,
                6,
                0xFFFFFFFF
        );
    }

    @Override
    public boolean shouldPause() {
        return false;
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
}