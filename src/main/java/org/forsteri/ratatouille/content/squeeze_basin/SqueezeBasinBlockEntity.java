package org.forsteri.ratatouille.content.squeeze_basin;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.forsteri.ratatouille.entry.CRBlockEntityTypes;
import org.forsteri.ratatouille.entry.CRItems;
import org.forsteri.ratatouille.entry.CRRecipeTypes;
import org.forsteri.ratatouille.util.Lang;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinBlock.CASING;

public class SqueezeBasinBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public SqueezeBasinInventory inputInventory;
    public SmartFluidTankBehaviour inputTank;
    public SqueezingRecipe lastRecipe;
    protected SmartInventory outputInventory;
    protected IItemHandlerModifiable itemCapability;
    protected IFluidHandler fluidCapability;
    int recipeBackupCheck;
    private boolean contentsChanged;

    public SqueezeBasinBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inputInventory = (new SqueezeBasinInventory(1, this));
        this.inputInventory.whenContentsChanged($ -> this.contentsChanged = true).withMaxStackSize(64);
        this.outputInventory = (new SqueezeBasinInventory(1, this)).forbidInsertion().withMaxStackSize(64);
        this.itemCapability = new SqueezeBasinInventoryHandler();
        this.contentsChanged = true;
        this.recipeBackupCheck = 20;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CRBlockEntityTypes.SQUEEZE_BASIN_ENTITY.get(),
                (be, context) -> be.itemCapability
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CRBlockEntityTypes.SQUEEZE_BASIN_ENTITY.get(),
                (be, context) -> be.fluidCapability
        );
    }

    public boolean hasCasing() {
        return this.getBlockState().getValue(CASING);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this));
        this.inputTank = (new SmartFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, 1000, true)).whenFluidUpdates(() -> this.contentsChanged = true);
        behaviours.add(this.inputTank);
        fluidCapability = inputTank.getCapability();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null) return;
        if (!outputInventory.isEmpty() && !level.isClientSide)
            tryClearingSpoutputOverflow();
        if (contentsChanged)
            contentsChanged = false;
        if (getOperator().isEmpty() || getProcessingSpeed() == 0.0F || level.isClientSide)
            return;
        if (!outputInventory.isEmpty())
            return;

        PressingBehaviour behaviour = getOperator().get().getPressingBehaviour();
        if (behaviour.runningTicks == PressingBehaviour.CYCLE / 2)
            process();
        if (behaviour.running)
            return;

        if (inputInventory.getStackInSlot(0).isEmpty() && inputTank.isEmpty()) return;
        if (lastRecipe == null || !lastRecipe.match(this)) {
            Optional<RecipeHolder<SqueezingRecipe>> recipe = CRRecipeTypes.SQUEEZING.find(inputInventory, level);
            if (recipe.isPresent()) {
                lastRecipe = recipe.get().value();
                if (lastRecipe.match(this))
                    getOperator().ifPresent(be -> be.pressingBehaviour.start(PressingBehaviour.Mode.BASIN));
            }
            sendData();
        } else {
            if (lastRecipe.match(this))
                getOperator().ifPresent(be -> be.pressingBehaviour.start(PressingBehaviour.Mode.BASIN));
            sendData();
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (this.level == null) return;
        if (!this.level.isClientSide) {
            if (this.recipeBackupCheck-- <= 0) {
                this.recipeBackupCheck = 20;
                if (!this.isEmpty()) {
                    this.notifyChangeOfContents();
                }
            }
        }
    }

    public boolean isEmpty() {
        return this.inputInventory.isEmpty() && this.outputInventory.isEmpty();
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.put("InputItems", inputInventory.serializeNBT(registries));
        compound.put("OutputItems", outputInventory.serializeNBT(registries));
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        inputInventory.deserializeNBT(registries, compound.getCompound("InputItems"));
        outputInventory.deserializeNBT(registries, compound.getCompound("OutputItems"));
    }

    public void invalidate() {
        super.invalidate();
        this.invalidateCapabilities();
    }

    public void remove() {
        super.remove();
        this.onEmptied();
    }

    public void destroy() {
        super.destroy();
        if (this.level == null) return;
        ItemHelper.dropContents(this.level, this.worldPosition, this.inputInventory);
        ItemHelper.dropContents(this.level, this.worldPosition, this.outputInventory);
        if (this.getBlockState().getValue(CASING))
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), new ItemStack(CRItems.SAUSAGE_CASING.get(), 1));
    }

    public void onEmptied() {
        this.getOperator().ifPresent((be) -> be.basinRemoved = true);
    }

    private void tryClearingSpoutputOverflow() {
        if (this.level == null) return;
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof SqueezeBasinBlock))
            return;
        Direction direction = blockState.getValue(SqueezeBasinBlock.FACING);
        BlockEntity be = this.level.getBlockEntity(this.worldPosition.below().relative(direction.getOpposite()));
        InvManipulationBehaviour inserter = null;
        if (be != null) {
            inserter = BlockEntityBehaviour.get(this.level, be.getBlockPos(), InvManipulationBehaviour.TYPE);
        }
        IItemHandler targetInv = be == null ? null
                : Optional.ofNullable(level.getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), direction.getOpposite()))
                .orElse(inserter == null ? null : inserter.getInventory());

        for (int i = 0; i < outputInventory.getSlots(); i++) {
            ItemStack itemStack = outputInventory.getStackInSlot(i);
            if (targetInv != null) {
                if (ItemHandlerHelper.insertItemStacked(targetInv, itemStack, true).isEmpty()) {
                    ItemHandlerHelper.insertItemStacked(targetInv, itemStack.copy(), false);
                    outputInventory.setStackInSlot(i, ItemStack.EMPTY);
                    notifyChangeOfContents();
                    sendData();
                }
            }
        }
    }

    public Optional<MechanicalPressBlockEntity> getOperator() {
        if (this.level == null) {
            return Optional.empty();
        } else {
            BlockEntity be = this.level.getBlockEntity(this.worldPosition.above(2));
            return be instanceof MechanicalPressBlockEntity ? Optional.of((MechanicalPressBlockEntity) be) : Optional.empty();
        }
    }

    public float getProcessingSpeed() {
        if (getOperator().isPresent()) {
            MechanicalPressBlockEntity be = getOperator().get();
            return be.pressingBehaviour.getRunningTickSpeed();
        } else {
            return 0F;
        }
    }

    private void process() {
        if (level == null) return;
        if (this.lastRecipe == null || !lastRecipe.match(this)) {
            Optional<RecipeHolder<SqueezingRecipe>> recipe = CRRecipeTypes.SQUEEZING.find(inputInventory, this.level);
            if (recipe.isEmpty()) {
                return;
            }
            this.lastRecipe = recipe.get().value();
        }


        boolean useCasing = this.lastRecipe.useCasing();
        if (useCasing != getBlockState().getValue(CASING))
            return;

        if (useCasing)
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(CASING, false));
        if (!this.lastRecipe.getFluidIngredients().isEmpty())
            this.fluidCapability.drain(lastRecipe.getFluidIngredients().getFirst().amount(), IFluidHandler.FluidAction.EXECUTE);

        ItemStack stackInSlot = this.inputInventory.getStackInSlot(0);
        stackInSlot.shrink(1);
        this.inputInventory.setStackInSlot(0, stackInSlot);
        acceptOutputs(this.lastRecipe.rollResults(level.random), false);
        notifyChangeOfContents();
        notifyUpdate();
    }

    public void notifyChangeOfContents() {
        this.contentsChanged = true;
    }

    public boolean acceptOutputs(List<ItemStack> outputItems, boolean simulate) {
        this.outputInventory.allowInsertion();
        boolean acceptOutputsInner = this.acceptOutputsInner(outputItems, simulate);
        this.outputInventory.forbidInsertion();
        return acceptOutputsInner;
    }

    private boolean acceptOutputsInner(List<ItemStack> outputItems, boolean simulate) {
        BlockState blockState = this.getBlockState();
        if (!(blockState.getBlock() instanceof SqueezeBasinBlock)) {
            return false;
        } else {
            if (simulate) {
                return true;
            } else {
                IItemHandler targetInv = outputInventory;
                if (targetInv == null)
                    return false;
                if (!acceptItemOutputsIntoBasin(outputItems, simulate, targetInv))
                    return false;
                return true;
            }
        }
    }

    private boolean acceptItemOutputsIntoBasin(@NotNull List<ItemStack> outputItems, boolean simulate, IItemHandler targetInv) {
        for (ItemStack itemStack : outputItems) {
            if (!ItemHandlerHelper.insertItemStacked(targetInv, itemStack.copy(), simulate)
                    .isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("gui.goggles.squeeze_basin_contents")
                .forGoggles(tooltip);

        if (itemCapability == null)
            itemCapability = new ItemStackHandler();
        if (fluidCapability == null)
            fluidCapability = new FluidTank(0);

        boolean isEmpty = true;

        for (int i = 0; i < itemCapability.getSlots(); i++) {
            ItemStack stackInSlot = itemCapability.getStackInSlot(i);
            if (stackInSlot.isEmpty())
                continue;
            Lang.text("")
                    .add(Component.translatable(stackInSlot.getDescriptionId())
                            .withStyle(ChatFormatting.GRAY))
                    .add(Lang.text(" x" + stackInSlot.getCount())
                            .style(ChatFormatting.GREEN))
                    .forGoggles(tooltip, 1);
            isEmpty = false;
        }

        LangBuilder mb = Lang.translate("generic.unit.millibuckets");
        for (int i = 0; i < fluidCapability.getTanks(); i++) {
            FluidStack fluidStack = fluidCapability.getFluidInTank(i);
            if (fluidStack.isEmpty())
                continue;
            Lang.text("")
                    .add(Lang.fluidName(fluidStack)
                            .add(Lang.text(" "))
                            .style(ChatFormatting.GRAY)
                            .add(Lang.number(fluidStack.getAmount())
                                    .add(mb)
                                    .style(ChatFormatting.BLUE)))
                    .forGoggles(tooltip, 1);
            isEmpty = false;
        }

        if (getBlockState().getValue(CASING)) {
            Lang.text("")
                    .add(Component.translatable(CRItems.SAUSAGE_CASING.get().getDescriptionId())
                            .withStyle(ChatFormatting.GRAY))
                    .forGoggles(tooltip, 1);
        }


        if (isEmpty)
            tooltip.removeFirst();

        return true;
    }

    private class SqueezeBasinInventoryHandler extends CombinedInvWrapper {
        public SqueezeBasinInventoryHandler() {
            super(SqueezeBasinBlockEntity.this.inputInventory, SqueezeBasinBlockEntity.this.outputInventory);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (SqueezeBasinBlockEntity.this.outputInventory == this.getHandlerFromIndex(this.getIndexForSlot(slot)))
                return stack;
            if (!this.isItemValid(slot, stack))
                return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (SqueezeBasinBlockEntity.this.inputInventory == this.getHandlerFromIndex(getIndexForSlot(slot)))
                return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (SqueezeBasinBlockEntity.this.outputInventory == this.getHandlerFromIndex(this.getIndexForSlot(slot)))
                return false;
            return super.isItemValid(slot, stack);
        }
    }

}
