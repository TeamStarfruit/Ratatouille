package org.forsteri.ratatouille.entry;

import com.simibubi.create.AllShapes;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CRShapes {
    public static final VoxelShaper FISHING_NET = shape(0, 5, 0, 16, 9, 16).forDirectional(),
            AERATOR = shape(0, 2, 0, 16, 12, 16).forDirectional();

    public static final VoxelShape[] SLUDGE = new VoxelShape[]{
            Block.box(0D, 0D, 0D, 16.0D, 0.5D, 16.0D),
            Block.box(0D, 0D, 0D, 16.0D, 1.0D, 16.0D),
            Block.box(0D, 0D, 0D, 16.0D, 1.5D, 16.0D),
            Block.box(0D, 0D, 0D, 16.0D, 2.0D, 16.0D)
    };

    private static AllShapes.Builder shape(VoxelShape shape) {
        return new AllShapes.Builder(shape);
    }
    private static AllShapes.Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }
    private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }
}
