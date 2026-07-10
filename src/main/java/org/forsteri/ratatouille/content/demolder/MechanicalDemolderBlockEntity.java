package org.forsteri.ratatouille.content.demolder;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.forsteri.ratatouille.entry.CRBlockEntityTypes;
import org.forsteri.ratatouille.entry.CRRecipeTypes;
import org.forsteri.ratatouille.entry.CRTags;

import java.util.List;
import java.util.Optional;

public class MechanicalDemolderBlockEntity extends KineticBlockEntity implements PressingBehaviour.PressingBehaviourSpecifics {

    public ItemStackHandler outputInv;
    public IItemHandler capability;
    public PressingBehaviour demoldingBehaviour;

    public MechanicalDemolderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.outputInv = new ItemStackHandler(1);
        this.capability = new MechanicalDemolderBlockEntity.DemolderInventoryHandler();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CRBlockEntityTypes.MECHANICAL_DEMOLDER_ENTITY.get(),
                (be, context) -> be.capability
        );
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0, -1.5, 0)
                .expandTowards(0, 1, 0);
    }

    public PressingBehaviour getPressingBehaviour() {
        return demoldingBehaviour;
    }

    public void invalidate() {
        super.invalidate();
        invalidateCapabilities();
    }

    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(this.level, this.worldPosition, this.outputInv);
    }

    @Override
    public boolean tryProcessInBasin(boolean simulate) {
        return false;
    }

    @Override
    public boolean tryProcessOnBelt(TransportedItemStack input, List<ItemStack> outputList, boolean simulate) {

        Optional<RecipeHolder<DemoldingRecipe>> recipeHolder = getRecipe(input.stack);
        if (recipeHolder.isEmpty())
            return false;

        DemoldingRecipe recipe = recipeHolder.get().value();

        List<ItemStack> outputs = RecipeApplier.applyRecipeOn(
                level,
                canProcessInBulk() ? input.stack : new ItemStack(input.stack.getItem(), 1),
                recipe,
                true
        );

        for (ItemStack stack : outputs) {
            if (!stack.is(CRTags.MOLD))
                continue;

            if (!outputInv.insertItem(0, stack.copy(), true).isEmpty())
                return false;
        }

        if (simulate)
            return true;

        demoldingBehaviour.particleItems.add(input.stack);

        for (ItemStack stack : outputs) {

            if (stack.is(CRTags.MOLD)) {
                outputInv.insertItem(0, stack.copy(), false);
            } else {
                outputList.add(stack);
            }

        }

        return true;
    }

    public Optional<RecipeHolder<DemoldingRecipe>> getRecipe(ItemStack item) {
        if (level == null) return Optional.empty();
        Optional<RecipeHolder<DemoldingRecipe>> assemblyRecipe =
                SequencedAssemblyRecipe.getRecipe(level, item, CRRecipeTypes.DEMOLDING.getType(), DemoldingRecipe.class);
        if (assemblyRecipe.isPresent())
            return assemblyRecipe;

        return CRRecipeTypes.DEMOLDING.find(new SingleRecipeInput(item), level);
    }

    @Override
    public boolean tryProcessInWorld(ItemEntity itemEntity, boolean simulate) {
        return false;
    }

    @Override
    public boolean canProcessInBulk() {
        return false;
    }

    @Override
    public void onPressingCompleted() {

    }

    @Override
    public int getParticleAmount() {
        return 15;
    }

    @Override
    public float getKineticSpeed() {
        return getSpeed();
    }

    @Override
    public float calculateStressApplied() {
        return 8.0f;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.put("OutputInventory", this.outputInv.serializeNBT(registries));
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        this.outputInv.deserializeNBT(registries, compound.getCompound("OutputInventory"));
        super.read(compound, registries, clientPacket);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        demoldingBehaviour = new PressingBehaviour(this);
        behaviours.add(demoldingBehaviour);
    }

    private class DemolderInventoryHandler extends CombinedInvWrapper {
        public DemolderInventoryHandler() {
            super(new IItemHandlerModifiable[]{MechanicalDemolderBlockEntity.this.outputInv});
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }
}
