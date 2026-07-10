package org.forsteri.ratatouille.data.recipe;

import com.simibubi.create.api.data.recipe.StandardProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezingRecipe;
import org.forsteri.ratatouille.entry.CRFluids;
import org.forsteri.ratatouille.entry.CRItems;
import org.forsteri.ratatouille.entry.CRRecipeTypes;
import org.forsteri.ratatouille.entry.CRTags;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class SqueezingRecipeGen extends StandardProcessingRecipeGen<SqueezingRecipe> {
    GeneratedRecipe
            RAW_SAUSAGE = this.create("raw_sausage", b -> b.require(CRFluids.MINCE_MEAT.get(), 250)
            .require(CRItems.SAUSAGE_CASING.get())
            .output(CRItems.RAW_SAUSAGE.get())),

    RAW_PASTA = this.create("raw_pasta", b -> b.require(CRItems.SALTY_DOUGH.get())
            .output(ModItems.RAW_PASTA.get())),

    COMPOST_1TO1 = this.create("compost_1to1", b -> b
            .require(CRTags.COMPOSTABLE_ITEMS_1to1)
            .output(CRItems.COMPOST_MASS.get(), 1)
            .duration(200)
    ),

    COMPOST_1TO4 = this.create("compost_1to4", b -> b
            .require(CRTags.COMPOSTABLE_ITEMS_1to4)
            .output(CRItems.COMPOST_MASS.get(), 4)
            .duration(200)
    ),

    COMPOST_FROM_MELON = this.create("compost_from_melon", b -> b
            .require(Items.MELON)
            .output(CRItems.COMPOST_MASS.get(), 9)
            .duration(200)
    ),

    COMPOST_2TO1 = this.create("compost_2to1", b -> b
            .require(CRTags.COMPOSTABLE_ITEMS_2to1)
            .output(0.5f, CRItems.COMPOST_MASS.get())
            .duration(200)
    ),

    COMPOST_4TO1 = this.create("compost_4to1", b -> b
            .require(CRTags.COMPOSTABLE_ITEMS_4to1)
            .output(0.25f, CRItems.COMPOST_MASS.get())
            .duration(200)
    );

    public SqueezingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Ratatouille.MOD_ID);
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return CRRecipeTypes.SQUEEZING;
    }
}
