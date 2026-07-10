package org.forsteri.ratatouille.content.demolder;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.forsteri.ratatouille.content.thresher.ThresherBlockEntity;
import org.forsteri.ratatouille.entry.CRRecipeTypes;
import org.forsteri.ratatouille.entry.CRTags;

import java.util.List;
import java.util.Optional;

public class MechanicalDemolderBlockEntity extends KineticBlockEntity  implements PressingBehaviour.PressingBehaviourSpecifics {

    public ItemStackHandler outputInv;
    public LazyOptional<IItemHandler> capability;
    public PressingBehaviour demoldingBehaviour;

    public MechanicalDemolderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.outputInv = new ItemStackHandler(1);
        this.capability = LazyOptional.of(MechanicalDemolderBlockEntity.DemolderInventoryHandler::new);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0, -1.5, 0)
                .expandTowards(0, 1, 0);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        demoldingBehaviour = new PressingBehaviour(this);
        behaviours.add(demoldingBehaviour);
    }

    public PressingBehaviour getPressingBehaviour() {
        return demoldingBehaviour;
    }

    private static final RecipeWrapper demoldingInv = new RecipeWrapper(new ItemStackHandler(1));

    public Optional<DemoldingRecipe> getRecipe(ItemStack item) {
        Optional<DemoldingRecipe> assemblyRecipe =
                SequencedAssemblyRecipe.getRecipe(level, item, CRRecipeTypes.DEMOLDING.getType(), DemoldingRecipe.class);
        if (assemblyRecipe.isPresent())
            return assemblyRecipe;

        demoldingInv.setItem(0, item);
        return CRRecipeTypes.DEMOLDING.find(demoldingInv, level);
    }

    @Override
    public boolean tryProcessOnBelt(TransportedItemStack input, List<ItemStack> outputList, boolean simulate) {
        Optional<DemoldingRecipe> recipeOptional = getRecipe(input.stack);
        if (recipeOptional.isEmpty())
            return false;

        DemoldingRecipe recipe = recipeOptional.get();

        List<ItemStack> outputs = RecipeApplier.applyRecipeOn(
                level,
                canProcessInBulk() ? input.stack : ItemHandlerHelper.copyStackWithSize(input.stack, 1),
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

    public void invalidate() {
        super.invalidate();
        this.capability.invalidate();
    }

    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(this.level, this.worldPosition, this.outputInv);
    }

    public void write(CompoundTag compound, boolean clientPacket) {
        compound.put("OutputInventory", this.outputInv.serializeNBT());
        super.write(compound, clientPacket);
    }

    protected void read(CompoundTag compound, boolean clientPacket) {
        this.outputInv.deserializeNBT(compound.getCompound("OutputInventory"));
        super.read(compound, clientPacket);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (isItemHandlerCap(cap))
            return capability.cast();
        return super.getCapability(cap, side);
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
    public void onPressingCompleted() {

    }

    @Override
    public boolean tryProcessInBasin(boolean simulate) {
        return false;
    }

    @Override
    public boolean tryProcessInWorld(ItemEntity itemEntity, boolean simulate) {
        return false;
    }

    @Override
    public boolean canProcessInBulk() {
        return false;
    }

    private class DemolderInventoryHandler extends CombinedInvWrapper {
        public DemolderInventoryHandler() {
            super(new IItemHandlerModifiable[]{MechanicalDemolderBlockEntity.this.outputInv});
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return super.extractItem(slot, amount, simulate);
        }
    }

    @Override
    public float calculateStressApplied() {
        return 8.0f;
    }
}
