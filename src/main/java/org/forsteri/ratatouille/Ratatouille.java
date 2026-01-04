package org.forsteri.ratatouille;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.forsteri.ratatouille.data.recipe.RatatouilleDataGen;
import org.forsteri.ratatouille.entry.*;

@Mod(Ratatouille.MOD_ID)
public class Ratatouille {
    public static final String MOD_ID = "ratatouille";

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(Ratatouille.MOD_ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    public Ratatouille(IEventBus modEventBus, ModContainer modContainer) {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        REGISTRATE.registerEventListeners(modEventBus);

        CRCreativeModeTabs.register(modEventBus);

        CRTags.init();
        CRBlocks.register();
        CRItems.register();
        CRFluids.register();
        CRBlockEntityTypes.register();

        CRRecipeTypes.register(modEventBus);
        CRParticleTypes.register(modEventBus);
        CRDataComponents.register(modEventBus);

        CRConfigs.register(modLoadingContext, modContainer);

        modEventBus.addListener(this::init);
        modEventBus.addListener(EventPriority.HIGHEST, RatatouilleDataGen::gatherDataHighPriority);
        modEventBus.addListener(EventPriority.LOWEST, RatatouilleDataGen::gatherData);
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> onClient(modEventBus, modContainer));
    }


    public static void onClient(IEventBus modEventBus, ModContainer container) {
        CRPartialModels.init();
        modEventBus.addListener(Ratatouille::clientInit);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CRPonderPlugin());
    }

    private void init(final FMLCommonSetupEvent event) {

    }

    public static LangBuilder lang() {
        return new LangBuilder(MOD_ID);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
