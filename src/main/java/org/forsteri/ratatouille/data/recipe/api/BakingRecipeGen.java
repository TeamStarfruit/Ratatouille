package org.forsteri.ratatouille.data.recipe.api;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import net.minecraft.data.PackOutput;
import org.forsteri.ratatouille.entry.CRRecipeTypes;

public abstract class BakingRecipeGen extends ProcessingRecipeGen {
    public BakingRecipeGen(PackOutput generator, String defaultNamespace) {
        super(generator, defaultNamespace);
    }

    @Override
    protected CRRecipeTypes getRecipeType() {
        return CRRecipeTypes.BAKING;
    }

}