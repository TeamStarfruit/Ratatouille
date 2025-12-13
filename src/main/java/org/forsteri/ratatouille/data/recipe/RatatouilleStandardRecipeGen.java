package org.forsteri.ratatouille.data.recipe;

import com.google.common.base.Supplier;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.BaseRecipeProvider;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import org.checkerframework.checker.units.qual.A;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.entry.CRBlocks;
import org.forsteri.ratatouille.entry.CRFluids;
import org.forsteri.ratatouille.entry.CRItems;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.CommonTags;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public final class RatatouilleStandardRecipeGen extends BaseRecipeProvider {
    final List<GeneratedRecipe> all = new ArrayList<>();

    GeneratedRecipe
            CHEF_HAT = create(CRItems.CHEF_HAT)
            .unlockedBy(Items.WHITE_WOOL::asItem) // mandatory
            .viaShaped(b -> b
                    .pattern(" Y ")
                    .pattern(" X ")
                    .define('Y', Items.WHITE_WOOL)
                    .define('X', CommonTags.TOOLS_KNIFE)
            ),
            CHEF_HAT_WITH_GOGGLES = create(CRItems.CHEF_HAT_WITH_GOGGLES)
                    .unlockedBy(CRItems.CHEF_HAT::get)
                    .viaShapeless(b -> b
                            .requires(CRItems.CHEF_HAT.get())
                            .requires(com.simibubi.create.AllItems.GOGGLES.get())
                    ),
            COMPOST_TEA_BOTTLE = create(CRItems.COMPOST_TEA_BOTTLE)
                    .unlockedBy(CRFluids.COMPOST_TEA.getBucket()::get)
                    .viaShapeless(b -> b
                            .requires(CRFluids.COMPOST_TEA.getBucket().get())
                            .requires(Items.GLASS_BOTTLE, 8)
                    ),
            COMPOST_TOWER = create(CRBlocks.COMPOST_TOWER_BLOCK)
                    .unlockedBy(Items.BARREL::asItem) // mandatory
                    .viaShaped(b -> b
                            .pattern(" Y ")
                            .pattern(" X ")
                            .pattern(" Y ")
                            .define('Y', AllItems.ZINC_INGOT.get())
                            .define('X', Items.BARREL)
                    ),
            EGG_SHELL_TO_BONE_MEAL = create(Items.BONE_MEAL::asItem)
                    .unlockedBy(Items.EGG::asItem)
                    .viaShapeless(b -> b
                            .requires(CRItems.EGG_SHELL.get())
                    ),
            FROZEN_BLOCK = create(CRBlocks.FROZEN_BLOCK)
                    .unlockedBy(Items.BLUE_ICE::asItem) // mandatory
                    .viaShaped(b -> b
                            .pattern(" Y ")
                            .pattern("YXY")
                            .pattern(" Y ")
                            .define('Y', Items.BLUE_ICE)
                            .define('X', Items.POWDER_SNOW_BUCKET)
                    ),
            IRRIGATION_TOWER = create(CRBlocks.IRRIGATION_TOWER_BLOCK)
                    .unlockedBy(AllBlocks.FLUID_TANK::asItem) // mandatory
                    .viaShaped(b -> b
                            .pattern(" Z ")
                            .pattern("YXY")
                            .define('X', AllBlocks.FLUID_PIPE)
                            .define('Y', AllItems.COPPER_SHEET)
                            .define('Z', AllBlocks.FLUID_TANK)
                    ),
            MECHANICAL_DEMOLDER = create(CRBlocks.MECHANICAL_DEMOLDER)
                    .unlockedBy(AllBlocks.ANDESITE_CASING::asItem) // mandatory
                    .viaShaped(b -> b
                            .pattern(" Z ")
                            .pattern(" Y ")
                            .pattern(" X ")
                            .define('Z', AllItems.ANDESITE_ALLOY)
                            .define('Y', AllBlocks.ANDESITE_CASING)
                            .define('X', Items.SLIME_BALL)
                    ),
            ORGANIC_COMPOST = create(ModItems.ORGANIC_COMPOST::get)
                    .unlockedBy(CRItems.COMPOST_RESIDUE::asItem) // mandatory
                    .viaShaped(b -> b
                            .pattern(" Y ")
                            .pattern("YXY")
                            .pattern(" Y ")
                            .define('Y', CRItems.COMPOST_RESIDUE)
                            .define('X', Items.DIRT)
                    ),
            OVEN = create(CRBlocks.OVEN)
                    .unlockedBy(AllItems.ANDESITE_ALLOY::asItem) // mandatory
                    .viaShaped(b -> b
                            .pattern(" Y ")
                            .pattern(" X ")
                            .pattern(" Y ")
                            .define('Y', AllItems.ANDESITE_ALLOY)
                            .define('X', Items.BARREL)
                    ),
            OVEN_FAN = create(CRBlocks.OVEN_FAN)
                    .unlockedBy(AllBlocks.ANDESITE_CASING::asItem) // mandatory
                    .viaShaped(b -> b
                            .pattern(" Y ")
                            .pattern(" X ")
                            .pattern(" Z ")
                            .define('Y', AllBlocks.COGWHEEL)
                            .define('X', AllBlocks.ANDESITE_CASING)
                            .define('Z', AllItems.PROPELLER)
                    ),
            SPREADER = create(CRBlocks.SPREADER_BLOCK)
                    .unlockedBy(AllItems.ANDESITE_ALLOY::asItem) // mandatory
                    .viaShaped(b -> b
                            .pattern(" X ")
                            .pattern("ZSZ")
                            .pattern(" Y ")
                            .define('Y', AllItems.PROPELLER)
                            .define('S', AllBlocks.ANDESITE_CASING)
                            .define('Z', AllItems.TREE_FERTILIZER)
                            .define('X', AllBlocks.COGWHEEL)
                    ),
            SQUEEZE_BASIN = create(CRBlocks.SQUEEZE_BASIN)
                    .unlockedBy(Items.COPPER_INGOT::asItem) // mandatory
                    .viaShaped(b -> b
                            .pattern(" Z ")
                            .pattern("Y Y")
                            .pattern("YYY")
                            .define('Y', Items.COPPER_INGOT)
                            .define('Z', AllItems.COPPER_SHEET)
                    ),
            THRESHER = create(CRBlocks.THRESHER)
                    .unlockedBy(AllItems.ANDESITE_ALLOY::asItem) // mandatory
                    .viaShaped(b -> b
                            .pattern(" S ")
                            .pattern("ZXZ")
                            .pattern(" Y ")
                            .define('Y', AllBlocks.ANDESITE_CASING)
                            .define('Z', AllBlocks.SHAFT)
                            .define('S', AllItems.ANDESITE_ALLOY)
                            .define('X', AllBlocks.MECHANICAL_HARVESTER)
                    );
    GeneratedRecipe
            CAKE_MOLD_BAKED = create(CRItems.CAKE_MOLD_BAKED::get)
            .viaCooking(CRItems.CAKE_MOLD_FILLED::get)
            .forDuration(200)
            .inSmoker(),

    ROAST_COCO = create(CRItems.DRIED_COCOA_BEANS::get)
            .viaCooking(() -> Items.COCOA_BEANS)
            .forDuration(20)
            .inSmoker(),

    SAUSAGE = create(CRItems.SAUSAGE::get)
            .viaCooking(CRItems.RAW_SAUSAGE::get)
            .forDuration(100)
            .inSmoker();

    public RatatouilleStandardRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Ratatouille.MOD_ID);
    }

    GeneratedRecipeBuilder create(ItemProviderEntry<? extends ItemLike, ? extends ItemLike> result) {
        return create(result::get);
    }

    GeneratedRecipeBuilder create(Supplier<ItemLike> result) {
        return new GeneratedRecipeBuilder(result);
    }

    @Override
    public @NotNull String getName() {
        return "Create: Ratatouille's Standard Recipes";
    }

    class GeneratedRecipeBuilder {

        List<ICondition> recipeConditions;
        private String suffix;
        private Supplier<? extends ItemLike> result;
        private ResourceLocation compatDatagenOutput;
        private Supplier<ItemPredicate> unlockedBy;
        private int amount;

        public GeneratedRecipeBuilder(Supplier<? extends ItemLike> result) {
            this();
            this.result = result;
        }

        private GeneratedRecipeBuilder() {
            this.recipeConditions = new ArrayList<>();
            this.suffix = "";
            this.amount = 1;
        }

        public GeneratedRecipeBuilder(ResourceLocation result) {
            this();
            this.compatDatagenOutput = result;
        }

        GeneratedRecipeBuilder returns(int amount) {
            this.amount = amount;
            return this;
        }

        GeneratedRecipeBuilder whenModLoaded(String modid) {
            return withCondition(new ModLoadedCondition(modid));
        }

        GeneratedRecipeBuilder withCondition(ICondition condition) {
            recipeConditions.add(condition);
            return this;
        }

        GeneratedRecipeBuilder whenModMissing(String modid) {
            return withCondition(new NotCondition(new ModLoadedCondition(modid)));
        }

        GeneratedRecipeBuilder withSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        GeneratedRecipe viaShaped(UnaryOperator<ShapedRecipeBuilder> builder) {
            return register(consumer -> {
                ShapedRecipeBuilder b =
                        builder.apply(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result.get(), amount));
                if (unlockedBy != null)
                    b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                b.save(consumer, createLocation("crafting"));
            });
        }

        private ResourceLocation createLocation(String recipeType) {
            return Ratatouille.asResource(recipeType + "/" + getRegistryName().getPath() + suffix);
        }

        private ResourceLocation getRegistryName() {
            return compatDatagenOutput == null ? RegisteredObjectsHelper.getKeyOrThrow(result.get()
                    .asItem()) : compatDatagenOutput;
        }

        GeneratedRecipe viaShapeless(UnaryOperator<ShapelessRecipeBuilder> builder) {
            return register(recipeOutput -> {
                ShapelessRecipeBuilder b =
                        builder.apply(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result.get(), amount));
                if (unlockedBy != null)
                    b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));

                RecipeOutput conditionalOutput = recipeOutput.withConditions(recipeConditions.toArray(new ICondition[0]));

                b.save(conditionalOutput, createLocation("crafting"));
            });
        }

        GeneratedRecipe viaNetheriteSmithing(Supplier<? extends Item> base, Supplier<Ingredient> upgradeMaterial) {
            return register(consumer -> {
                SmithingTransformRecipeBuilder b =
                        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                                Ingredient.of(base.get()), upgradeMaterial.get(), RecipeCategory.COMBAT, result.get()
                                        .asItem());
                b.unlocks("has_item", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(base.get())
                        .build()));
                b.save(consumer, createLocation("crafting"));
            });
        }

        private ResourceLocation createSimpleLocation(String recipeType) {
            return Ratatouille.asResource(recipeType + "/" + getRegistryName().getPath() + suffix);
        }

        GeneratedRecipeBuilder.GeneratedCookingRecipeBuilder viaCooking(Supplier<? extends ItemLike> item) {
            return unlockedBy(item).viaCookingIngredient(() -> Ingredient.of(item.get()));
        }

        GeneratedRecipeBuilder.GeneratedCookingRecipeBuilder viaCookingIngredient(Supplier<Ingredient> ingredient) {
            return new GeneratedRecipeBuilder.GeneratedCookingRecipeBuilder(ingredient);
        }

        GeneratedRecipeBuilder unlockedBy(Supplier<? extends ItemLike> item) {
            this.unlockedBy = () -> ItemPredicate.Builder.item()
                    .of(item.get())
                    .build();
            return this;
        }

        GeneratedRecipeBuilder.GeneratedCookingRecipeBuilder viaCookingTag(Supplier<TagKey<Item>> tag) {
            return unlockedByTag(tag).viaCookingIngredient(() -> Ingredient.of(tag.get()));
        }

        GeneratedRecipeBuilder unlockedByTag(Supplier<TagKey<Item>> tag) {
            this.unlockedBy = () -> ItemPredicate.Builder.item()
                    .of(tag.get())
                    .build();
            return this;
        }

        class GeneratedCookingRecipeBuilder {

            private Supplier<Ingredient> ingredient;
            private float exp;
            private int cookingTime;

            GeneratedCookingRecipeBuilder(Supplier<Ingredient> ingredient) {
                this.ingredient = ingredient;
                cookingTime = 200;
                exp = 0;
            }

            GeneratedRecipeBuilder.GeneratedCookingRecipeBuilder forDuration(int duration) {
                cookingTime = duration;
                return this;
            }

            GeneratedRecipeBuilder.GeneratedCookingRecipeBuilder rewardXP(float xp) {
                exp = xp;
                return this;
            }

            GeneratedRecipe inFurnace() {
                return inFurnace(b -> b);
            }

            GeneratedRecipe inFurnace(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
                return create(RecipeSerializer.SMELTING_RECIPE, builder, SmeltingRecipe::new, 1);
            }

            private <T extends AbstractCookingRecipe> GeneratedRecipe create(RecipeSerializer<T> serializer,
                                                                             UnaryOperator<SimpleCookingRecipeBuilder> builder, AbstractCookingRecipe.Factory<T> factory, float cookingTimeModifier) {
                return register(recipeOutput -> {
                    boolean isOtherMod = compatDatagenOutput != null;

                    SimpleCookingRecipeBuilder b = builder.apply(SimpleCookingRecipeBuilder.generic(ingredient.get(),
                            RecipeCategory.MISC, isOtherMod ? Items.DIRT : result.get(), exp,
                            (int) (cookingTime * cookingTimeModifier), serializer, factory));
                    if (unlockedBy != null)
                        b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));

                    RecipeOutput conditionalOutput = recipeOutput.withConditions(recipeConditions.toArray(new ICondition[0]));

                    b.save(
//                            isOtherMod ? new ModdedCookingRecipeOutput(conditionalOutput, compatDatagenOutput) : conditionalOutput,
                            conditionalOutput,
                            createSimpleLocation(RegisteredObjectsHelper.getKeyOrThrow(serializer).getPath())
                    );
                });
            }

            GeneratedRecipe inSmoker() {
                return inSmoker(b -> b);
            }

            GeneratedRecipe inSmoker(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
                create(RecipeSerializer.SMELTING_RECIPE, builder, SmeltingRecipe::new, 1);
                create(RecipeSerializer.CAMPFIRE_COOKING_RECIPE, builder, CampfireCookingRecipe::new, 3);
                return create(RecipeSerializer.SMOKING_RECIPE, builder, SmokingRecipe::new, .5f);
            }

            GeneratedRecipe inBlastFurnace() {
                return inBlastFurnace(b -> b);
            }

            GeneratedRecipe inBlastFurnace(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
                create(RecipeSerializer.SMELTING_RECIPE, builder, SmeltingRecipe::new, 1);
                return create(RecipeSerializer.BLASTING_RECIPE, builder, BlastingRecipe::new, .5f);
            }
        }
    }
}
