package org.forsteri.ratatouille.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.forge.ForgeTypes;
import net.createmod.catnip.data.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.forsteri.ratatouille.compat.jei.category.animations.AnimatedCompostTower;
import org.forsteri.ratatouille.content.compost_tower.CompostingRecipe;

import java.util.ArrayList;
import java.util.List;

public class CompostingCategory extends CreateRecipeCategory<CompostingRecipe> {

    private final AnimatedCompostTower tower = new AnimatedCompostTower();
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();

    public CompostingCategory(Info<CompostingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CompostingRecipe recipe, IFocusGroup focuses) {

        /*
         * ======================
         * 物品输入（左侧 3 个）
         * ======================
         */
        List<Pair<Ingredient, MutableInt>> itemInputs =
                ItemHelper.condenseIngredients(recipe.getIngredients());

        for (int i = 0; i < Math.min(3, itemInputs.size()); i++) {
            Pair<Ingredient, MutableInt> pair = itemInputs.get(i);
            List<ItemStack> stacks = new ArrayList<>();

            for (ItemStack is : pair.getFirst().getItems()) {
                ItemStack copy = is.copy();
                copy.setCount(pair.getSecond().getValue());
                stacks.add(copy);
            }

            builder.addSlot(
                            RecipeIngredientRole.INPUT,
                            5,
                            17 + i * 19
                    )
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addItemStacks(stacks);
        }

        /*
         * ======================
         * 流体输入（中左 3 个）
         * ======================
         */
        List<FluidStack> fluidInputs = recipe.getFluidIngredients()
                .stream()
                .map(fi -> fi.getMatchingFluidStacks())
                .flatMap(List::stream)
                .limit(3)
                .toList();

        for (int i = 0; i < fluidInputs.size(); i++) {
            FluidStack stack = fluidInputs.get(i);

            builder.addSlot(
                            RecipeIngredientRole.INPUT,
                            28,
                            17 + i * 19
                    )
                    .setBackground(getRenderedSlot(), -1, -1)
                    .setFluidRenderer(stack.getAmount(), false, 16, 16)
                    .addIngredient(ForgeTypes.FLUID_STACK, stack);
        }

        /*
         * ======================
         * 物品输出（右侧 3 个）
         * ======================
         */
        List<ItemStack> itemOutputs = recipe.getRollableResults()
                .stream()
                .map(r -> r.getStack())
                .toList();

        for (int i = 0; i < Math.min(3, itemOutputs.size()); i++) {
            builder.addSlot(
                            RecipeIngredientRole.OUTPUT,
                            124,
                            17 + i * 19
                    )
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addItemStack(itemOutputs.get(i));
        }

        /*
         * ======================
         * 流体输出（最右 3 个）
         * ======================
         */
        List<FluidStack> fluidOutputs = recipe.getFluidResults();

        for (int i = 0; i < Math.min(3, fluidOutputs.size()); i++) {
            FluidStack stack = fluidOutputs.get(i);

            builder.addSlot(
                            RecipeIngredientRole.OUTPUT,
                            147,
                            17 + i * 19
                    )
                    .setBackground(getRenderedSlot(), -1, -1)
                    .setFluidRenderer(stack.getAmount(), false, 16, 16)
                    .addIngredient(ForgeTypes.FLUID_STACK, stack);
        }
    }

    protected void renderWidgets(GuiGraphics graphics, CompostingRecipe recipe, double mouseX, double mouseY) {
        getBlockShadow().render(graphics, 65, 39);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 54, 51);
    }

    protected AllGuiTextures getBlockShadow() {
        return AllGuiTextures.JEI_LIGHT;
    }

    @Override
    public void draw(CompostingRecipe recipe, IRecipeSlotsView view, GuiGraphics g, double mouseX, double mouseY) {
        PoseStack stack = g.pose();

        renderWidgets(g, recipe, mouseX, mouseY);
        stack.pushPose();
        stack.translate(75, -15, 0);

        stack.pushPose();
        stack.translate(0, 20, -7);
        heater.withHeat(recipe.getRequiredHeat().visualizeAsBlazeBurner()).draw(g);
        stack.popPose();

        tower.draw(g);
        stack.popPose();
    }
}
