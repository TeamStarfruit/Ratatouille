package org.forsteri.ratatouille.content.aerator;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.forsteri.ratatouille.entry.CRBlockEntityTypes;

public class AeratorBlock extends KineticBlock implements IBE<AeratorBlockEntity>, ICogWheel {
    public AeratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.CASING_12PX.get(Direction.UP);
    }


    @Override
    public Direction.Axis getRotationAxis(BlockState blockState) {
        return Direction.Axis.Y;
    }

    @Override
    public Class<AeratorBlockEntity> getBlockEntityClass() {
        return AeratorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AeratorBlockEntity> getBlockEntityType() {
        return CRBlockEntityTypes.AERATOR_BLOCK_ENTITY.get();
    }
}
