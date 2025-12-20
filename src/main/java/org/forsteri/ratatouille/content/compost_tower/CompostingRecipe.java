package org.forsteri.ratatouille.content.compost_tower;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.forsteri.ratatouille.entry.CRRecipeTypes;
import org.jetbrains.annotations.NotNull;

public class CompostingRecipe extends ProcessingRecipe<RecipeWrapper> {

    public static void apply(CompostTowerBlockEntity controller, CompostingRecipe recipe) {
        var inputInventory = controller.inputInventory;
        var outputInventory = controller.outputInventory;
        var tankInventory = controller.tankInventory;

        recipe.getIngredients().forEach(ingredient -> {
            for (int slot = 0; slot < inputInventory.getSlots(); slot++) {
                var stackInSlot = inputInventory.getStackInSlot(slot);

                for (var item: ingredient.getItems()) {
                    if (item.is(stackInSlot.getItem())
                            && stackInSlot.getCount() >= item.getCount()) {
                        inputInventory.setStackInSlot(slot, stackInSlot.copyWithCount(
                                stackInSlot.getCount() - item.getCount()
                        ));
                        return;
                    }
                }
            }
        });

        recipe.getFluidIngredients().forEach(fluidIngredient -> {
            for (int tank = 0; tank < tankInventory.getTanks(); tank++) {
                var fluidInTank = tankInventory.getFluidInTank(tank);

                var fluidToDrain = fluidInTank.copy();
                fluidToDrain.setAmount(fluidIngredient.getRequiredAmount());
                if (fluidIngredient.test(fluidInTank)) {
                    tankInventory.drain(fluidToDrain, IFluidHandler.FluidAction.EXECUTE);
                    return;
                }
            }
        });

        recipe.rollResults().forEach((stack) -> {
            ItemHandlerHelper.insertItemStacked(outputInventory, stack, false);
        });
        recipe.getFluidResults().forEach((fluidStack) -> {
            tankInventory.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        });
    }

    public static boolean match(CompostTowerBlockEntity controller, Recipe<?> recipe) {
        if (!(recipe instanceof CompostingRecipe compostingRecipe)) return false;

        var testInv = new ItemStackHandler(controller.inputInventory.getSlots());
        ItemHelper.copyContents(controller.inputInventory, testInv);

        for (var itemIngredient: compostingRecipe.getIngredients()) {
            boolean found = false;
            for (int slot = 0; slot < testInv.getSlots(); slot++) {
                var stackInSlot = testInv.getStackInSlot(slot);

                for (var item: itemIngredient.getItems()) {
                    if (item.is(stackInSlot.getItem())
                            && stackInSlot.getCount() >= item.getCount()) {
                        found = true;
                        testInv.setStackInSlot(slot, stackInSlot.copyWithCount(
                                stackInSlot.getCount() - item.getCount()
                        ));
                        break;
                    }
                }
            }
            if (!found) return false;
        }

        for (var fluidIngredient: compostingRecipe.getFluidIngredients()) {
            boolean found = false;
            for (int tank = 0; tank < controller.tankInventory.getTanks(); tank++) {
                var fluidInTank = controller.tankInventory.getFluidInTank(tank);

                if (fluidIngredient.test(fluidInTank) && fluidInTank.getAmount() >= fluidIngredient.getRequiredAmount()) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        return true;
    }
    public CompostingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CRRecipeTypes.COMPOSTING, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 3 * 64;
    }

    @Override
    protected int getMaxOutputCount() {
        return 3 * 64;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 3;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 3;
    }

    // only for CompostTowerBlockEntity::canProcess, not used in recipe matching
    @Override
    public boolean matches(@NotNull RecipeWrapper inv, @NotNull Level world) {
        for (Ingredient ingredient : ingredients) {
            if (ingredient.test(inv.getItem(0))) {
                return true;
            }
        }
        return false;
    }
}
