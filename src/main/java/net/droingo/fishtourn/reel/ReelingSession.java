package net.droingo.fishtourn.reel;

import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class ReelingSession {
    private final UUID playerUuid;
    private final int bobberId;
    private final Vec3d startPosition;
    private final ReelDifficulty difficulty;

    private float progress;
    private float tension;
    private int lastInputAge;
    private int overTensionTicks;

    public ReelingSession(UUID playerUuid, int bobberId, int playerAge, Vec3d startPosition, ReelDifficulty difficulty) {
        this.playerUuid = playerUuid;
        this.bobberId = bobberId;
        this.startPosition = startPosition;
        this.difficulty = difficulty;
        this.progress = 0.0F;
        this.tension = 55.0F;
        this.lastInputAge = playerAge;
        this.overTensionTicks = 0;
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    public int bobberId() {
        return bobberId;
    }

    public Vec3d startPosition() {
        return startPosition;
    }

    public ReelDifficulty difficulty() {
        return difficulty;
    }

    public float progress() {
        return progress;
    }

    public float tension() {
        return tension;
    }

    public int lastInputAge() {
        return lastInputAge;
    }

    public int overTensionTicks() {
        return overTensionTicks;
    }

    public void addReelInput(float amount, int playerAge) {
        progress = Math.min(100.0F, progress + amount * difficulty.progressMultiplier());
        tension = Math.min(100.0F, tension + amount * difficulty.tensionGain());
        lastInputAge = playerAge;
    }

    public void tickTension(int playerAge) {
        int ageSinceInput = playerAge - lastInputAge;

        if (ageSinceInput > 8) {
            // Player stopped reeling. Tension drops, but progress stays.
            tension = Math.max(0.0F, tension - difficulty.tensionDrop());
        } else {
            // While actively reeling, tension tries to settle toward a playable middle.
            if (tension > 65.0F) {
                tension = Math.max(65.0F, tension - 0.08F);
            } else if (tension < 45.0F) {
                tension = Math.min(45.0F, tension + 0.08F);
            }
        }

        if (tension >= 99.5F) {
            overTensionTicks++;
        } else {
            overTensionTicks = Math.max(0, overTensionTicks - 2);
        }
    }

    public boolean hasLineSnapped() {
        return overTensionTicks >= 18;
    }

    public boolean isComplete() {
        return progress >= 100.0F;
    }
}