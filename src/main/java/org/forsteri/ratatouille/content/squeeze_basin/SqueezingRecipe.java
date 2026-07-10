package org.forsteri.ratatouille.content.squeeze_basin;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.forsteri.ratatouille.entry.CRItems;
import org.forsteri.ratatouille.entry.CRRecipeTypes;
import org.jetbrains.annotations.NotNull;

import static org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinBlock.CASING;

public class SqueezingRecipe extends ProcessingRecipe<SqueezeBasinInventory> {
    public SqueezingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CRRecipeTypes.SQUEEZING, params);
    }

    public boolean match(@NotNull SqueezeBasinBlockEntity be, boolean hasCasing) {
        if (be.getOperator().isEmpty())
            return false;

        IFluidHandler inputTank = be.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElse(null);

        if (be.inputInventory == null || inputTank == null)
            return false;

        boolean useCasing = useCasing();
        if (useCasing != hasCasing)
            return false;

        if (!ingredients.isEmpty() && (!useCasing || ingredients.size() > 1)) {
            boolean matched = false;

            for (Ingredient ingredient : ingredients) {
                if (ingredient.test(be.inputInventory.getItem(0))) {
                    matched = true;
                    break;
                }
            }

            if (!matched)
                return false;
        }

        if (!fluidIngredients.isEmpty()) {

            FluidStack tankFluid = inputTank.getFluidInTank(0);
            var ingredient = fluidIngredients.get(0);

            return ingredient.test(tankFluid);
        }

        return true;
    }

    public boolean useCasing() {
        for (Ingredient ingredient : ingredients) {
            if (ingredient.test(CRItems.SAUSAGE_CASING.asStack())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getMaxInputCount() {
        return 2;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    public boolean matches(@NotNull SqueezeBasinInventory smartInventory, @NotNull Level level) {
        if (!fluidIngredients.isEmpty()) {
            if (smartInventory.blockEntity == null) return false;
            var fluidHandler = smartInventory.blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
            if (fluidHandler == null) return false;

            boolean result = false;
            for (FluidIngredient ingredient : fluidIngredients) {
                if (ingredient.test(fluidHandler.getFluidInTank(0))) {
                    result = true;
                    break;
                }
            }
            if (!result) return false;
        }

        boolean useCasing = useCasing();
        if (ingredients.isEmpty()) {
            return true;
        }
        
        for (Ingredient ingredient : ingredients) {
            if (ingredient.test(CRItems.SAUSAGE_CASING.asStack())) {
                if (smartInventory.blockEntity == null) return false;
                if (!smartInventory.blockEntity.getBlockState().getValue(CASING)) return false;
            } else if (ingredient.test(smartInventory.getItem(0))) {
                return true;
            }
        }

        return useCasing && ingredients.size() == 1;
    }

}
