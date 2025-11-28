package org.forsteri.ratatouille.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.entry.CRItems;
import org.forsteri.ratatouille.entry.CRRecipeTypes;
import vectorwing.farmersdelight.common.registry.ModItems;

public class DemoldingRecipeGen extends ProcessingRecipeGen {
    GeneratedRecipe
        BAR_OF_CHOCOLATE = this.create(
            CRItems.CHOCOLATE_MOLD_SOLID::get,
            b -> b.output(AllItems.BAR_OF_CHOCOLATE.get())
                    .output(CRItems.CHOCOLATE_MOLD.get())
        ),
        CAKE_BASE = this.create(
            CRItems.CAKE_MOLD_BAKED::get,
            b -> b.output(CRItems.CAKE_BASE.get())
                    .output(CRItems.CAKE_MOLD.get())
        ),

        MELON_POPSICLE = this.create(
            CRItems.MELON_POPSICLE_MOLD_SOLID::get,
            b -> b.output(ModItems.MELON_POPSICLE.get())
                    .output(CRItems.POPSICLE_MOLD.get())
            );
    public DemoldingRecipeGen(PackOutput generator) {
        super(generator, Ratatouille.MOD_ID);
    }

    @Override
    protected CRRecipeTypes getRecipeType() {
        return CRRecipeTypes.DEMOLDING;
    }
}
