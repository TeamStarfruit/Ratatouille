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

    private static final int SLOT = 18;
    private static final int COLUMNS = 3;
    private static final int CENTER_Y = 57;

    private final AnimatedCompostTower tower = new AnimatedCompostTower();
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();

    public CompostingCategory(Info<CompostingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CompostingRecipe recipe, IFocusGroup focuses) {
        List<SlotEntry> inputs = new ArrayList<>();

        for (Pair<Ingredient, MutableInt> pair : ItemHelper.condenseIngredients(recipe.getIngredients())) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack is : pair.getFirst().getItems()) {
                ItemStack copy = is.copy();
                copy.setCount(pair.getSecond().getValue());
                stacks.add(copy);
            }
            inputs.add(SlotEntry.item(stacks));
        }

        recipe.getFluidIngredients()
                .forEach(fi -> fi.getMatchingFluidStacks()
                        .forEach(fs -> inputs.add(SlotEntry.fluid(fs))));

        List<SlotEntry> outputs = new ArrayList<>();

        recipe.getRollableResults()
                .forEach(r -> outputs.add(SlotEntry.item(List.of(r.getStack()))));

        recipe.getFluidResults()
                .forEach(fs -> outputs.add(SlotEntry.fluid(fs)));

        int bgWidth = getBackground().getWidth();
        int padding = 8;

        int inputX = padding;
        int outputX = bgWidth - (COLUMNS * SLOT) - padding;

        layoutMixedSlots(builder, inputs, RecipeIngredientRole.INPUT, inputX);
        layoutMixedSlots(builder, outputs, RecipeIngredientRole.OUTPUT, outputX);
    }

    private void layoutMixedSlots(
            IRecipeLayoutBuilder builder,
            List<SlotEntry> entries,
            RecipeIngredientRole role,
            int baseX
    ) {
        int rows = (int) Math.ceil(entries.size() / (double) COLUMNS);
        int startY = CENTER_Y - (rows * SLOT) / 2;

        for (int row = 0; row < rows; row++) {
            int rowStart = row * COLUMNS;
            int rowEnd = Math.min(rowStart + COLUMNS, entries.size());
            int countInRow = rowEnd - rowStart;

            int rowWidth = countInRow * SLOT;
            int startX = baseX + (COLUMNS * SLOT - rowWidth) / 2;

            for (int i = 0; i < countInRow; i++) {
                int index = rowStart + i;
                int x = startX + i * SLOT;
                int y = startY + row * SLOT;

                SlotEntry entry = entries.get(index);

                var slot = builder.addSlot(role, x, y)
                        .setBackground(getRenderedSlot(), -1, -1);

                if (entry.isFluid()) {
                    FluidStack fs = entry.fluid;
                    slot.setFluidRenderer(fs.getAmount(), false, 16, 16)
                            .addIngredient(ForgeTypes.FLUID_STACK, fs);
                } else {
                    slot.addItemStacks(entry.items);
                }
            }
        }
    }

    @Override
    public void draw(
            CompostingRecipe recipe,
            IRecipeSlotsView view,
            GuiGraphics g,
            double mouseX,
            double mouseY
    ) {
        PoseStack pose = g.pose();
        int centerX = getBackground().getWidth() / 2;

        pose.pushPose();
        pose.translate(centerX - 10, -15, 0);

        getBlockShadow().render(g, -11-6, 54);

        AllGuiTextures.JEI_ARROW.render(g, -11, 66);

        pose.pushPose();
        pose.translate(-6, 20, -7);
        heater.withHeat(recipe.getRequiredHeat().visualizeAsBlazeBurner()).draw(g);
        pose.popPose();

        pose.pushPose();
        pose.translate(-6, 0, 0);
        tower.draw(g);
        pose.popPose();

        pose.popPose();
    }

    protected AllGuiTextures getBlockShadow() {
        return AllGuiTextures.JEI_LIGHT;
    }

    private static class SlotEntry {
        final List<ItemStack> items;
        final FluidStack fluid;

        private SlotEntry(List<ItemStack> items, FluidStack fluid) {
            this.items = items;
            this.fluid = fluid;
        }

        static SlotEntry item(List<ItemStack> stacks) {
            return new SlotEntry(stacks, null);
        }

        static SlotEntry fluid(FluidStack stack) {
            return new SlotEntry(null, stack);
        }

        boolean isFluid() {
            return fluid != null;
        }
    }
}
