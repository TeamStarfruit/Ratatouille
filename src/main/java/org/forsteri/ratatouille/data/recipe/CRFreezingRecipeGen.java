package org.forsteri.ratatouille.data.recipe;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.data.PackOutput;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.data.recipe.api.FreezingRecipeGen;
import org.forsteri.ratatouille.entry.CRItems;
import org.forsteri.ratatouille.entry.CRRecipeTypes;

public class CRFreezingRecipeGen extends FreezingRecipeGen {
    GeneratedRecipe
            CHOCOLATE_MOLD_SOLID  = this.create(
            CRItems.CHOCOLATE_MOLD_FILLED::get,
            b -> b.output(CRItems.CHOCOLATE_MOLD_SOLID.get())
    );
    public CRFreezingRecipeGen(PackOutput generator) {
        super(generator, Ratatouille.MOD_ID);
    }
}
