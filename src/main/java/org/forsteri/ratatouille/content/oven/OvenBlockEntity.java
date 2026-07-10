package org.forsteri.ratatouille.content.oven;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.forsteri.ratatouille.entry.CRBlockEntityTypes;
import org.forsteri.ratatouille.entry.CRRecipeTypes;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OvenBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IMultiBlockEntityContainer {
    public CombinedInvWrapper itemCapability;
    public List<List<List<Inventory>>> inventories = null;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected BakeData bakeData;
    protected Inventory inventory = new Inventory();
    protected int height = 1;
    protected int radius = 1;
    private boolean updateConnectivity = false;
    private FilteringBehaviour filtering;

    public OvenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        bakeData = new BakeData();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CRBlockEntityTypes.OVEN_ENTITY.get(),
                (be, context) -> {
                    be.initCapability();
                    if (be.itemCapability == null)
                        return null;
                    return be.itemCapability;
                }
        );
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        filtering = new FilteringBehaviour(this, new OvenValueBox()) {
            @Override
            public boolean isActive() {
                return isController();
            }
        }.withCallback($ -> updateBakeData())
                .forRecipes();

        behaviours.add(filtering);
    }

    @Override
    public void initialize() {
        super.initialize();
        notifyUpdate();
        if (level == null) return;
        if (level.isClientSide)
            invalidateRenderBoundingBox();
    }

    @Override
    public void tick() {
        super.tick();
        if (isController())
            bakeData.tick(this);

        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition)) {
            removeController(true);
            lastKnownPos = worldPosition;
            return;
        }

        if (updateConnectivity)
            updateConnectivity();
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

    private void refreshCapability() {
        itemCapability = null;
        invalidateCapabilities();
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        if (updateConnectivity)
            compound.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            compound.putInt("Size", radius);
            compound.putInt("Height", height);
        }

        super.write(compound, registries, clientPacket);

        compound.putString("StorageType", "CombinedInv");
        compound.put("Inventory", inventory.serializeNBT(registries));
        bakeData.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        assert level != null;
        super.read(compound, registries, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = radius;
        int prevHeight = height;

        updateConnectivity = compound.contains("Uninitialized");
        controller = null;
        lastKnownPos = null;

        if (compound.contains("LastKnownPos"))
            lastKnownPos = NBTHelper.readBlockPos(compound, "LastKnownPos");
        if (compound.contains("Controller"))
            controller = NBTHelper.readBlockPos(compound, "Controller");

        if (isController()) {
            radius = compound.getInt("Size");
            height = compound.getInt("Height");
        }

        inventory.deserializeNBT(registries, compound.getCompound("Inventory"));

        bakeData.read(compound, clientPacket);

        if (!clientPacket) {
            return;
        }

        boolean changeOfController =
                !Objects.equals(controllerBefore, controller);
        if (hasLevel() && (changeOfController || prevSize != radius || prevHeight != height)) {
            level.setBlocksDirty(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState());
            if (hasLevel())
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            invalidateRenderBoundingBox();
        }
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @SuppressWarnings("unchecked")
    @Override
    public OvenBlockEntity getControllerBE() {
        assert level != null;

        if (isController())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof OvenBlockEntity ovenBlockEntity)
            return ovenBlockEntity;
        return null;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.equals(controller);
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
        setChanged();
        sendData();
    }

    @Override
    public void removeController(boolean keepContents) {
        assert level != null;
        if (level.isClientSide())
            return;
        updateConnectivity = true;
        controller = null;
        radius = 1;
        height = 1;

        refreshCapability();
        bakeData.clear();
        setChanged();
        sendData();
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        assert level != null;

        level.setBlock(getBlockPos(), getBlockState().setValue(OvenBlock.IS_2x2, getWidth() == 2), 6);

        refreshCapability();
        updateOvenState();
        setChanged();
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

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getWidth() {
        return radius;
    }

    @Override
    public void setWidth(int width) {
        radius = width;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        OvenBlockEntity controllerBE = getControllerBE();
        if (controllerBE == null)
            return false;

        return controllerBE.bakeData.addToGoggleTooltip(tooltip, isPlayerSneaking, controllerBE.getTotalOvenSize());
    }

    public int getTotalOvenSize() {
        return radius * radius * height;
    }

    public void updateOvenState() {
        if (!isController() && getControllerBE() != null) {
            getControllerBE().updateOvenState();
            return;
        }

        if (bakeData.evaluate(this)) {
            notifyUpdate();
        }
    }

    public void updateBakeData() {
        OvenBlockEntity be = getControllerBE();
        if (be == null)
            return;
        be.bakeData.updateRequired = 2;
    }

    private void initCapability() {
        assert level != null;

        if (itemCapability != null)
            return;
        if (!isController()) {
            OvenBlockEntity controllerBE = getControllerBE();
            if (controllerBE == null)
                return;
            controllerBE.initCapability();
            itemCapability = controllerBE.itemCapability;
            return;
        }

        IItemHandlerModifiable[] invs = new IItemHandlerModifiable[height * radius * radius];
        inventories = new ArrayList<>();
        for (int xOffset = 0; xOffset < radius; xOffset++) {
            List<List<Inventory>> x = new ArrayList<>();
            inventories.add(x);
            for (int yOffset = 0; yOffset < height; yOffset++) {
                List<Inventory> y = new ArrayList<>();
                x.add(y);
                for (int zOffset = 0; zOffset < radius; zOffset++) {
                    BlockPos vaultPos = worldPosition.offset(xOffset, yOffset, zOffset);
                    OvenBlockEntity vaultAt =
                            ConnectivityHandler.partAt(CRBlockEntityTypes.OVEN_ENTITY.get(), level, vaultPos);
                    Inventory inv = vaultAt != null ? vaultAt.inventory : new Inventory();
                    invs[yOffset * radius * radius + xOffset * radius + zOffset] = inv;
                    y.add(inv);
                }
            }
        }
        itemCapability = new CombinedInvWrapper(invs);
    }

    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    public class Inventory extends ItemStackHandler {
        public int tickTillFinishCooking = -1;
        public Recipe<?> lastRecipe = null;

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (level == null) return stack;

            var controller = OvenBlockEntity.this.getControllerBE();
            FilteringBehaviour filter = controller == null ? null : controller.getFilter();
            ItemStack returnValue = super.insertItem(slot, stack, simulate);

            if (!simulate && returnValue.getCount() != stack.getCount()) {
                var singleInputInv = new SingleRecipeInput(getStackInSlot(slot));

                Optional<RecipeHolder<BakingRecipe>> bakingOpt = CRRecipeTypes.BAKING.find(singleInputInv, level);
                Optional<RecipeHolder<SmokingRecipe>> smokingOpt = level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, singleInputInv, level);

                Recipe<?> selectedRecipe = null;
                int cookingTime = -1;

                if (filter != null) {
                    boolean bakingMatches = bakingOpt.isPresent() && filter.test(bakingOpt.get().value().getResultItem(level.registryAccess()));
                    boolean smokingMatches = smokingOpt.isPresent() && filter.test(smokingOpt.get().value().getResultItem(level.registryAccess()));

                    if (bakingMatches) {
                        BakingRecipe bakingRecipe = bakingOpt.get().value();
                        selectedRecipe = bakingRecipe;
                        cookingTime = bakingRecipe.getProcessingDuration();
                    } else if (smokingMatches) {
                        SmokingRecipe smokingRecipe = smokingOpt.get().value();
                        selectedRecipe = smokingRecipe;
                        cookingTime = smokingRecipe.getCookingTime();
                    }
                } else {
                    if (bakingOpt.isPresent()) {
                        BakingRecipe bakingRecipe = bakingOpt.get().value();
                        selectedRecipe = bakingRecipe;
                        cookingTime = bakingRecipe.getProcessingDuration();
                    } else if (smokingOpt.isPresent()) {
                        SmokingRecipe smokingRecipe = smokingOpt.get().value();
                        selectedRecipe = smokingRecipe;
                        cookingTime = smokingRecipe.getCookingTime();
                    }
                }

                if (selectedRecipe != null) {
                    this.tickTillFinishCooking = cookingTime * ((getStackInSlot(slot).getCount() - 1) / 16 + 1);
                    this.lastRecipe = selectedRecipe;
                } else {
                    this.tickTillFinishCooking = -1;
                    this.lastRecipe = null;
                }
            }

            return returnValue;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!simulate) {
                tickTillFinishCooking = -1;
            }
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 16;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            assert level != null;
            var singleInputInv = new SingleRecipeInput(stack);

            Optional<RecipeHolder<BakingRecipe>> bakingOpt = CRRecipeTypes.BAKING.find(singleInputInv, level);
            Optional<RecipeHolder<SmokingRecipe>> smokingOpt = level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, singleInputInv, level);

            if (bakingOpt.isEmpty() && smokingOpt.isEmpty()) {
                return false;
            }

            var controller = OvenBlockEntity.this.getControllerBE();
            FilteringBehaviour filter = controller == null ? null : controller.getFilter();

            if (filter != null) {
                boolean bakingOutputMatches = bakingOpt.isPresent()
                        && filter.test(bakingOpt.get().value().getResultItem(level.registryAccess()));

                boolean smokingOutputMatches = smokingOpt.isPresent()
                        && filter.test(smokingOpt.get().value().getResultItem(level.registryAccess()));

                return bakingOutputMatches || smokingOutputMatches;
            }

            return true;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            CompoundTag tag = super.serializeNBT(provider);
            tag.putInt("tickTillFinishCooking", tickTillFinishCooking);
//            if (lastRecipe != null)
//                tag.putString("lastRecipe", lastRecipe.toString());
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            assert level != null;
            super.deserializeNBT(provider, nbt);
            if (nbt.contains("tickTillFinishCooking"))
                tickTillFinishCooking = nbt.getInt("tickTillFinishCooking");
//            if (nbt.contains("lastRecipe"))
//                lastRecipe = (SmokingRecipe) level.getRecipeManager().byKey(ResourceLocation.parse(nbt.getString("lastRecipe"))).orElse(null).value();
        }

        @Override
        protected void onContentsChanged(int slot) {
            assert level != null;
            if (!level.isClientSide) {
                setChanged();
                sendData();
                notifyUpdate();
            }
        }
    }


    public FilteringBehaviour getFilter() {
        return filtering;
    }

    public static class OvenValueBox extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 12, 16.05);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }
}
