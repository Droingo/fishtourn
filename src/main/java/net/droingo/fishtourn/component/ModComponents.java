package net.droingo.fishtourn.component;

import net.droingo.fishtourn.FishingTournament;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModComponents {
    public static final ComponentType<FishDataComponent> FISH_DATA = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(FishingTournament.MOD_ID, "fish_data"),
            ComponentType.<FishDataComponent>builder()
                    .codec(FishDataComponent.CODEC)
                    .build()
    );

    private ModComponents() {
    }

    public static void register() {
        FishingTournament.LOGGER.info("Registering {} components", FishingTournament.MOD_ID);
    }
}