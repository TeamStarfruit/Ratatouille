package org.forsteri.ratatouille.content.compost_tower;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IS_2x2);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPlace(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock())
            return;
        if (moved)
            return;

        withBlockEntityDo(level, pos, CompostTowerBlockEntity::updateConnectivity);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof CompostTowerBlockEntity towerBE))
                return;

            ItemHelper.dropContents(level, pos, towerBE.inputInventory);
            ItemHelper.dropContents(level, pos, towerBE.outputInventory);

            level.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(towerBE);
        }
    }

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
    public @NotNull BlockState updateShape(@NotNull BlockState state,
                                           @NotNull Direction direction,
                                           @NotNull BlockState neighborState,
                                           @NotNull LevelAccessor level,
                                           @NotNull BlockPos pos,
                                           @NotNull BlockPos neighborPos) {
        if (neighborState.getBlock() != this)
            withBlockEntityDo(level, pos, CompostTowerBlockEntity::updateCompostData);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}
