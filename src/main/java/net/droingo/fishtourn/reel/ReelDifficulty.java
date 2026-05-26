package net.droingo.fishtourn.reel;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;

public enum ReelDifficulty {
    COMMON("Common", Formatting.WHITE, 1.15F, 0.45F, 0.25F, 0.06D),
    UNCOMMON("Uncommon", Formatting.GREEN, 1.00F, 0.65F, 0.35F, 0.09D),
    RARE("Rare", Formatting.AQUA, 0.82F, 0.90F, 0.48F, 0.12D),
    LEGENDARY("Legendary", Formatting.LIGHT_PURPLE, 0.62F, 1.25F, 0.65F, 0.16D);

    private final String displayName;
    private final Formatting formatting;
    private final float progressMultiplier;
    private final float tensionGain;
    private final float tensionDrop;
    private final double slackPullbackMax;

    ReelDifficulty(
            String displayName,
            Formatting formatting,
            float progressMultiplier,
            float tensionGain,
            float tensionDrop,
            double slackPullbackMax
    ) {
        this.displayName = displayName;
        this.formatting = formatting;
        this.progressMultiplier = progressMultiplier;
        this.tensionGain = tensionGain;
        this.tensionDrop = tensionDrop;
        this.slackPullbackMax = slackPullbackMax;
    }

    public String displayName() {
        return displayName;
    }

    public Formatting formatting() {
        return formatting;
    }

    public float progressMultiplier() {
        return progressMultiplier;
    }

    public float tensionGain() {
        return tensionGain;
    }

    public float tensionDrop() {
        return tensionDrop;
    }

    public double slackPullbackMax() {
        return slackPullbackMax;
    }

    public static ReelDifficulty roll(Random random) {
        int roll = random.nextInt(100);

        if (roll < 55) {
            return COMMON;
        }

        if (roll < 82) {
            return UNCOMMON;
        }

        if (roll < 96) {
            return RARE;
        }

        return LEGENDARY;
    }
}