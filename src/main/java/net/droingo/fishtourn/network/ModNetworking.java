package net.droingo.fishtourn.network;

import net.droingo.fishtourn.reel.ReelingManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(OpenReelScreenPayload.ID, OpenReelScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ReelInputPayload.ID, ReelInputPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ReelCompletePayload.ID, ReelCompletePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ReelInputPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ReelingManager.receiveInput(context.player(), payload.amount(), payload.clockwise());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ReelCompletePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ReelingManager.completeReel(context.player(), payload.bobberId());
            });
        });
    }
}