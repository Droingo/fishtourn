package net.droingo.fishtourn.client;

import net.droingo.fishtourn.client.screen.ReelScreen;
import net.droingo.fishtourn.memorial.MemorialParticleRenderer;
import net.droingo.fishtourn.network.MemorialHeadEffectPayload;
import net.droingo.fishtourn.network.OpenReelScreenPayload;
import net.droingo.fishtourn.network.ReelFailPayload;
import net.droingo.fishtourn.network.ReelSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public final class ModClientNetworking {
    private static boolean registered = false;

    private ModClientNetworking() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ClientPlayNetworking.registerGlobalReceiver(OpenReelScreenPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new ReelScreen(payload.bobberId()));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ReelSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().currentScreen instanceof ReelScreen reelScreen) {
                    reelScreen.updateFromServer(
                            payload.bobberId(),
                            payload.progress(),
                            payload.tension(),
                            payload.difficulty()
                    );
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ReelFailPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().currentScreen instanceof ReelScreen) {
                    context.client().setScreen(null);
                }

                if (context.client().player != null) {
                    context.client().player.sendMessage(
                            Text.literal("The line snapped!"),
                            true
                    );
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(MemorialHeadEffectPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().world == null) {
                    return;
                }

                MemorialParticleRenderer.spawnMemorialHead(
                        context.client().world,
                        new Vec3d(payload.x(), payload.y(), payload.z()),
                        payload.yaw()
                );
            });
        });
    }
}