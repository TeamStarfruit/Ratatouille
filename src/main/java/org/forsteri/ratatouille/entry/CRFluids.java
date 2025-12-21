package org.forsteri.ratatouille.entry;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.content.NoPlaceBucketItem;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class CRFluids {
    public static final FluidEntry<BaseFlowingFluid.Flowing> COCOA_LIQUOR =
            Ratatouille.REGISTRATE
            .standardFluid("cocoa_liquor", SolidRenderedPlaceableFluidType.create(6430752, () -> {return 0.1f;}))
            .lang("Cocoa Liquor")
            .register();


    public static final FluidEntry<BaseFlowingFluid.Flowing> CAKE_BATTER =
            Ratatouille.REGISTRATE
                    .standardFluid("cake_batter")
                    .lang("Cake Batter")
                    .source(BaseFlowingFluid.Source::new).block().build()
                    .bucket(NoPlaceBucketItem::new).build()
                    .register();

    public static final FluidEntry<BaseFlowingFluid.Flowing> MINCE_MEAT =
            Ratatouille.REGISTRATE
                    .standardFluid("mince_meat")
                    .lang("Mince Meat")
                    .source(BaseFlowingFluid.Source::new).block().build()
                    .bucket(NoPlaceBucketItem::new).build()
                    .register();

    public static final FluidEntry<BaseFlowingFluid.Flowing> EGG_YOLK =
            Ratatouille.REGISTRATE
                    .standardFluid("egg_yolk")
                    .lang("Egg Yolk")
                    .source(BaseFlowingFluid.Source::new).block().build()
                    .bucket(NoPlaceBucketItem::new).build()
                    .register();
    public static final FluidEntry<BaseFlowingFluid.Flowing> COMPOST_TEA =
            Ratatouille.REGISTRATE
                    .standardFluid("compost_tea")
                    .properties(p -> p
                            .density(1050))
                    .lang("Compost Tea")
                    .source(BaseFlowingFluid.Source::new).block().build()
                    .bucket().build().register();
    public static final FluidEntry<BaseFlowingFluid.Flowing> BIO_GAS =
            Ratatouille.REGISTRATE
                    .standardFluid("bio_gas")
                    .properties(p -> p
                            .density(-150))
                    .lang("Biogas")
                    .source(BaseFlowingFluid.Source::new).block().build()
                    .bucket(NoPlaceBucketItem::new).build().register();

    public static final FluidEntry<VirtualFluid> COMPOST_FLUID =
            Ratatouille.REGISTRATE
                    .virtualFluid("compost_fluid")
                    .properties(p -> p
                            .density(1400))
                    .lang("Compost Fluid")
                    .register();
    public static final FluidEntry<BaseFlowingFluid.Flowing> COMPOST_RESIDUE_FLUID =
            Ratatouille.REGISTRATE
                    .standardFluid("compost_residue_fluid")
                    .properties(p -> p
                            .density(1450))
                    .lang("Compost Residue Fluid")
                    .source(BaseFlowingFluid.Source::new).block().build()
                    .bucket().build().register();

    public static final FluidEntry<BaseFlowingFluid.Flowing> MELON_JUICE_FLUID =
            Ratatouille.REGISTRATE
                    .standardFluid("melon_juice_fluid")
                    .lang("Melon Juice")
                    .source(BaseFlowingFluid.Source::new).block().build()
                    .bucket(NoPlaceBucketItem::new)
                    .lang("Melon Juice Bucket")
                    .build()
                    .register();

    static {
        Ratatouille.REGISTRATE.setCreativeTab(CRCreativeModeTabs.BASE_CREATIVE_TAB);
    }

    public static void register() {
    }

    private static class SolidRenderedPlaceableFluidType extends AllFluids.TintedFluidType {

        private Vector3f fogColor;
        private Supplier<Float> fogDistance;

        private SolidRenderedPlaceableFluidType(Properties properties, ResourceLocation stillTexture,
                                                ResourceLocation flowingTexture) {
            super(properties, stillTexture, flowingTexture);
        }

        public static FluidBuilder.FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
            return (p, s, f) -> {
                SolidRenderedPlaceableFluidType fluidType = new SolidRenderedPlaceableFluidType(p, s, f);
                fluidType.fogColor = new Color(fogColor, false).asVectorF();
                fluidType.fogDistance = fogDistance;
                return fluidType;
            };
        }

        @Override
        protected int getTintColor(FluidStack stack) {
            return NO_TINT;
        }

        public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
            return 0x00ffffff;
        }

        @Override
        protected Vector3f getCustomFogColor() {
            return fogColor;
        }

        @Override
        protected float getFogDistanceModifier() {
            return fogDistance.get();
        }

    }
}
