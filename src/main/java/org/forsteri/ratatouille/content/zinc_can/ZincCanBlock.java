package org.forsteri.ratatouille.content.zinc_can;

import com.mojang.serialization.MapCodec;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.forsteri.ratatouille.content.frozen_block.FrozenBlock;
import org.forsteri.ratatouille.content.frozen_block.FrozenBlockEntity;
import org.forsteri.ratatouille.entry.CRBlockEntityTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ZincCanBlock extends BaseEntityBlock{
    public static final MapCodec<ZincCanBlock> CODEC = simpleCodec(ZincCanBlock::new);

    public ZincCanBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    /*@Override
    public void randomTick(@NotNull BlockState pState, @NotNull ServerLevel pLevel, BlockPos pPos, @NotNull RandomSource pRandom) {
        for (BlockPos blockpos : BlockPos.betweenClosed(pPos.offset(-4, -4, -4), pPos.offset(4, 4, 4))) {
            if (pLevel.getBlockState(blockpos).is(Blocks.TORCH)) {
                return;
            }
        }



    }
*/
    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new ZincCanBlockEntity(CRBlockEntityTypes.ZINC_CAN_BLOCK_ENTITY.get(), pPos, pState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, CRBlockEntityTypes.ZINC_CAN_BLOCK_ENTITY.get(),ZincCanBlockEntity::tick);
    }

}
