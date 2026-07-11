package org.forsteri.ratatouille.content.oven;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.forsteri.ratatouille.entry.CRBlockEntityTypes;
import org.forsteri.ratatouille.entry.CRRecipeTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OvenBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IMultiBlockEntityContainer {
    public LazyOptional<CombinedInvWrapper> itemCapability = LazyOptional.empty();
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
        }

        super.write(compound, clientPacket);

        compound.putString("StorageType", "CombinedInv");
        compound.put("Inventory", inventory.serializeNBT());
        bakeData.write(compound, clientPacket);
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
        }

        inventory.deserializeNBT(compound.getCompound("Inventory"));

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

    private void refreshCapability() {
        itemCapability.invalidate();
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

        itemCapability.invalidate();
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

        itemCapability.invalidate();
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

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (isItemHandlerCap(cap)) {
            initCapability();
            return itemCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    private void initCapability() {
        assert level != null;

        if (itemCapability.isPresent())
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

        CombinedInvWrapper itemHandler = new CombinedInvWrapper(invs);
        itemCapability = LazyOptional.of(() -> itemHandler);
    }

    public class Inventory extends ItemStackHandler {
        private final RecipeWrapper RECIPE_WRAPPER = new RecipeWrapper(new ItemStackHandler(1));
        public int tickTillFinishCooking = -1;
        public Recipe<?> lastRecipe = null;

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (level == null) return stack;

            var controller = OvenBlockEntity.this.getControllerBE();
            FilteringBehaviour filter = controller == null ? null : controller.getFilter();
            ItemStack returnValue = super.insertItem(slot, stack, simulate);

            if (!simulate && returnValue.getCount() != stack.getCount()) {
                RECIPE_WRAPPER.setItem(0, getStackInSlot(slot));

                var bakingOpt = CRRecipeTypes.BAKING.find(RECIPE_WRAPPER, level);
                var smokingOpt = level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, RECIPE_WRAPPER, level);

                Recipe<?> selectedRecipe = null;
                int cookingTime = -1;

                if (filter != null) {
                    boolean bakingMatches = bakingOpt.isPresent() && filter.test(bakingOpt.get().getResultItem(level.registryAccess()));
                    boolean smokingMatches = smokingOpt.isPresent() && filter.test(smokingOpt.get().getResultItem(level.registryAccess()));

                    if (bakingMatches) {
                        BakingRecipe bakingRecipe = (BakingRecipe) bakingOpt.get();
                        selectedRecipe = bakingRecipe;
                        cookingTime = bakingRecipe.getProcessingDuration();
                    } else if (smokingMatches) {
                        SmokingRecipe smokingRecipe = smokingOpt.get();
                        selectedRecipe = smokingRecipe;
                        cookingTime = smokingRecipe.getCookingTime();
                    }
                } else {
                    if (bakingOpt.isPresent()) {
                        BakingRecipe bakingRecipe = (BakingRecipe) bakingOpt.get();
                        selectedRecipe = bakingRecipe;
                        cookingTime = bakingRecipe.getProcessingDuration();
                    } else if (smokingOpt.isPresent()) {
                        SmokingRecipe smokingRecipe = smokingOpt.get();
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
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
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
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            assert level != null;

            RECIPE_WRAPPER.setItem(0, stack);
            var bakingOpt = CRRecipeTypes.BAKING.find(RECIPE_WRAPPER, level);
            var smokingOpt = level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, RECIPE_WRAPPER, level);
            if (bakingOpt.isEmpty() && smokingOpt.isEmpty()) {
                return false;
            }

            var controller = OvenBlockEntity.this.getControllerBE();
            FilteringBehaviour filter = controller == null ? null : controller.getFilter();

            if (filter != null) {
                boolean bakingOutputMatches = bakingOpt.isPresent()
                        && filter.test(bakingOpt.get().getResultItem(level.registryAccess()));

                boolean smokingOutputMatches = smokingOpt.isPresent()
                        && filter.test(smokingOpt.get().getResultItem(level.registryAccess()));

                return bakingOutputMatches || smokingOutputMatches;
            }

            return true;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = super.serializeNBT();
            tag.putInt("tickTillFinishCooking", tickTillFinishCooking);
//            if (lastRecipe != null)
//                tag.putString("lastRecipe", lastRecipe.getId().toString());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            assert level != null;
            super.deserializeNBT(nbt);
            if (nbt.contains("tickTillFinishCooking"))
                tickTillFinishCooking = nbt.getInt("tickTillFinishCooking");
//            if (nbt.contains("lastRecipe"))
//                lastRecipe = (SmokingRecipe) level.getRecipeManager().byKey(new ResourceLocation(nbt.getString("lastRecipe"))).orElse(null);


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
        public boolean shouldRender(LevelAccessor level, BlockPos pos, BlockState state) {

            if (!super.shouldRender(level, pos, state))
                return false;

            BlockPos neighbourPos = pos.relative(getSide());

            BlockEntity be = level.getBlockEntity(neighbourPos);

            if (!(be instanceof OvenBlockEntity other))
                return true;

            BlockEntity current = level.getBlockEntity(pos);

            if (!(current instanceof OvenBlockEntity self))
                return true;

            return !self.getController().equals(other.getController());
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }
}
