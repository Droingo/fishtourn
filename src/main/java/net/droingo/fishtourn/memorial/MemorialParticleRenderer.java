package net.droingo.fishtourn.memorial;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class MemorialParticleRenderer {
    private static final String SKIN_RESOURCE =
            "assets/fishtourn/textures/memorial/FieryTNT.png";

    private static final List<MemorialHeadPattern.HeadPixel> HEAD_PIXELS =
            MemorialHeadPattern.loadHeadPixels(SKIN_RESOURCE);

    private static final List<ActiveEffect> ACTIVE_EFFECTS = new ArrayList<>();

    private MemorialParticleRenderer() {
    }

    public static void registerClientTick() {
        ClientTickEvents.END_CLIENT_TICK.register(MemorialParticleRenderer::tick);
    }

    public static void spawnMemorialHead(ClientWorld world, Vec3d center, float yawDegrees) {
        ACTIVE_EFFECTS.add(new ActiveEffect(world, center, yawDegrees));
    }

    private static void tick(MinecraftClient client) {
        if (client.world == null) {
            ACTIVE_EFFECTS.clear();
            return;
        }

        Iterator<ActiveEffect> iterator = ACTIVE_EFFECTS.iterator();

        while (iterator.hasNext()) {
            ActiveEffect effect = iterator.next();

            if (effect.world != client.world) {
                iterator.remove();
                continue;
            }

            effect.tick();

            if (effect.isFinished()) {
                iterator.remove();
            }
        }
    }

    private static final class ActiveEffect {
        private static final int DURATION_TICKS = 330;

        private final ClientWorld world;
        private final Vec3d center;
        private final float startYawDegrees;
        private final Random random = new Random();

        private int age = 0;

        private ActiveEffect(ClientWorld world, Vec3d center, float startYawDegrees) {
            this.world = world;
            this.center = center;
            this.startYawDegrees = startYawDegrees;
        }

        private void tick() {
            age++;

            spawnComplimentaryFireworks();

            // Render the head every other tick so it stays visible without becoming too dense.
            if (age % 2 != 0) {
                return;
            }

            double fade = 1.0D - age / (double) DURATION_TICKS;

            double pixelSpacing = 0.42D;
            double clusterSpread = 0.045D;

            float rotationDegrees = startYawDegrees + age * 1.1F;
            float yawRadians = rotationDegrees * MathHelper.RADIANS_PER_DEGREE;

            float pitchDegrees = -45.0F;
            float pitchRadians = pitchDegrees * MathHelper.RADIANS_PER_DEGREE;

            for (MemorialHeadPattern.HeadPixel pixel : HEAD_PIXELS) {
                Vec3d localOffset = MemorialHeadPattern.getCubeOffset(pixel, pixelSpacing);
                Vec3d pitchedOffset = rotateX(localOffset, pitchRadians);
                Vec3d rotatedOffset = rotateY(pitchedOffset, yawRadians);

                Vector3f color = new Vector3f(
                        glow(pixel.red(), pixel.overlay()),
                        glow(pixel.green(), pixel.overlay()),
                        glow(pixel.blue(), pixel.overlay())
                );

                float particleSize = pixel.overlay() ? 1.65F : 1.45F;
                DustParticleEffect dust = new DustParticleEffect(color, particleSize);

                int particleCount = pixel.overlay() ? 2 : 1;

                for (int i = 0; i < particleCount; i++) {
                    double px = center.x + rotatedOffset.x + (random.nextDouble() - 0.5D) * clusterSpread;
                    double py = center.y + rotatedOffset.y + (random.nextDouble() - 0.5D) * clusterSpread;
                    double pz = center.z + rotatedOffset.z + (random.nextDouble() - 0.5D) * clusterSpread;

                    world.addParticle(
                            dust,
                            px,
                            py,
                            pz,
                            0.0D,
                            0.002D * fade,
                            0.0D
                    );

                    if (random.nextFloat() < 0.012F) {
                        world.addParticle(
                                ParticleTypes.END_ROD,
                                px,
                                py,
                                pz,
                                0.0D,
                                0.004D,
                                0.0D
                        );
                    }
                }
            }
        }

        private void spawnComplimentaryFireworks() {
            if (age % 22 != 0) {
                return;
            }

            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = 2.8D + random.nextDouble() * 2.2D;

            double x = center.x + Math.cos(angle) * radius;
            double y = center.y + random.nextDouble() * 2.6D - 0.8D;
            double z = center.z + Math.sin(angle) * radius;

            world.addParticle(
                    ParticleTypes.FLASH,
                    x,
                    y,
                    z,
                    0.0D,
                    0.0D,
                    0.0D
            );

            world.addParticle(
                    ParticleTypes.FIREWORK,
                    x,
                    y,
                    z,
                    0.0D,
                    0.04D,
                    0.0D
            );

            for (int i = 0; i < 35; i++) {
                double burstAngle = random.nextDouble() * Math.PI * 2.0D;
                double burstUp = random.nextDouble() * 0.9D - 0.2D;
                double speed = 0.08D + random.nextDouble() * 0.08D;

                world.addParticle(
                        ParticleTypes.FIREWORK,
                        x,
                        y,
                        z,
                        Math.cos(burstAngle) * speed,
                        burstUp * speed,
                        Math.sin(burstAngle) * speed
                );
            }

            for (int i = 0; i < 12; i++) {
                world.addParticle(
                        ParticleTypes.END_ROD,
                        x,
                        y,
                        z,
                        (random.nextDouble() - 0.5D) * 0.08D,
                        (random.nextDouble() - 0.2D) * 0.08D,
                        (random.nextDouble() - 0.5D) * 0.08D
                );
            }

            if (age % 44 == 0) {
                playComplimentaryFireworkSounds();
            }
        }

        private void playComplimentaryFireworkSounds() {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null) {
                return;
            }

            double soundX = client.player.getX();
            double soundY = client.player.getY();
            double soundZ = client.player.getZ();

            world.playSound(
                    soundX,
                    soundY,
                    soundZ,
                    SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,
                    SoundCategory.PLAYERS,
                    0.35F,
                    1.15F + random.nextFloat() * 0.35F,
                    false
            );

            world.playSound(
                    soundX,
                    soundY,
                    soundZ,
                    SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    SoundCategory.PLAYERS,
                    0.45F,
                    1.25F + random.nextFloat() * 0.45F,
                    false
            );

            world.playSound(
                    soundX,
                    soundY,
                    soundZ,
                    SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT,
                    SoundCategory.PLAYERS,
                    0.25F,
                    1.6F + random.nextFloat() * 0.35F,
                    false
            );
        }

        private boolean isFinished() {
            return age >= DURATION_TICKS;
        }

        private static Vec3d rotateY(Vec3d vector, float yawRadians) {
            double cos = Math.cos(yawRadians);
            double sin = Math.sin(yawRadians);

            double x = vector.x * cos - vector.z * sin;
            double z = vector.x * sin + vector.z * cos;

            return new Vec3d(x, vector.y, z);
        }

        private static Vec3d rotateX(Vec3d vector, float pitchRadians) {
            double cos = Math.cos(pitchRadians);
            double sin = Math.sin(pitchRadians);

            double y = vector.y * cos - vector.z * sin;
            double z = vector.y * sin + vector.z * cos;

            return new Vec3d(vector.x, y, z);
        }

        private static float glow(float value, boolean overlay) {
            float boosted = value * 1.45F + 0.18F;

            if (overlay) {
                boosted = value * 1.65F + 0.25F;
            }

            return Math.min(1.0F, Math.max(0.18F, boosted));
        }
    }
}