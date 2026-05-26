package net.droingo.fishtourn.reel;

import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class ReelingSession {
    private final UUID playerUuid;
    private final int bobberId;
    private final Vec3d startPosition;

    private float progress;
    private int lastInputAge;

    public ReelingSession(UUID playerUuid, int bobberId, int playerAge, Vec3d startPosition) {
        this.playerUuid = playerUuid;
        this.bobberId = bobberId;
        this.startPosition = startPosition;
        this.progress = 0.0F;
        this.lastInputAge = playerAge;
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

    public float progress() {
        return progress;
    }

    public int lastInputAge() {
        return lastInputAge;
    }

    public void addProgress(float amount, int playerAge) {
        progress = Math.min(100.0F, progress + amount);
        lastInputAge = playerAge;
    }

    public void decay() {
        progress = Math.max(0.0F, progress - 0.25F);
    }

    public boolean isComplete() {
        return progress >= 100.0F;
    }
}