package org.forsteri.ratatouille.content.zinc_can;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ZincCanBlockEntity extends BlockEntity {
    public static int MAX_CHILLNESS = 50;

    public ZincCanBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }


    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, ZincCanBlockEntity pBlockEntity) {
        if (pLevel == null || pLevel.isClientSide) return;

        double x = pPos.getX();
        double y = pPos.getY() + 1;
        double z = pPos.getZ();
        AABB searchBox = new AABB(x, y, z, x + 1, y + 0.25, z + 1);
        List<ItemEntity> itemEntities = pLevel.getEntitiesOfClass(ItemEntity.class, searchBox);



    }
}
