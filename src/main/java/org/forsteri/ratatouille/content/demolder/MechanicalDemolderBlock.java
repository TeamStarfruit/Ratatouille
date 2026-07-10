package org.forsteri.ratatouille.content.demolder;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.forsteri.ratatouille.entry.CRBlockEntityTypes;
import org.jetbrains.annotations.NotNull;

public class MechanicalDemolderBlock extends HorizontalKineticBlock implements IBE<MechanicalDemolderBlockEntity> {

    public MechanicalDemolderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.CASING_12PX.get(Direction.DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction prefferedSide = getPreferredHorizontalFacing(context);
        if (prefferedSide != null)
            return defaultBlockState().setValue(HORIZONTAL_FACING, prefferedSide);
        return super.getStateForPlacement(context);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING)
                .getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(HORIZONTAL_FACING)
                .getAxis();
    }

    @Override
    public Class<MechanicalDemolderBlockEntity> getBlockEntityClass() {
        return MechanicalDemolderBlockEntity.class;
    }


    @Override
    public BlockEntityType<? extends MechanicalDemolderBlockEntity> getBlockEntityType() {
        return CRBlockEntityTypes.MECHANICAL_DEMOLDER_ENTITY.get();
    }

//    @Override
//    public SpeedLevel getMinimumRequiredSpeedLevel() {
//        return SpeedLevel.MEDIUM;
//    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public @NotNull InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide)
            return InteractionResult.SUCCESS;

        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);

        if (!(blockEntity instanceof MechanicalDemolderBlockEntity be))
            return InteractionResult.PASS;

        ItemStack mold = be.outputInv.extractItem(0, 64, false);

        if (mold.isEmpty())
            return InteractionResult.PASS;

        pPlayer.getInventory().placeItemBackInInventory(mold);

        be.notifyUpdate();

        return InteractionResult.SUCCESS;
    }
}
