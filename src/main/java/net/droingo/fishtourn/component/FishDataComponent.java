package net.droingo.fishtourn.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Locale;

public record FishDataComponent(
        String species,
        double lengthCm,
        double weightKg,
        String rarity,
        int score
) {
    public static final double KG_TO_LB = 2.2046226218;

    public static final Codec<FishDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("species").forGetter(FishDataComponent::species),
            Codec.DOUBLE.fieldOf("length_cm").forGetter(FishDataComponent::lengthCm),
            Codec.DOUBLE.fieldOf("weight_kg").forGetter(FishDataComponent::weightKg),
            Codec.STRING.fieldOf("rarity").forGetter(FishDataComponent::rarity),
            Codec.INT.fieldOf("score").forGetter(FishDataComponent::score)
    ).apply(instance, FishDataComponent::new));

    public double weightLb() {
        return weightKg * KG_TO_LB;
    }

    public String formattedLength() {
        return String.format(Locale.ROOT, "%.1f cm", lengthCm);
    }

    public String formattedWeightKg() {
        return String.format(Locale.ROOT, "%.2f kg", weightKg);
    }

    public String formattedWeightLb() {
        return String.format(Locale.ROOT, "%.2f lb", weightLb());
    }

    public String formattedWeightBoth() {
        return formattedWeightKg() + " / " + formattedWeightLb();
    }
}