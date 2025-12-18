package org.forsteri.ratatouille.content.compost_tower;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.forsteri.ratatouille.entry.CRBlockEntityTypes;
import org.jetbrains.annotations.NotNull;

public class CompostTowerBlock extends Block implements IWrenchable, IBE<CompostTowerBlockEntity> {
    public static final BooleanProperty IS_2x2 = BooleanProperty.create("is_2x2");
    public CompostTowerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(IS_2x2, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(IS_2x2);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onPlace(BlockState state, @NotNull Level world, @NotNull BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock())
            return;
        if (moved)
            return;

        withBlockEntityDo(world, pos, CompostTowerBlockEntity::updateConnectivity);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof CompostTowerBlockEntity towerBE))
                return;
            ItemHelper.dropContents(world, pos, towerBE.inputInventory);
            ItemHelper.dropContents(world, pos, towerBE.outputInventory);
            world.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(towerBE);
        }
    }

//    @Override
//    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
//        return Shapes.block();
//    }
//
//    @Override
//    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
//        ItemStack heldItem = player.getItemInHand(hand);
//        return this.onBlockEntityUse(level, pos, be -> {
//            if (!heldItem.isEmpty()) {
//                if (FluidHelper.tryEmptyItemIntoBE(level, player, hand, heldItem, be)) {
//                    return InteractionResult.SUCCESS;
//                } else if (FluidHelper.tryFillItemFromBE(level, player, hand, heldItem, be)) {
//                    return InteractionResult.SUCCESS;
//                } else if (!GenericItemEmptying.canItemBeEmptied(level, heldItem) && !GenericItemFilling.canItemBeFilled(level, heldItem)) {
//                    return heldItem.getItem().equals(Items.SPONGE) && !be.getCapability(ForgeCapabilities.FLUID_HANDLER)
//                            .map(handler -> handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE))
//                            .orElse(FluidStack.EMPTY).isEmpty() ? InteractionResult.SUCCESS : InteractionResult.PASS;
//                } else {
//                    return InteractionResult.SUCCESS;
//                }
//            } else {
//                return InteractionResult.FAIL;
//            }
//        });
//    }


    @Override
    public Class<CompostTowerBlockEntity> getBlockEntityClass() {
        return CompostTowerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CompostTowerBlockEntity> getBlockEntityType() {
        return CRBlockEntityTypes.COMPOST_TOWER_BLOCK_ENTITY.get();
    }

    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        withBlockEntityDo(level, pos, CompostTowerBlockEntity::updateCompostTowerState);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState pState, @NotNull Direction pDirection, @NotNull BlockState pNeighborState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pPos, @NotNull BlockPos pNeighborPos) {
        if (pNeighborState.getBlock() != this)
            withBlockEntityDo(pLevel, pPos, CompostTowerBlockEntity::updateCompostData);
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }
}