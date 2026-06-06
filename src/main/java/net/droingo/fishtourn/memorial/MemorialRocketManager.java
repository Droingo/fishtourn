package net.droingo.fishtourn.memorial;

import net.droingo.fishtourn.network.MemorialHeadEffectPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class MemorialRocketManager {
    private static final List<ActiveRocket> ACTIVE_ROCKETS = new ArrayList<>();
    private static final List<ActiveMemorialLight> ACTIVE_LIGHTS = new ArrayList<>();
    private static final List<ActiveBurstLighting> ACTIVE_BURST_LIGHTING = new ArrayList<>();

    private MemorialRocketManager() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(MemorialRocketManager::tick);
    }

    public static void launch(ServerWorld world, Vec3d startPos, Vec3d velocity, float displayYaw) {
        ACTIVE_ROCKETS.add(new ActiveRocket(world, startPos, velocity, displayYaw));

        world.playSound(
                null,
                startPos.x,
                startPos.y,
                startPos.z,
                SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH,
                SoundCategory.PLAYERS,
                1.0F,
                0.9F
        );
    }

    private static void tick(MinecraftServer server) {
        Iterator<ActiveRocket> rocketIterator = ACTIVE_ROCKETS.iterator();

        while (rocketIterator.hasNext()) {
            ActiveRocket rocket = rocketIterator.next();
            rocket.tick();

            if (rocket.finished) {
                rocketIterator.remove();
            }
        }

        Iterator<ActiveBurstLighting> burstIterator = ACTIVE_BURST_LIGHTING.iterator();

        while (burstIterator.hasNext()) {
            ActiveBurstLighting burstLighting = burstIterator.next();
            burstLighting.tick();

            if (burstLighting.finished) {
                burstIterator.remove();
            }
        }

        Iterator<ActiveMemorialLight> lightIterator = ACTIVE_LIGHTS.iterator();

        while (lightIterator.hasNext()) {
            ActiveMemorialLight light = lightIterator.next();
            light.tick();

            if (light.finished) {
                lightIterator.remove();
            }
        }
    }

    private static void placeTemporaryLight(ServerWorld world, BlockPos pos, int durationTicks, int level) {
        if (!world.isChunkLoaded(pos)) {
            return;
        }

        BlockState currentState = world.getBlockState(pos);

        // Never overwrite real blocks. Only use air or an existing light block.
        if (!currentState.isAir() && !currentState.isOf(Blocks.LIGHT)) {
            return;
        }

        ActiveMemorialLight existing = findExistingLight(world, pos);

        if (existing != null) {
            existing.refresh(durationTicks, level);
            return;
        }

        BlockState lightState = Blocks.LIGHT
                .getDefaultState()
                .with(Properties.LEVEL_15, Math.max(1, Math.min(15, level)));

        world.setBlockState(pos, lightState, 3);
        ACTIVE_LIGHTS.add(new ActiveMemorialLight(world, pos, durationTicks));
    }

    private static ActiveMemorialLight findExistingLight(ServerWorld world, BlockPos pos) {
        for (ActiveMemorialLight light : ACTIVE_LIGHTS) {
            if (light.world == world && light.pos.equals(pos) && !light.finished) {
                return light;
            }
        }

        return null;
    }

    private static final class ActiveMemorialLight {
        private final ServerWorld world;
        private final BlockPos pos;

        private int remainingTicks;
        private boolean finished = false;

        private ActiveMemorialLight(ServerWorld world, BlockPos pos, int remainingTicks) {
            this.world = world;
            this.pos = pos;
            this.remainingTicks = remainingTicks;
        }

        private void refresh(int durationTicks, int level) {
            this.remainingTicks = Math.max(this.remainingTicks, durationTicks);

            if (!world.isChunkLoaded(pos)) {
                return;
            }

            if (!world.getBlockState(pos).isOf(Blocks.LIGHT)) {
                return;
            }

            BlockState lightState = Blocks.LIGHT
                    .getDefaultState()
                    .with(Properties.LEVEL_15, Math.max(1, Math.min(15, level)));

            world.setBlockState(pos, lightState, 3);
        }

        private void tick() {
            remainingTicks--;

            if (remainingTicks > 0) {
                return;
            }

            if (world.isChunkLoaded(pos) && world.getBlockState(pos).isOf(Blocks.LIGHT)) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            }

            finished = true;
        }
    }

    private static final class ActiveBurstLighting {
        private static final int TOTAL_DURATION_TICKS = 330;
        private static final int BURST_INTERVAL_TICKS = 22;
        private static final int BURST_LIGHT_DURATION_TICKS = 5;

        private static final int SKY_BURST_MIN_LIGHT = 7;
        private static final int SKY_BURST_MAX_LIGHT = 12;

        private static final int GROUND_BURST_MIN_LIGHT = 5;
        private static final int GROUND_BURST_MAX_LIGHT = 10;

        private final ServerWorld world;
        private final Vec3d center;
        private final Random random = new Random();

        private int age = 0;
        private boolean finished = false;

        private ActiveBurstLighting(ServerWorld world, Vec3d center) {
            this.world = world;
            this.center = center;
        }

        private void tick() {
            age++;

            if (age >= TOTAL_DURATION_TICKS) {
                finished = true;
                return;
            }

            if (age % BURST_INTERVAL_TICKS != 0) {
                return;
            }

            placeSkyBurstLights();
            placeGroundLightsNearViewers();
        }

        private void placeSkyBurstLights() {
            for (int i = 0; i < 4; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double radius = 1.8D + random.nextDouble() * 2.7D;

                BlockPos pos = BlockPos.ofFloored(
                        center.x + Math.cos(angle) * radius,
                        center.y + random.nextDouble() * 2.0D - 0.6D,
                        center.z + Math.sin(angle) * radius
                );

                placeTemporaryLight(
                        world,
                        pos,
                        BURST_LIGHT_DURATION_TICKS,
                        randomLightLevel(SKY_BURST_MIN_LIGHT, SKY_BURST_MAX_LIGHT)
                );
            }
        }

        private void placeGroundLightsNearViewers() {
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (player.squaredDistanceTo(center) > ActiveRocket.SEND_RADIUS * ActiveRocket.SEND_RADIUS) {
                    continue;
                }

                BlockPos base = player.getBlockPos();

                placeTemporaryLight(
                        world,
                        base.up(2),
                        BURST_LIGHT_DURATION_TICKS,
                        randomLightLevel(GROUND_BURST_MIN_LIGHT, GROUND_BURST_MAX_LIGHT)
                );

                if (random.nextBoolean()) {
                    placeTemporaryLight(
                            world,
                            base.add(2, 2, 0),
                            BURST_LIGHT_DURATION_TICKS,
                            randomLightLevel(GROUND_BURST_MIN_LIGHT, GROUND_BURST_MAX_LIGHT)
                    );

                    placeTemporaryLight(
                            world,
                            base.add(-2, 2, 0),
                            BURST_LIGHT_DURATION_TICKS,
                            randomLightLevel(GROUND_BURST_MIN_LIGHT, GROUND_BURST_MAX_LIGHT)
                    );
                } else {
                    placeTemporaryLight(
                            world,
                            base.add(0, 2, 2),
                            BURST_LIGHT_DURATION_TICKS,
                            randomLightLevel(GROUND_BURST_MIN_LIGHT, GROUND_BURST_MAX_LIGHT)
                    );

                    placeTemporaryLight(
                            world,
                            base.add(0, 2, -2),
                            BURST_LIGHT_DURATION_TICKS,
                            randomLightLevel(GROUND_BURST_MIN_LIGHT, GROUND_BURST_MAX_LIGHT)
                    );
                }
            }
        }

        private int randomLightLevel(int min, int max) {
            return min + random.nextInt(max - min + 1);
        }
    }

    private static final class ActiveRocket {
        private static final int EXPLODE_AGE_TICKS = 45;
        private static final int HEAD_DURATION_TICKS = 330;
        private static final int MAIN_BURST_LIGHT_DURATION_TICKS = 6;
        private static final double SEND_RADIUS = 384.0D;

        private final ServerWorld world;
        private final float displayYaw;

        private Vec3d position;
        private Vec3d velocity;
        private int age = 0;
        private boolean finished = false;

        private ActiveRocket(ServerWorld world, Vec3d position, Vec3d velocity, float displayYaw) {
            this.world = world;
            this.position = position;
            this.velocity = velocity;
            this.displayYaw = displayYaw;
        }

        private void tick() {
            age++;

            velocity = new Vec3d(
                    velocity.x * 0.96D,
                    velocity.y * 0.98D + 0.018D,
                    velocity.z * 0.96D
            );

            position = position.add(velocity);

            spawnTrail();

            if (age >= EXPLODE_AGE_TICKS) {
                explode();
                finished = true;
            }
        }

        private void spawnTrail() {
            spawnForcedParticles(
                    ParticleTypes.FIREWORK,
                    position.x,
                    position.y,
                    position.z,
                    4,
                    0.05D,
                    0.05D,
                    0.05D,
                    0.01D
            );

            if (age % 4 == 0) {
                spawnForcedParticles(
                        ParticleTypes.END_ROD,
                        position.x,
                        position.y,
                        position.z,
                        1,
                        0.03D,
                        0.03D,
                        0.03D,
                        0.003D
                );
            }
        }

        private void explode() {
            spawnForcedParticles(
                    ParticleTypes.FLASH,
                    position.x,
                    position.y,
                    position.z,
                    4,
                    0.35D,
                    0.35D,
                    0.35D,
                    0.0D
            );

            spawnForcedParticles(
                    ParticleTypes.FIREWORK,
                    position.x,
                    position.y,
                    position.z,
                    170,
                    1.0D,
                    1.0D,
                    1.0D,
                    0.13D
            );

            spawnForcedParticles(
                    ParticleTypes.END_ROD,
                    position.x,
                    position.y,
                    position.z,
                    60,
                    0.9D,
                    0.9D,
                    0.9D,
                    0.065D
            );

            // Long-lived full-bright light inside the memorial head.
            placeTemporaryLight(
                    world,
                    BlockPos.ofFloored(position),
                    HEAD_DURATION_TICKS + 20,
                    15
            );

            // Short full-bright pop at the main explosion.
            placeTemporaryLight(
                    world,
                    BlockPos.ofFloored(position.add(1.0D, 0.0D, 0.0D)),
                    MAIN_BURST_LIGHT_DURATION_TICKS,
                    15
            );

            placeTemporaryLight(
                    world,
                    BlockPos.ofFloored(position.add(-1.0D, 0.0D, 0.0D)),
                    MAIN_BURST_LIGHT_DURATION_TICKS,
                    15
            );

            placeTemporaryLight(
                    world,
                    BlockPos.ofFloored(position.add(0.0D, 1.0D, 1.0D)),
                    MAIN_BURST_LIGHT_DURATION_TICKS,
                    15
            );

            placeTemporaryLight(
                    world,
                    BlockPos.ofFloored(position.add(0.0D, -1.0D, -1.0D)),
                    MAIN_BURST_LIGHT_DURATION_TICKS,
                    15
            );

            ACTIVE_BURST_LIGHTING.add(new ActiveBurstLighting(world, position));

            for (ServerPlayerEntity player : world.getPlayers()) {
                if (player.squaredDistanceTo(position) > SEND_RADIUS * SEND_RADIUS) {
                    continue;
                }

                ServerPlayNetworking.send(
                        player,
                        new MemorialHeadEffectPayload(position.x, position.y, position.z, displayYaw)
                );

                placeMainGroundFlashFor(player);
                playMainExplosionSoundsFor(player);
            }
        }

        private void spawnForcedParticles(
                ParticleEffect particle,
                double x,
                double y,
                double z,
                int count,
                double deltaX,
                double deltaY,
                double deltaZ,
                double speed
        ) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (player.squaredDistanceTo(x, y, z) > SEND_RADIUS * SEND_RADIUS) {
                    continue;
                }

                world.spawnParticles(
                        player,
                        particle,
                        true,
                        x,
                        y,
                        z,
                        count,
                        deltaX,
                        deltaY,
                        deltaZ,
                        speed
                );
            }
        }

        private void placeMainGroundFlashFor(ServerPlayerEntity player) {
            BlockPos base = player.getBlockPos();

            placeTemporaryLight(world, base.up(2), MAIN_BURST_LIGHT_DURATION_TICKS, 15);
            placeTemporaryLight(world, base.add(2, 2, 0), MAIN_BURST_LIGHT_DURATION_TICKS, 12);
            placeTemporaryLight(world, base.add(-2, 2, 0), MAIN_BURST_LIGHT_DURATION_TICKS, 12);
            placeTemporaryLight(world, base.add(0, 2, 2), MAIN_BURST_LIGHT_DURATION_TICKS, 12);
            placeTemporaryLight(world, base.add(0, 2, -2), MAIN_BURST_LIGHT_DURATION_TICKS, 12);
        }

        private void playMainExplosionSoundsFor(ServerPlayerEntity player) {
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            world.playSound(null, x, y, z,
                    SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,
                    SoundCategory.PLAYERS,
                    1.2F,
                    0.75F
            );

            world.playSound(null, x, y, z,
                    SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST,
                    SoundCategory.PLAYERS,
                    0.9F,
                    0.9F
            );

            world.playSound(null, x, y, z,
                    SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    SoundCategory.PLAYERS,
                    0.9F,
                    1.15F
            );

            world.playSound(null, x, y, z,
                    SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    SoundCategory.PLAYERS,
                    0.7F,
                    1.45F
            );

            world.playSound(null, x, y, z,
                    SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT,
                    SoundCategory.PLAYERS,
                    0.45F,
                    1.75F
            );
        }
    }
}