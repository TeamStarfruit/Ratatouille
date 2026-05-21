package org.forsteri.ratatouille.data.recipe;

import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import org.checkerframework.checker.units.qual.C;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.entry.CRFluids;
import org.forsteri.ratatouille.entry.CRItems;
import org.forsteri.ratatouille.entry.CRTags;
import vectorwing.farmersdelight.common.registry.ModItems;
import net.neoforged.neoforge.common.Tags;

public class RatatouilleRegistrateTags {
    public static void addGenerators() {
        Ratatouille.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, RatatouilleRegistrateTags::genBlockTags);
        Ratatouille.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, RatatouilleRegistrateTags::genItemTags);
        Ratatouille.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, RatatouilleRegistrateTags::genFluidTags);
        Ratatouille.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, RatatouilleRegistrateTags::genEntityTags);
    }

    private static void genBlockTags(RegistrateTagsProvider<Block> provIn) {

    }

    private static void genItemTags(RegistrateTagsProvider<Item> provIn) {
        TagGen.CreateTagsProvider<Item> prov = new TagGen.CreateTagsProvider<>(provIn, Item::builtInRegistryHolder);

       prov.tag(CRTags.SALTS)
                .add(CRItems.SALT.get());
        prov.tag(CRTags.MOLD)
                .add(
                        CRItems.CAKE_MOLD.get(),
                        CRItems.CHOCOLATE_MOLD.get(),
                        CRItems.POPSICLE_MOLD.get()
                );
        prov.tag(CRTags.RAW_MEAT)
                .add(
                        Items.BEEF,
                        Items.PORKCHOP,
                        Items.CHICKEN,
                        Items.MUTTON,
                        Items.RABBIT
                );
        prov.tag(CRTags.EGGS)
                .add(
                        Items.EGG,
                        Items.TURTLE_EGG,
                        Items.SNIFFER_EGG,
                        Items.FROGSPAWN,
                        ModItems.FRIED_EGG.get(),
                        CRItems.EGG_SHELL.get()
                );
        prov.tag(CRTags.COMPOSTABLE_ITEMS_1to1)
                .add(
                        Items.SUGAR_CANE,
                        ModItems.STRAW.get(),
                        ModItems.TREE_BARK.get(),
                        Items.KELP,
                        Items.DRIED_KELP,
                        Items.SEAGRASS,
                        Items.VINE,
                        Items.TWISTING_VINES,
                        Items.WEEPING_VINES,
                        Items.MELON_SLICE,
                        ModItems.PUMPKIN_SLICE.get(),
                        Items.CRIMSON_FUNGUS,
                        Items.WARPED_FUNGUS,
                        Items.LILY_PAD,
                        Items.BIG_DRIPLEAF,
                        Items.SEA_PICKLE,
                        Items.APPLE,
                        Items.ROTTEN_FLESH,
                        CRItems.SAUSAGE.get(),
                        CRItems.RAW_SAUSAGE.get(),
                        CRItems.DRIED_COCOA_BEANS.get(),
                        CRItems.CAKE_BASE.get()
                )
                .addTag(Tags.Items.CROPS)
                .addTag(Tags.Items.FOODS_BERRY)
                .addTag(CRTags.EGGS)
                .addTag(CRTags.RAW_MEAT)
                .addTag(ItemTags.FISHES)
                .addTag(Tags.Items.FOODS_RAW_FISH)
                .addTag(Tags.Items.FOODS_COOKED_MEAT);

        prov.tag(CRTags.COMPOSTABLE_ITEMS_1to4)
                .add(
                        Blocks.MOSS_BLOCK.asItem(),
                        Blocks.MUSHROOM_STEM.asItem(),
                        Blocks.BROWN_MUSHROOM_BLOCK.asItem(),
                        Blocks.RED_MUSHROOM_BLOCK.asItem(),
                        Blocks.SHROOMLIGHT.asItem(),
                        Blocks.CACTUS.asItem(),
                        Blocks.PUMPKIN.asItem(),
                        Blocks.CARVED_PUMPKIN.asItem()
                );


        prov.tag(CRTags.COMPOSTABLE_ITEMS_2to1)
                .addTag(ItemTags.SAPLINGS)
                .addTag(ItemTags.FLOWERS)
                .addTag(Tags.Items.MUSHROOMS);


        prov.tag(CRTags.COMPOSTABLE_ITEMS_4to1)
                .add(CRItems.DRIED_COCOA_NIBS.get())
                .add(CRItems.WHEAT_KERNELS.get())
                .add(CRItems.EGG_SHELL.get())
                .addTag(ItemTags.LEAVES)
                .addTag(Tags.Items.SEEDS);
    }

    private static void genFluidTags(RegistrateTagsProvider<Fluid> provIn) {
        TagGen.CreateTagsProvider<Fluid> prov = new TagGen.CreateTagsProvider<>(provIn, Fluid::builtInRegistryHolder);

        prov.tag(CRTags.bio_gas)
                .add(CRFluids.BIO_GAS.get());
    }

    private static void genEntityTags(RegistrateTagsProvider<EntityType<?>> provIn) {

    }
}
