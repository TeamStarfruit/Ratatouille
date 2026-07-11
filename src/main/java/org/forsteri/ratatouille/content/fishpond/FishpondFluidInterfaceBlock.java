package org.forsteri.ratatouille.content.fishpond;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.forsteri.ratatouille.entry.CRBlockEntityTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FishpondFluidInterfaceBlock extends Block implements IBE<FishpondFluidInterfaceBlockEntity> {
    public FishpondFluidInterfaceBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<FishpondFluidInterfaceBlockEntity> getBlockEntityClass() {
        return FishpondFluidInterfaceBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FishpondFluidInterfaceBlockEntity> getBlockEntityType() {
        return CRBlockEntityTypes.FISHPOND_FLUID_INTERFACE_ENTITY.get();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new FishpondFluidInterfaceBlockEntity(CRBlockEntityTypes.FISHPOND_FLUID_INTERFACE_ENTITY.get(), pPos, pState);
    }
}
