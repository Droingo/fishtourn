package net.droingo.fishtourn.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.droingo.fishtourn.entity.ModEntities;
import net.droingo.fishtourn.entity.WakeSplashEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;

public class FishingZoneBlock extends Block implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public FishingZoneBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        FluidState fluidState = context.getWorld().getFluidState(context.getBlockPos());
        boolean waterlogged = fluidState.getFluid() == Fluids.WATER;

        return this.getDefaultState().with(WATERLOGGED, waterlogged);
    }

    @Override
    protected void onBlockAdded(
            BlockState state,
            net.minecraft.world.World world,
            BlockPos pos,
            BlockState oldState,
            boolean notify
    ) {
        super.onBlockAdded(state, world, pos, oldState, notify);

        if (!world.isClient()) {
            world.scheduleBlockTick(pos, this, 20);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        spawnZoneParticles(world, pos);
        world.scheduleBlockTick(pos, this, 20);
    }

    private static void spawnZoneParticles(ServerWorld world, BlockPos pos) {
        BlockPos surfacePos = findWaterSurface(world, pos);

        double x = surfacePos.getX() + 0.5;
        double y = surfacePos.getY() + 1.15;
        double z = surfacePos.getZ() + 0.5;

        world.spawnParticles(
                ParticleTypes.SPLASH,
                x,
                y,
                z,
                10,
                0.45,
                0.05,
                0.45,
                0.015
        );

        world.spawnParticles(
                ParticleTypes.BUBBLE_POP,
                x,
                y + 0.05,
                z,
                8,
                0.35,
                0.04,
                0.35,
                0.01
        );

        world.spawnParticles(
                ParticleTypes.DOLPHIN,
                x,
                y - 0.15,
                z,
                5,
                0.40,
                0.04,
                0.40,
                0.02
        );
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);

        if (random.nextInt(5) != 0) {
            return;
        }

        BlockPos spawnPos = pos.up();

        if (!world.getBlockState(spawnPos).isAir()) {
            return;
        }

        WakeSplashEntity entity = new WakeSplashEntity(ModEntities.WAKE_SPLASH, world);

        double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
        double y = pos.getY() + 1.35D + random.nextDouble() * 0.45D;
        double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;

        entity.refreshPositionAndAngles(x, y, z, 0.0F, 0.0F);
        entity.setVelocity(0.0D, -0.16D, 0.0D);

        world.spawnEntity(entity);
    }

    private static BlockPos findWaterSurface(ServerWorld world, BlockPos markerPos) {
        BlockPos.Mutable mutable = markerPos.mutableCopy();

        int maxY = world.getTopY();

        while (mutable.getY() < maxY - 1) {
            BlockPos above = mutable.up();

            boolean currentIsWater = world.getFluidState(mutable).isOf(Fluids.WATER);
            boolean aboveIsWater = world.getFluidState(above).isOf(Fluids.WATER);

            if (currentIsWater && !aboveIsWater) {
                return mutable.toImmutable();
            }

            mutable.move(Direction.UP);
        }

        return markerPos;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED)) {
            return Fluids.WATER.getStill(false);
        }

        return super.getFluidState(state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            WorldAccess world,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }
}