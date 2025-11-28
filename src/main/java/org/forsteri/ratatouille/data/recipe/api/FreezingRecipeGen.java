package org.forsteri.ratatouille.data.recipe.api;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.data.PackOutput;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.entry.CRRecipeTypes;

public abstract class FreezingRecipeGen extends ProcessingRecipeGen {
    public FreezingRecipeGen(PackOutput generator, String defaultNamespace) {
        super(generator, defaultNamespace);
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return CRRecipeTypes.FREEZING;
    }
}
