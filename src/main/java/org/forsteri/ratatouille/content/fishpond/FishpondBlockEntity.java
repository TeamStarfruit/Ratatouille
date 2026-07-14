package org.forsteri.ratatouille.content.fishpond;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.forsteri.ratatouille.content.oven.OvenBlockEntity;

import java.util.List;

public class FishpondBlockEntity extends SmartBlockEntity {
    private boolean updateConnectivity = true;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected FishpondData fishpondData;
    protected int height = 1;
    protected int radius = 1;

    public FishpondBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        fishpondData = new FishpondData();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void tick() {
        super.tick();
        if (isController())
            fishpondData.tick(this);

        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition)) {
            removeController();
            lastKnownPos = worldPosition;
            return;
        }

        if (updateConnectivity)
            updateConnectivity();
    }

    public void updateConnectivity() {
        if (level == null) return;

        updateConnectivity = false;
        if (level.isClientSide)
            return;
        FishpondConnectivityHandler.formMulti(this);
    }

    public void updateFishpondState() {
        if (!isController() && getControllerBE() != null) {
            getControllerBE().updateFishpondState();
            return;
        }

        if (fishpondData.evaluate(this)) {
            notifyUpdate();
        }
    }

    public boolean isController() {
        return controller == null || worldPosition.equals(controller);
    }

    public FishpondBlockEntity getControllerBE() {
        assert level != null;

        if (isController())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof FishpondBlockEntity be)
            return be;
        return null;
    }

    public void updateFishpondData() {
        FishpondBlockEntity be = getControllerBE();
        if (be == null)
            return;
        be.fishpondData.updateRequired = 2;
    }

    public void removeController() {
        if (level == null || level.isClientSide())
            return;
        updateConnectivity = true;
        controller = null;
        radius = 1;
        height = 1;

        fishpondData.clear();
        setChanged();
        sendData();
    }
}
