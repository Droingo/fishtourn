package net.droingo.fishtourn.memorial;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
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
        private static final int DURATION_TICKS = 110;

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

            // Spawn every other tick to keep it big without being too heavy.
            if (age % 2 != 0) {
                return;
            }

            double fade = 1.0D - age / (double) DURATION_TICKS;

            double pixelSpacing = 0.38D;
            double clusterSpread = 0.075D;

            float rotationDegrees = startYawDegrees + age * 2.2F;
            float yawRadians = rotationDegrees * MathHelper.RADIANS_PER_DEGREE;

            for (MemorialHeadPattern.HeadPixel pixel : HEAD_PIXELS) {
                Vec3d localOffset = MemorialHeadPattern.getCubeOffset(pixel, pixelSpacing);
                Vec3d rotatedOffset = rotateY(localOffset, yawRadians);

                Vector3f color = new Vector3f(
                        brighten(pixel.red(), pixel.overlay()),
                        brighten(pixel.green(), pixel.overlay()),
                        brighten(pixel.blue(), pixel.overlay())
                );

                float particleSize = pixel.overlay() ? 1.55F : 1.35F;
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
                }
            }
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

        private static float brighten(float value, boolean overlay) {
            if (!overlay) {
                return Math.max(0.08F, value);
            }

            return Math.min(1.0F, Math.max(0.08F, value * 1.12F + 0.04F));
        }
    }
}