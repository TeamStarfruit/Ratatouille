package org.forsteri.ratatouille.content.compost_tower;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import com.sun.jdi.connect.spi.TransportService;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.forsteri.ratatouille.entry.CRBlockEntityTypes;
import org.forsteri.ratatouille.entry.CRRecipeTypes;
import org.forsteri.ratatouille.util.Lang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.simibubi.create.content.fluids.tank.FluidTankBlockEntity.getCapacityMultiplier;

public class CompostTowerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IMultiBlockEntityContainer.Fluid {
    public LazyOptional<CompostTowerInventoryHandler> itemCapability = LazyOptional.empty();
    public LazyOptional<CompostTowerFluidHandler> fluidCapability = LazyOptional.empty();
    private boolean updateConnectivity = true;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected CompostData compostData;
    public CompostFluidTank tankInventory = new CompostFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
    public ItemStackHandler inputInventory = new ItemStackHandler(3);
    public ItemStackHandler outputInventory = new ItemStackHandler(3);
    public HashMap<net.minecraft.world.level.material.Fluid, LerpedFloat> fluidLevels = new HashMap<>();
    public HashMap<net.minecraft.world.level.material.Fluid, LerpedFloat> gasLevels = new HashMap<>();

    private boolean canProcess(ItemStack stack) {
        ItemStackHandler tester = new ItemStackHandler(1);
        tester.setStackInSlot(0, stack);
        RecipeWrapper inventoryIn = new RecipeWrapper(tester);

        if (level == null) return false;
        if (compostData.lastRecipe != null && compostData.lastRecipe.matches(inventoryIn, level))
            return true;
        return CRRecipeTypes.COMPOSTING.find(inventoryIn, level)
                .isPresent();
    }


    public class CompostTowerInventoryHandler extends CombinedInvWrapper {
        public CompostTowerInventoryHandler() {
            super(CompostTowerBlockEntity.this.inputInventory, CompostTowerBlockEntity.this.outputInventory);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (CompostTowerBlockEntity.this.outputInventory == this.getHandlerFromIndex(this.getIndexForSlot(slot)))
                return false;
            return CompostTowerBlockEntity.this.canProcess(stack) && super.isItemValid(slot, stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (CompostTowerBlockEntity.this.outputInventory == this.getHandlerFromIndex(this.getIndexForSlot(slot)))
                return stack;
            if (!this.isItemValid(slot, stack))
                return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (CompostTowerBlockEntity.this.inputInventory == this.getHandlerFromIndex(getIndexForSlot(slot)))
                return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }
    }

    public class CompostTowerFluidHandler extends CompostFluidTank {
        public CompostTowerFluidHandler(CompostFluidTank tank) {
            super(tank.fluidIds, tank.tanks, tank.updateCallback, tank.index, tank.capacity);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            var outputHeight = CompostTowerBlockEntity.this.getOutputHeight();
            var towerHeight = CompostTowerBlockEntity.this.getTowerHeight();
            var availFluid = getFluidAtBlockHeight(outputHeight, towerHeight);

            if (!resource.getFluid().isSame(availFluid)) return FluidStack.EMPTY;
            return super.drain(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            var outputHeight = CompostTowerBlockEntity.this.getOutputHeight();
            var towerHeight = CompostTowerBlockEntity.this.getTowerHeight();
            var availFluid = getFluidAtBlockHeight(outputHeight, towerHeight);

            return super.drain(new FluidStack(availFluid, maxDrain), action);
        }
    }

    private int getTowerHeight() {
        var be = getControllerBE();
        if (be == null) return 1;
        return be.height;
    }


    private int getOutputHeight() {
        var be = getControllerBE();
        if (be == null) return 0;
        return getBlockPos().getY() - be.getBlockPos().getY();
    }

    public CompostTowerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        compostData = new CompostData();
    }

    @Override
    public int getTankSize(int tank) {
        return getCapacityMultiplier();
    }

    @Override
    public void setTankSize(int tank, int blocks) {
        applyFluidTankSize(1);
    }

    public void applyFluidTankSize(int blocks) {
        tankInventory.setCapacity(blocks * getCapacityMultiplier());
        tankInventory.clearOverflow();
    }

    @Override
    public FluidStack getFluid(int tank) {
        return tankInventory.getFluidInTank(tank);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        if (isController())
            return super.createRenderBoundingBox().expandTowards(radius - 1, height - 1, radius - 1);
        else
            return super.createRenderBoundingBox();
    }

    @Override
    public void initialize() {
        super.initialize();
        notifyUpdate();
        if (level != null && level.isClientSide)
            invalidateRenderBoundingBox();
    }

    @Override
    public void tick() {
        super.tick();

        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition)) {
            removeController(true);
            lastKnownPos = worldPosition;
            return;
        }

        if (updateConnectivity)
            updateConnectivity();
        if (isController())
            compostData.tick(this);
        fluidLevels.values().forEach(LerpedFloat::tickChaser);
        gasLevels.values().forEach(LerpedFloat::tickChaser);
    }

    private void refreshCapability() {
        itemCapability.invalidate();
        fluidCapability.invalidate();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompostTowerBlockEntity getControllerBE() {
        assert level != null;

        if (isController())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof CompostTowerBlockEntity CompostTowerBlockEntity)
            return CompostTowerBlockEntity;
        return null;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.getX() == controller.getX()
                && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
    }

    @Override
    public void setController(BlockPos pos) {
        assert level != null;

        if (level.isClientSide && !isVirtual())
            return;
        if (pos.equals(this.controller))
            return;
        this.controller = pos;
        refreshCapability();
        notifyUpdate();
    }

    @Override
    public void removeController(boolean keepContents) {
        assert level != null;
        if (level.isClientSide())
            return;
        updateConnectivity = true;
        if (!keepContents)
            applyFluidTankSize(1);

        controller = null;
        radius = 1;
        height = 1;

        refreshCapability();
        compostData.clear();
        tankInventory.onContentsChanged();
        notifyUpdate();
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    public void updateConnectivity() {
        assert level != null;

        updateConnectivity = false;
        if (level.isClientSide)
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    @Override
    public void notifyMultiUpdated() {
        assert level != null;

        level.setBlock(getBlockPos(), getBlockState().setValue(CompostTowerBlock.IS_2x2, getWidth() == 2), 6);

        refreshCapability();
        tankInventory.onContentsChanged();
        updateCompostTowerState();
        if (isController()) {
            for (int xOffset = 0; xOffset < radius; xOffset++) {
                for (int yOffset = 0; yOffset < height; yOffset++) {
                    for (int zOffset = 0; zOffset < radius; zOffset++) {
                        BlockPos vaultPos = worldPosition.offset(xOffset, yOffset, zOffset);
                        CompostTowerBlockEntity partAt =
                                ConnectivityHandler.partAt(CRBlockEntityTypes.COMPOST_TOWER_BLOCK_ENTITY.get(), level, vaultPos);
                        if (partAt == null || this == partAt) continue;
                        tankInventory.fillFrom(partAt.tankInventory);
                        partAt.tankInventory.clear();
                    }
                }
            }
        }

        notifyUpdate();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis == Direction.Axis.Y)
            return 7;
        return getMaxWidth();
    }

    @Override
    public int getMaxWidth() {
        return 3;
    }

    protected int height = 1;

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    protected int radius = 1;

    @Override
    public int getWidth() {
        return radius;
    }

    @Override
    public void setWidth(int width) {
        radius = width;
    }

    private boolean outputFluidTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CompostTowerBlockEntity controllerBE = getControllerBE();
        if (controllerBE == null)
            return false;

        Lang.translate("gui.goggles.current_layer_fluid_output")
                .forGoggles(tooltip);

        var outputHeight = getOutputHeight();
        var towerHeight = getTowerHeight();
        var availFluid = controllerBE.tankInventory.getFluidAtBlockHeight(outputHeight, towerHeight);

        FluidStack fluidStack = new FluidStack(availFluid, 1);
        CreateLang.fluidName(fluidStack)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        return true;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CompostTowerBlockEntity controllerBE = getControllerBE();
        if (controllerBE == null || level == null)
            return false;

        if (!outputFluidTooltip(tooltip, isPlayerSneaking))
            return false;

        return controllerBE.compostData.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    public void updateCompostTowerState() {
        if (!isController() && getControllerBE() != null) {
            getControllerBE().updateCompostTowerState();
            return;
        }

        if (compostData.evaluate(this)) {
            notifyUpdate();
        }
    }

    public void updateCompostData() {
        CompostTowerBlockEntity be = getControllerBE();
        if (be == null)
            return;
        be.compostData.updateRequired = 2;
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        assert level != null;
        super.read(compound, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = radius;
        int prevHeight = height;

        updateConnectivity = compound.contains("Uninitialized");
        controller = null;
        lastKnownPos = null;

        if (compound.contains("LastKnownPos"))
            lastKnownPos = NbtUtils.readBlockPos(compound.getCompound("LastKnownPos"));
        if (compound.contains("Controller"))
            controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));

        if (isController()) {
            radius = compound.getInt("Size");
            height = compound.getInt("Height");
            tankInventory.deserializeNBT(compound.getCompound("TankContent"));
            inputInventory.deserializeNBT(compound.getCompound("InputInv"));
            outputInventory.deserializeNBT(compound.getCompound("OutputInv"));
            applyFluidTankSize(getTotalTankSize());

            for (var fluid : tankInventory.getSortedFluids()) {
                var targetLevel = fluid.getFluidType().isLighterThanAir() ? gasLevels : fluidLevels;
                var fluidLevel = targetLevel.get(fluid);
                if (fluidLevel == null) {
                    fluidLevel = LerpedFloat.linear()
                            .startWithValue(tankInventory.getFilledPercentage(fluid));
                    targetLevel.put(fluid, fluidLevel);
                }
                fluidLevel.chase(tankInventory.getFilledPercentage(fluid), 0.5f, LerpedFloat.Chaser.EXP);
            }
        }

        compostData.read(compound, clientPacket);
        if (!clientPacket) {
            return;
        }

        boolean changeOfController =
                !Objects.equals(controllerBefore, controller);
        if (hasLevel() && (changeOfController || prevSize != radius || prevHeight != height)) {
            level.setBlocksDirty(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState());
            if (hasLevel())
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            if (isController())
                applyFluidTankSize(getTotalTankSize());

            invalidateRenderBoundingBox();
        }
    }

    public int getTotalTankSize() {
        return radius * radius * height;
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        if (updateConnectivity)
            compound.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            compound.putInt("Size", radius);
            compound.putInt("Height", height);
            applyFluidTankSize(getTotalTankSize());
            compound.put("TankContent", tankInventory.serializeNBT());
            compound.put("InputInv", inputInventory.serializeNBT());
            compound.put("OutputInv", outputInventory.serializeNBT());
        }

        super.write(compound, clientPacket);
        compostData.write(compound, clientPacket);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (isItemHandlerCap(cap)) {
            initCapability();
            return itemCapability.cast();
        }
        if (isFluidHandlerCap(cap)) {
            initCapability();
            return fluidCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    private void initCapability() {
        if (level == null) return;

        var controller = getControllerBE();
        if (itemCapability.isPresent() && fluidCapability.isPresent() || controller == null)
            return;

        if (isController()) {
            itemCapability = LazyOptional.of(CompostTowerInventoryHandler::new);
            fluidCapability = LazyOptional.of(() -> new CompostTowerFluidHandler(tankInventory));
        } else {
            controller.initCapability();

            itemCapability = controller.itemCapability;
            fluidCapability = LazyOptional.of(() -> new CompostTowerFluidHandler(controller.tankInventory));
        }
    }

    protected void onFluidStackChanged(HashMap<net.minecraft.world.level.material.Fluid, Integer> tanks) {
        if (level == null)
            return;
        if (!level.isClientSide) {
            notifyUpdate();
        }
    }
}
