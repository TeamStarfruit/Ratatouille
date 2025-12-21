package org.forsteri.ratatouille.content.irrigation_tower;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.forsteri.ratatouille.entry.CRFluids;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;


public class IrrigationTowerBlockEntity extends FluidTankBlockEntity {

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking,
                getCapability(ForgeCapabilities.FLUID_HANDLER));
    }

    public IrrigationTowerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void applyFluidTankSize(int blocks) {
        tankInventory.setCapacity(blocks * 1000);
        int overflow = tankInventory.getFluidAmount() - tankInventory.getCapacity();
        if (overflow > 0)
            tankInventory.drain(overflow, IFluidHandler.FluidAction.EXECUTE);
        forceFluidLevelUpdate = true;
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if (isController()) {
            tankInventory.setCapacity(getTotalTankSize() * 1000);
            tankInventory.readFromNBT(compound.getCompound("TankContent"));
            if (tankInventory.getSpace() < 0)
                tankInventory.drain(-tankInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    public int getTankSize(int tank) {
        return 1000;
    }

    @Override
    protected SmartFluidTank createInventory() {
        return new IrrigationSmartFluidTank(1000, this::onFluidStackChanged);
    }

    public static void isNearWater(LevelReader pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        for (BlockPos blockpos : BlockPos.betweenClosed(pPos.offset(-8, -1, -8), pPos.offset(8, 1, 8))) {
            if (pLevel.getBlockEntity(blockpos.above()) instanceof IrrigationTowerBlockEntity be) {
                FluidStack fluid = be.getTankInventory().getFluid();
                if (!fluid.isEmpty() && (fluid.getFluid().isSame(Fluids.WATER) || fluid.getFluid().isSame(CRFluids.COMPOST_TEA.get()))) {
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }
            }
        }
    }

    public static class IrrigationSmartFluidTank extends SmartFluidTank {

        public IrrigationSmartFluidTank(int capacity, Consumer<FluidStack> updateCallback) {
            super(capacity, updateCallback);
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(Fluids.WATER)
                    || stack.getFluid().isSame(CRFluids.COMPOST_TEA.get().getSource());
        }
    }

}
