package org.forsteri.ratatouille.data.recipe.api;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.data.PackOutput;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.entry.CRFluids;
import org.forsteri.ratatouille.entry.CRItems;
import org.forsteri.ratatouille.entry.CRRecipeTypes;

public abstract class CompostingRecipeGen extends ProcessingRecipeGen {
    public CompostingRecipeGen(PackOutput generator, String defaultNamespace) {
        super(generator, defaultNamespace);
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return CRRecipeTypes.COMPOSTING;
    }
}
