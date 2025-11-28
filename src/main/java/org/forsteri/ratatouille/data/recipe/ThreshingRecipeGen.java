package org.forsteri.ratatouille.data.recipe;
import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.entry.CRRecipeTypes;
import vectorwing.farmersdelight.common.registry.ModItems;

public class ThreshingRecipeGen extends ProcessingRecipeGen {

    GeneratedRecipe
            RICE = this.create(
            ModItems.RICE_PANICLE::get,
                b -> b.output(vectorwing.farmersdelight.common.registry.ModItems.RICE.get()).output(0.5F, vectorwing.farmersdelight.common.registry.ModItems.RICE.get()).duration(200).whenModLoaded("farmersdelight")
            );
//            CORN_KERNELS = this.create(
//                    () -> com.ncpbails.culturaldelights.item.ModItems.CORN_COB.get(),
//                    b -> b.output(com.ncpbails.culturaldelights.item.ModItems.CORN_KERNELS.get()).output(0.5F, com.ncpbails.culturaldelights.item.ModItems.CORN_KERNELS.get()).duration(200).whenModLoaded("culturaldelights")
//            );
    public ThreshingRecipeGen(PackOutput generator) {
        super(generator, Ratatouille.MOD_ID);
    }

    @Override
    protected CRRecipeTypes getRecipeType() {
        return CRRecipeTypes.THRESHING;
    }

}