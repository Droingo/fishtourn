package net.droingo.fishtourn.fish;

import net.droingo.fishtourn.block.ModBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FishingZoneDetector {
    private static final int HORIZONTAL_RADIUS = 1;
    private static final int DOWN_SCAN_DISTANCE = 24;
    private static final int UP_SCAN_DISTANCE = 3;

    private FishingZoneDetector() {
    }

    public static CastZone detect(World world, BlockPos bobberPos) {
        for (int x = -HORIZONTAL_RADIUS; x <= HORIZONTAL_RADIUS; x++) {
            for (int z = -HORIZONTAL_RADIUS; z <= HORIZONTAL_RADIUS; z++) {
                for (int y = -DOWN_SCAN_DISTANCE; y <= UP_SCAN_DISTANCE; y++) {
                    BlockPos checkPos = bobberPos.add(x, y, z);

                    if (world.getBlockState(checkPos).isOf(ModBlocks.DEEP_FISHING_ZONE)) {
                        return CastZone.DEEP;
                    }
                }
            }
        }

        return CastZone.NONE;
    }
}