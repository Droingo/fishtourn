package net.droingo.fishtourn.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TrophyBlock extends Block {
    public static final MapCodec<TrophyBlock> CODEC = createCodec(TrophyBlock::new);
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    private static final VoxelShape BASE = Block.createCuboidShape(
            3.0D, 0.0D, 3.0D,
            13.0D, 2.0D, 13.0D
    );

    private static final VoxelShape STEM = Block.createCuboidShape(
            6.0D, 2.0D, 6.0D,
            10.0D, 7.0D, 10.0D
    );

    private static final VoxelShape CUP = Block.createCuboidShape(
            3.5D, 7.0D, 3.5D,
            12.5D, 13.0D, 12.5D
    );

    private static final VoxelShape SHAPE = VoxelShapes.union(BASE, STEM, CUP);

    public TrophyBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.SOUTH));
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(8) != 0) {
            return;
        }

        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = 0.55D + random.nextDouble() * 0.25D;

        double x = pos.getX() + 0.5D + Math.cos(angle) * radius;
        double y = pos.getY() + 0.75D + random.nextDouble() * 0.65D;
        double z = pos.getZ() + 0.5D + Math.sin(angle) * radius;

        world.addParticle(
                ParticleTypes.END_ROD,
                x,
                y,
                z,
                0.0D,
                0.018D,
                0.0D
        );
    }
}