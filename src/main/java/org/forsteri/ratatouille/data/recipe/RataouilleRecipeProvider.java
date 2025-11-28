package org.forsteri.ratatouille.data.recipe;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class RataouilleRecipeProvider extends RecipeProvider {
    protected static final List<ProcessingRecipeGen> GENERATORS = new ArrayList<>();
    protected static final int BUCKET = 1000;
    protected static final int BOTTLE = 250;

    public RataouilleRecipeProvider(PackOutput generator) {
        super(generator);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
    }

    public static void registerAllProcessing(DataGenerator gen, PackOutput output) {
        GENERATORS.add(new CRThreshingRecipeGen(output));
        GENERATORS.add(new CRSqueezingRecipeGen(output));
        GENERATORS.add(new CRDemoldingRecipeGen(output));
        GENERATORS.add(new CRFreezingRecipeGen(output));
        GENERATORS.add(new CRCompostingRecipeGen(output));
        GENERATORS.add(new CRBakingRecipeGen(output));

        gen.addProvider(true, new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput dc) {
                return CompletableFuture.allOf(GENERATORS.stream()
                        .map(gen -> gen.run(dc))
                        .toArray(CompletableFuture[]::new));
            }

            @Override
            public String getName() {
                return "Ratatouille's Processing Recipes";
            }
        });
    }
}
