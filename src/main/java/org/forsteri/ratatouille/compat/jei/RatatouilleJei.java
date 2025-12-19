package org.forsteri.ratatouille.compat.jei;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.ItemIcon;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.compat.jei.category.*;
import org.forsteri.ratatouille.content.compost_tower.CompostingRecipe;
import org.forsteri.ratatouille.content.demolder.DemoldingRecipe;
import org.forsteri.ratatouille.content.frozen_block.FreezingRecipe;
import org.forsteri.ratatouille.content.oven.BakingRecipe;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezingRecipe;
import org.forsteri.ratatouille.content.thresher.ThreshingRecipe;
import org.forsteri.ratatouille.entry.CRBlocks;
import org.forsteri.ratatouille.entry.CRRecipeTypes;
import org.forsteri.ratatouille.util.Lang;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@JeiPlugin
@ParametersAreNonnullByDefault
public class RatatouilleJei implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation(Ratatouille.MOD_ID, "jei_plugin");
    private final List<CreateRecipeCategory<?>> allCategories = new ArrayList();
    private IIngredientManager ingredientManager;

    public RatatouilleJei() {
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        allCategories.clear();
        final CreateRecipeCategory<?> threshing = builder(ThreshingRecipe.class)
                .addTypedRecipes(CRRecipeTypes.THRESHING::getType)
                .catalyst(CRBlocks.THRESHER::get)
                .itemIcon(CRBlocks.THRESHER.get())
                .emptyBackground(177, 53)
                .build("threshing", ThreshingCategory::new);

        final CreateRecipeCategory<?> squeezing = builder(SqueezingRecipe.class)
                .addTypedRecipes(CRRecipeTypes.SQUEEZING::getType)
                .catalyst(AllBlocks.MECHANICAL_PRESS::get)
                .catalyst(CRBlocks.SQUEEZE_BASIN::get)
                .doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), CRBlocks.SQUEEZE_BASIN.get())
                .emptyBackground(177, 103)
                .build("squeezing", SqueezingCategory::new);

        final CreateRecipeCategory<?> demolding = builder(DemoldingRecipe.class)
                .addTypedRecipes(CRRecipeTypes.DEMOLDING::getType)
                .catalyst(CRBlocks.MECHANICAL_DEMOLDER::get)
                .emptyBackground(177, 70)
                .build("demolding", DemoldingCategory::new);

        final CreateRecipeCategory<?> baking = builder(BakingRecipe.class)
                .addTypedRecipes(CRRecipeTypes.BAKING::getType)
                .catalyst(CRBlocks.OVEN::get)
                .itemIcon(CRBlocks.OVEN.get())
                .emptyBackground(178, 72)
                .build("baking", BakingCategory::new);

        final CreateRecipeCategory<?> bakeSmoking = builder(SmokingRecipe.class)
                .addTypedRecipes(() -> RecipeType.SMOKING)
                .catalyst(CRBlocks.OVEN::get)
                .doubleItemIcon(CRBlocks.OVEN.get(), Items.CAMPFIRE)
                .emptyBackground(178, 72)
                .build("bakesmoking", BakeSmokingCategory::new);

        final CreateRecipeCategory<?> freezing = builder(FreezingRecipe.class)
                .addTypedRecipes(CRRecipeTypes.FREEZING::getType)
                .catalyst(CRBlocks.FROZEN_BLOCK::get)
                .itemIcon(CRBlocks.FROZEN_BLOCK.get())
                .emptyBackground(178, 72)
                .build("freezing", FreezingCategory::new);

        final CreateRecipeCategory<?> composting = builder(CompostingRecipe.class)
                .addTypedRecipes(CRRecipeTypes.COMPOSTING::getType)
                .catalyst(CRBlocks.COMPOST_TOWER_BLOCK::get)
                .itemIcon(CRBlocks.COMPOST_TOWER_BLOCK.get())
                .emptyBackground(185, 72)
                .build("composting", CompostingCategory::new);
        allCategories.forEach(registration::addRecipeCategories);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ingredientManager = registration.getIngredientManager();
        allCategories.forEach(c -> c.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        allCategories.forEach(c -> c.registerCatalysts(registration));
    }

    private <T extends Recipe<?>> CategoryBuilder<T> builder(Class<? extends T> recipeClass) {
        return new CategoryBuilder<>(recipeClass);
    }

    private class CategoryBuilder<T extends Recipe<?>> {
        private final Class<? extends T> recipeClass;
        private final List<Consumer<List<T>>> recipeListConsumers = new ArrayList<>();
        private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();
        private Predicate<CRecipes> predicate = cRecipes -> true;
        private IDrawable background;
        private IDrawable icon;

        public CategoryBuilder(Class<? extends T> recipeClass) {
            this.recipeClass = recipeClass;
        }

        public CategoryBuilder<T> addTypedRecipes(IRecipeTypeInfo recipeTypeEntry) {
            return addTypedRecipes(recipeTypeEntry::getType);
        }

        public CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipes::add, recipeType.get()));
        }

        public CategoryBuilder<T> addRecipeListConsumer(Consumer<List<T>> consumer) {
            recipeListConsumers.add(consumer);
            return this;
        }

        public CategoryBuilder<T> addTypedRecipesIf(Supplier<RecipeType<? extends T>> recipeType, Predicate<Recipe<?>> pred) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipe -> {
                if (pred.test(recipe)) {
                    recipes.add(recipe);
                }
            }, recipeType.get()));
        }

        public CategoryBuilder<T> addTypedRecipesExcluding(Supplier<RecipeType<? extends T>> recipeType,
                                                           Supplier<RecipeType<? extends T>> excluded) {
            return addRecipeListConsumer(recipes -> {
                List<Recipe<?>> excludedRecipes = CreateJEI.getTypedRecipes(excluded.get());
                CreateJEI.<T>consumeTypedRecipes(recipe -> {
                    for (Recipe<?> excludedRecipe : excludedRecipes) {
                        if (CreateJEI.doInputsMatch(recipe, excludedRecipe)) {
                            return;
                        }
                    }
                    recipes.add(recipe);
                }, recipeType.get());
            });
        }

        public CategoryBuilder<T> removeRecipes(Supplier<RecipeType<? extends T>> recipeType) {
            return addRecipeListConsumer(recipes -> {
                List<Recipe<?>> excludedRecipes = CreateJEI.getTypedRecipes(recipeType.get());
                recipes.removeIf(recipe -> {
                    for (Recipe<?> excludedRecipe : excludedRecipes) {
                        if (CreateJEI.doInputsMatch(recipe, excludedRecipe)) {
                            return true;
                        }
                    }
                    return false;
                });
            });
        }

        public CategoryBuilder<T> catalyst(Supplier<ItemLike> supplier) {
            return catalystStack(() -> new ItemStack(supplier.get()
                    .asItem()));
        }

        public CategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
            catalysts.add(supplier);
            return this;
        }

        public CategoryBuilder<T> itemIcon(ItemLike item) {
            icon(new ItemIcon(() -> new ItemStack(item)));
            return this;
        }

        public CategoryBuilder<T> icon(IDrawable icon) {
            this.icon = icon;
            return this;
        }

        public CategoryBuilder<T> doubleItemIcon(ItemLike item1, ItemLike item2) {
            icon(new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2)));
            return this;
        }

        public CategoryBuilder<T> emptyBackground(int width, int height) {
            background(new EmptyBackground(width, height));
            return this;
        }

        public CategoryBuilder<T> background(IDrawable background) {
            this.background = background;
            return this;
        }

        public CreateRecipeCategory<T> build(String name, CreateRecipeCategory.Factory<T> factory) {
            Supplier<List<T>> recipesSupplier;
            if (predicate.test(AllConfigs.server().recipes)) {
                recipesSupplier = () -> {
                    List<T> recipes = new ArrayList<>();
                    for (Consumer<List<T>> consumer : recipeListConsumers)
                        consumer.accept(recipes);
                    return recipes;
                };
            } else {
                recipesSupplier = () -> Collections.emptyList();
            }

            CreateRecipeCategory.Info<T> info = new CreateRecipeCategory.Info(new mezz.jei.api.recipe.RecipeType(Create.asResource(name), this.recipeClass), Lang.translateDirect("recipe." + name, new Object[0]), this.background, this.icon, recipesSupplier, this.catalysts);
            CreateRecipeCategory<T> category = factory.create(info);
            allCategories.add(category);
            return category;
        }
    }
}
