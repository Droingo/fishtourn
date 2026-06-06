package net.droingo.fishtourn;

import net.droingo.fishtourn.client.ModClientNetworking;
import net.droingo.fishtourn.component.FishDataComponent;
import net.droingo.fishtourn.component.ModComponents;
import net.droingo.fishtourn.fish.FishItemFactory;
import net.droingo.fishtourn.memorial.MemorialParticleRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.droingo.fishtourn.client.render.WakeSplashEntityRenderer;
import net.droingo.fishtourn.entity.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class FishingTournamentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModClientNetworking.register();

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            FishDataComponent fishData = stack.get(ModComponents.FISH_DATA);

            if (fishData == null) {
                return;
            }

            EntityRendererRegistry.register(ModEntities.WAKE_SPLASH, WakeSplashEntityRenderer::new);
            MemorialParticleRenderer.registerClientTick();
            lines.add(Text.literal("Fish Stats").formatted(Formatting.AQUA));
            lines.add(Text.literal("Species: " + fishData.species()).formatted(Formatting.GRAY));
            lines.add(Text.literal("Zone: " + fishData.catchZone()).formatted(Formatting.BLUE));
            lines.add(Text.literal("Length: " + fishData.formattedLength()).formatted(Formatting.GRAY));
            lines.add(Text.literal("Weight: " + fishData.formattedWeightBoth()).formatted(Formatting.GRAY));
            lines.add(Text.literal("Rarity: " + fishData.rarity())
                    .formatted(FishItemFactory.getRarityFormatting(fishData.rarity())));
            lines.add(Text.literal("Score: " + fishData.score()).formatted(Formatting.GREEN));
        });
    }
}