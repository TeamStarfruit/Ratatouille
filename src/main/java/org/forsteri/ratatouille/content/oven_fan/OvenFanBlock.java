package org.forsteri.ratatouille.content.oven_fan;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.levelWrappers.WrappedLevel;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.forsteri.ratatouille.entry.CRBlockEntityTypes;
import org.forsteri.ratatouille.entry.CRBlocks;

import java.util.List;
import java.util.function.Predicate;

public class OvenFanBlock extends HorizontalKineticBlock implements ICogWheel, IWrenchable, IBE<OvenFanBlockEntity> {
    public OvenFanBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pPlayer.isShiftKeyDown())
            return InteractionResult.PASS;

        ItemStack heldItem = pPlayer.getItemInHand(pHand);
        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(heldItem))
            return placementHelper.getOffset(pPlayer, pLevel, pState, pPos, pHit)
                    .placeInWorld(pLevel, (BlockItem) heldItem.getItem(), pPlayer, pHand, pHit);

        return InteractionResult.SUCCESS;
    }

    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, worldIn, pos, oldState, isMoving);
        this.blockUpdate(state, worldIn, pos);
    }

    public void updateIndirectNeighbourShapes(BlockState stateIn, LevelAccessor worldIn, BlockPos pos, int flags, int count) {
        super.updateIndirectNeighbourShapes(stateIn, worldIn, pos, flags, count);
        this.blockUpdate(stateIn, worldIn, pos);
    }

    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        this.blockUpdate(state, worldIn, pos);
    }

    protected void blockUpdate(BlockState state, LevelAccessor worldIn, BlockPos pos) {
        if (!(worldIn instanceof WrappedLevel)) {
            this.notifyOvenFanBlockEntity(worldIn, pos);
        }
    }

    protected void notifyOvenFanBlockEntity(LevelAccessor world, BlockPos pos) {
        this.withBlockEntityDo(world, pos, OvenFanBlockEntity::blockInFrontChanged);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferredFacing = this.getPreferredHorizontalFacing(context);
        if (preferredFacing == null) {
            preferredFacing = context.getHorizontalDirection();
        }

        return (BlockState)this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getPlayer() != null && context.getPlayer().isShiftKeyDown() ? preferredFacing : preferredFacing.getOpposite());
    }

    public BlockState updateAfterWrenched(BlockState newState, UseOnContext context) {
        this.blockUpdate(newState, context.getLevel(), context.getClickedPos());
        return newState;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState blockState) {
        return ((Direction)blockState.getValue(HORIZONTAL_FACING)).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    public Class<OvenFanBlockEntity> getBlockEntityClass() {
        return OvenFanBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends OvenFanBlockEntity> getBlockEntityType() {
        return CRBlockEntityTypes.OVEN_FAN_ENTITY.get();
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        Direction direction = pState.getValue(HORIZONTAL_FACING);
        return switch (direction) {
            case WEST -> Shapes.create(0, 0, 0, 13/16F, 1, 1);
            case EAST -> Shapes.create(3/16F, 0, 0, 1, 1, 1);
            case NORTH -> Shapes.create(0, 0, 0, 1, 1, 13/16F);
            case SOUTH -> Shapes.create(0, 0, 3/16F, 1, 1, 1);
            case UP, DOWN -> Shapes.block();
        };
    }

    private static final int placementHelperId = PlacementHelpers.register(new OvenFanBlock.PlacementHelper());

    @MethodsReturnNonnullByDefault
    private static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return CRBlocks.OVEN_FAN::isIn;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return CRBlocks.OVEN_FAN::has;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
                                         BlockHitResult ray) {
            List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(),
                    state.getValue(OvenFanBlock.HORIZONTAL_FACING)
                            .getAxis(),
                    dir -> world.getBlockState(pos.relative(dir)).canBeReplaced());

            return directions.isEmpty() ? PlacementOffset.fail()
                    : PlacementOffset.success(pos.relative(directions.get(0)), s ->
                            s.setValue(HORIZONTAL_FACING, state.getValue(OvenFanBlock.HORIZONTAL_FACING)));
        }
    }

}
