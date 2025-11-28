package org.forsteri.ratatouille.data.recipe.api;
import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import net.minecraft.data.PackOutput;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.entry.CRRecipeTypes;
import vectorwing.farmersdelight.common.registry.ModItems;

public abstract class ThreshingRecipeGen extends ProcessingRecipeGen {
    public ThreshingRecipeGen(PackOutput generator, String defaultNamespace) {
        super(generator, defaultNamespace);
    }

    @Override
    protected CRRecipeTypes getRecipeType() {
        return CRRecipeTypes.THRESHING;
    }

}