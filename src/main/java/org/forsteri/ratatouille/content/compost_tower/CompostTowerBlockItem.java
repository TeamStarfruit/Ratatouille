package org.forsteri.ratatouille.content.compost_tower;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.forsteri.ratatouille.entry.CRBlockEntityTypes;
import org.jetbrains.annotations.NotNull;

public class CompostTowerBlockItem extends BlockItem {
    public CompostTowerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult place(@NotNull BlockPlaceContext ctx) {
        InteractionResult initialResult = super.place(ctx);
        if (!initialResult.consumesAction())
            return initialResult;
        tryMultiPlace(ctx);
        return initialResult;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(@NotNull BlockPos pos, Level level, Player player,
                                                 @NotNull ItemStack stack, @NotNull BlockState state) {
        MinecraftServer server = level.getServer();
        if (server == null)
            return false;

        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            CompoundTag nbt = data.copyTag();
            nbt.remove("Size");
            nbt.remove("Height");
            nbt.remove("Controller");
            nbt.remove("LastKnownPos");
            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(nbt));
        }

        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    private void tryMultiPlace(BlockPlaceContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null)
            return;
        if (player.isShiftKeyDown())
            return;

        Direction face = ctx.getClickedFace();
        if (!face.getAxis().isVertical())
            return;

        ItemStack stack = ctx.getItemInHand();
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockPos placedOnPos = pos.relative(face.getOpposite());
        BlockState placedOnState = world.getBlockState(placedOnPos);

        if (!(placedOnState.getBlock() instanceof CompostTowerBlock))
            return;

        CompostTowerBlockEntity towerAt = ConnectivityHandler.partAt(
                CRBlockEntityTypes.COMPOST_TOWER_BLOCK_ENTITY.get(), world, placedOnPos
        );
        if (towerAt == null)
            return;

        CompostTowerBlockEntity controllerBE = towerAt.getControllerBE();
        if (controllerBE == null)
            return;

        int width = controllerBE.getWidth();
        if (width == 1)
            return;

        int tanksToPlace = 0;
        BlockPos startPos = face == Direction.DOWN
                ? controllerBE.getBlockPos().below()
                : controllerBE.getBlockPos().above(controllerBE.getHeight());

        if (startPos.getY() != pos.getY())
            return;

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockPos offsetPos = startPos.offset(x, 0, z);
                BlockState blockState = world.getBlockState(offsetPos);
                if (blockState.getBlock() instanceof CompostTowerBlock)
                    continue;
                if (!blockState.canBeReplaced())
                    return;
                tanksToPlace++;
            }
        }

        if (!player.isCreative() && stack.getCount() < tanksToPlace)
            return;

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockPos offsetPos = startPos.offset(x, 0, z);
                BlockState blockState = world.getBlockState(offsetPos);
                if (blockState.getBlock() instanceof CompostTowerBlock)
                    continue;

                BlockPlaceContext context = BlockPlaceContext.at(ctx, offsetPos, face);
                player.getPersistentData().putBoolean("SilenceTankSound", true);
                super.place(context);
                player.getPersistentData().remove("SilenceTankSound");
            }
        }
    }
}
