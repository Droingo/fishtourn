package net.droingo.fishtourn.client;

import net.droingo.fishtourn.client.screen.ReelScreen;
import net.droingo.fishtourn.network.OpenReelScreenPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ModClientNetworking {
    private ModClientNetworking() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(OpenReelScreenPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new ReelScreen(payload.bobberId()));
            });
        });
    }
}