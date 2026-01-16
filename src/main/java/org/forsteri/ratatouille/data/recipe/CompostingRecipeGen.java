package org.forsteri.ratatouille.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.StandardProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.content.compost_tower.CompostingRecipe;
import org.forsteri.ratatouille.entry.CRFluids;
import org.forsteri.ratatouille.entry.CRItems;
import org.forsteri.ratatouille.entry.CRRecipeTypes;

import java.util.concurrent.CompletableFuture;

public class CompostingRecipeGen extends StandardProcessingRecipeGen<CompostingRecipe> {
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

    LEATHER = this.create("leather", b -> b
            .require(Items.ROTTEN_FLESH)
            .require(CRItems.SALT.get())
            .require(Fluids.WATER, 50)
            .duration(500)
            .output(Items.LEATHER)),

    FERMENTED_SPIDER_EYE = this.create("fermented_spider_eye", b -> b
            .require(Items.SPIDER_EYE)
            .require(Items.SUGAR)
            .require(Fluids.WATER, 50)
            .duration(500)
            .output(Items.FERMENTED_SPIDER_EYE)),

    SLIME_BALL = this.create("slime_ball", b -> b
            .require(Items.KELP)
            .require(Items.KELP)
            .require(CRItems.SALT.get())
            .require(Fluids.WATER, 50)
            .duration(500)
            .output(Items.SLIME_BALL))
            ;
    public CompostingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Ratatouille.MOD_ID);
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return CRRecipeTypes.COMPOSTING;
    }
}
