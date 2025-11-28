package org.forsteri.ratatouille.data.recipe;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import net.minecraft.data.PackOutput;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.entry.CRRecipeTypes;

public class BakingRecipeGen extends ProcessingRecipeGen {

//    GeneratedRecipe
//            RICE = this.create(
//            CRItems.BOIL_STONE::get,
//            b -> b.output(CRItems.MATURE_MATTER.get()).duration(200)
//    );

    public BakingRecipeGen(PackOutput generator) {
        super(generator, Ratatouille.MOD_ID);
    }

    @Override
    protected CRRecipeTypes getRecipeType() {
        return CRRecipeTypes.BAKING;
    }

}