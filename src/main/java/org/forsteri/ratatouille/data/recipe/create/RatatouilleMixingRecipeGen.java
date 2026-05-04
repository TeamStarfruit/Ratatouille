package org.forsteri.ratatouille.data.recipe.create;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.entry.CRFluids;
import org.forsteri.ratatouille.entry.CRItems;
import org.forsteri.ratatouille.entry.CRTags;
import vectorwing.farmersdelight.common.registry.ModItems;
import org.forsteri.ratatouille.entry.CRTags;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class RatatouilleMixingRecipeGen extends MixingRecipeGen {

    GeneratedRecipe
            CAKE_BATTER = create("cake_batter", b -> b
            .require(com.simibubi.create.AllItems.WHEAT_FLOUR.get())
            .require(com.simibubi.create.AllItems.WHEAT_FLOUR.get())
            .require(Items.SUGAR)
            .require(CRFluids.EGG_YOLK.get(), 250)
            .require(Tags.Fluids.MILK, 250)
            .output(CRFluids.CAKE_BATTER.get(), 1000)
    ),

    COCOA_PRESS = create("cocoa_press", b -> b
            .require(CRFluids.COCOA_LIQUOR.get(), 250)
            .output(CRItems.COCOA_SOLIDS.get())
            .output(0.75f, CRItems.COCOA_BUTTER.get(), 1)
    ),

    NIBS_TO_LIQUOR = create("nibs_to_liquor", b -> b
            .require(CRItems.DRIED_COCOA_NIBS.get())
            .output(CRFluids.COCOA_LIQUOR.get(), 250)
            .requiresHeat(HeatCondition.HEATED)
    ),

    RESIDUE_SOLIDIFY = create("residue_solidify", b -> b
            .require(CRFluids.COMPOST_RESIDUE_FLUID.get(), 200)
            .output(CRItems.COMPOST_RESIDUE.get())
            .requiresHeat(HeatCondition.HEATED)
    ),

    MINCE_MEAT = create("mince_meat", b -> b
            .require(CRTags.RAW_MEAT)
            .require(CRTags.SALTS)
            .output(CRFluids.MINCE_MEAT.get(), 250)
    ),

    CHOCOLATE = create("chocolate", b -> b
            .require(CRItems.COCOA_SOLIDS.get())
            .require(CRItems.COCOA_BUTTER.get())
            .require(Items.SUGAR)
            .output(com.simibubi.create.AllFluids.CHOCOLATE.get(), 250)
            .requiresHeat(HeatCondition.HEATED)
    ),

    ORGANIC_COMPOST = create("organic_compost", b -> b
            .require(CRItems.COMPOST_RESIDUE.get())
            .require(CRItems.COMPOST_RESIDUE.get())
            .require(CRItems.COMPOST_RESIDUE.get())
            .require(CRItems.COMPOST_RESIDUE.get())
            .require(Items.DIRT)
            .output(ModItems.ORGANIC_COMPOST.get())
    ),

    RESIDUE_TO_PULP = create("residue_to_pulp", b -> b
            .require(CRItems.COMPOST_RESIDUE.get())
            .require(CRTags.SALTS)
            .require(Fluids.WATER, 250)
            .output(com.simibubi.create.AllItems.PULP.get())
            .requiresHeat(HeatCondition.HEATED)
    ),

    SALT_FROM_BOIL = create("salt_from_boil", b -> b
            .require(Fluids.WATER, 250)
            .require(CRItems.BOIL_STONE.get())
            .output(CRItems.SALT.get())
            .output(CRItems.BOIL_STONE.get())
            .requiresHeat(HeatCondition.HEATED)
    ),

    SALTY_DOUGH = create("salty_dough", b -> b
            .require(com.simibubi.create.AllItems.WHEAT_FLOUR.get())
            .require(CRTags.SALTS)
            .require(CRFluids.EGG_YOLK.get(), 100)
            .output(CRItems.SALTY_DOUGH.get())
    ),
    MELON_JUICE = create("melon_juice", b -> b
            .require(Items.MELON_SLICE)
            .require(Items.MELON_SLICE)
            .require(Items.MELON_SLICE)
            .require(Items.MELON_SLICE)
            .require(Items.SUGAR)
            .output(CRFluids.MELON_JUICE_FLUID.get(), 500)
            );

    public RatatouilleMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Ratatouille.MOD_ID);
    }
}
