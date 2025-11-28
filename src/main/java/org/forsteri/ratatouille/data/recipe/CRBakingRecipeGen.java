package org.forsteri.ratatouille.data.recipe;

import net.minecraft.data.PackOutput;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.data.recipe.api.BakingRecipeGen;

public class CRBakingRecipeGen extends BakingRecipeGen {

//    GeneratedRecipe
//            RICE = this.create(
//            CRItems.BOIL_STONE::get,
//            b -> b.output(CRItems.MATURE_MATTER.get()).duration(200)
//    );

    public CRBakingRecipeGen(PackOutput generator) {
        super(generator, Ratatouille.MOD_ID);
    }

}