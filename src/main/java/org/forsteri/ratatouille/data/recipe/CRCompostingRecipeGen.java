package org.forsteri.ratatouille.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraftforge.common.Tags;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.data.recipe.api.BakingRecipeGen;
import org.forsteri.ratatouille.data.recipe.api.CompostingRecipeGen;
import org.forsteri.ratatouille.entry.CRFluids;
import org.forsteri.ratatouille.entry.CRItems;
import org.forsteri.ratatouille.entry.CRRecipeTypes;

public class CRCompostingRecipeGen extends CompostingRecipeGen {
    GeneratedRecipe
            DEFAULT = this.create("composting", b -> b
            .require(CRItems.COMPOST_MASS.get())
            .duration(200)
            //.output(CRItems.COMPOST_RESIDUE.get(), 1)
            .output(CRFluids.COMPOST_RESIDUE_FLUID.get(), 60)
            .output(CRFluids.COMPOST_TEA.get(), 30)
            .output(CRFluids.BIO_GAS.get(), 10)),

            DOUGH = this.create("more_dough", b -> b
                    .require(AllItems.DOUGH.get())
                    .require(AllItems.WHEAT_FLOUR.get())
                    .require(Fluids.WATER, 100)
                    .duration(200)
                    .output(AllItems.DOUGH.get(),3)),

            TEST1 = this.create("test1", b -> b
                    .require(Fluids.WATER,50)
                    .require(Tags.Fluids.MILK, 50)
                    .require(Fluids.LAVA, 50)
                    .duration(200)
                    .output(AllFluids.HONEY.get(),150)),

            TEST2 = this.create("test2", b -> b
                    .require(Fluids.WATER,50)
                    .require(Tags.Fluids.MILK, 50)
                    .require(Fluids.LAVA, 50)
                    .duration(200)
                    .output(CRFluids.COMPOST_RESIDUE_FLUID.get(), 60)
                    .output(CRFluids.COMPOST_TEA.get(), 30)
                    .output(CRFluids.BIO_GAS.get(), 10))
    ;

    public CRCompostingRecipeGen(PackOutput generator) {
        super(generator, Ratatouille.MOD_ID);
    }
}
