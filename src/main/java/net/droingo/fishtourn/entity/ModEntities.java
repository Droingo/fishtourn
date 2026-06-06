package net.droingo.fishtourn.entity;

import net.droingo.fishtourn.FishingTournament;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
    public static final EntityType<WakeSplashEntity> WAKE_SPLASH = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(FishingTournament.MOD_ID, "wake_splash"),
            EntityType.Builder.<WakeSplashEntity>create(
                            WakeSplashEntity::new,
                            SpawnGroup.MISC
                    )
                    .dimensions(0.08F, 0.08F)
                    .maxTrackingRange(16)
                    .trackingTickInterval(1)
                    .build()
    );

    private ModEntities() {
    }

    public static void register() {
        FishingTournament.LOGGER.info("Registering {} entities", FishingTournament.MOD_ID);
    }
}