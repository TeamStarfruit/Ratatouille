package org.forsteri.ratatouille.entry;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.*;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.content.aerator.AeratorBlock;
import org.forsteri.ratatouille.content.compost_tower.CompostTowerBlock;
import org.forsteri.ratatouille.content.compost_tower.CompostTowerModel;
import org.forsteri.ratatouille.content.compost_tower.CompostTowerBlockItem;
import org.forsteri.ratatouille.content.demolder.MechanicalDemolderBlock;
import org.forsteri.ratatouille.content.fishpond.FishingNetBlock;
import org.forsteri.ratatouille.content.fishpond.FishpondFluidInterfaceBlock;
import org.forsteri.ratatouille.content.fishpond.FishpondWallBlock;
import org.forsteri.ratatouille.content.fishpond.SludgeBlock;
import org.forsteri.ratatouille.content.frozen_block.FrozenBlock;
import org.forsteri.ratatouille.content.irrigation_tower.IrrigationTowerBlock;
import org.forsteri.ratatouille.content.oven.*;
import org.forsteri.ratatouille.content.oven_fan.OvenFanBlock;
import org.forsteri.ratatouille.content.spreader.SpreaderBlock;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinBlock;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinGenerator;
import org.forsteri.ratatouille.content.thresher.ThresherBlock;

import static com.simibubi.create.foundation.data.BlockStateGen.simpleCubeAll;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class CRBlocks {

    static {
        Ratatouille.REGISTRATE.setCreativeTab(CRCreativeModeTabs.BASE_CREATIVE_TAB);
    }
    @SuppressWarnings("removal")
    public static final BlockEntry<OvenBlock> OVEN = Ratatouille.REGISTRATE
            .block("oven", OvenBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.lightLevel(state -> 1).isRedstoneConductor((p1, p2, p3) -> true).noOcclusion())
            .transform(pickaxeOnly())
            .blockstate(new OvenModel.OvenGenerator()::generate)
            .onRegister(CreateRegistrate.blockModel(() -> OvenModel::standard))
            .addLayer(() -> RenderType::cutoutMipped)
            .item(OvenBlockItem::new)
            .build()
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<ThresherBlock> THRESHER = Ratatouille.REGISTRATE
            .block("thresher", ThresherBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.lightLevel(state -> 1).noOcclusion().isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            //.transform(CRStress.setImpact(4.0f))
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p), 270))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(CRStress.setImpact((double)4.0F))
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), new ResourceLocation(Ratatouille.MOD_ID, "block/thresher/item")))
            .build()
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<OvenFanBlock> OVEN_FAN = Ratatouille.REGISTRATE
            .block("oven_fan", OvenFanBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.STONE))
            .transform(pickaxeOnly())
            //.transform(CRStress.setImpact(2.0f))
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(CRStress.setImpact((double)2.0F))
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), new ResourceLocation(Ratatouille.MOD_ID, "block/oven_fan/item")))
            .build()
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<SqueezeBasinBlock> SQUEEZE_BASIN = Ratatouille.REGISTRATE
            .block("squeeze_basin", SqueezeBasinBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.STONE))
            .transform(pickaxeOnly())
            .blockstate(new SqueezeBasinGenerator()::generate)
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), new ResourceLocation(Ratatouille.MOD_ID, "block/squeeze_basin/item")))
            .build()
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<MechanicalDemolderBlock> MECHANICAL_DEMOLDER = Ratatouille.REGISTRATE
            .block("mechanical_demolder", MechanicalDemolderBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(pickaxeOnly())
            .transform(CRStress.setImpact(8.0))
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .addLayer(() -> RenderType::cutoutMipped)
            .item(AssemblyOperatorBlockItem::new)
            .transform(customItemModel())
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<IrrigationTowerBlock> IRRIGATION_TOWER_BLOCK = Ratatouille.REGISTRATE
            .block("irrigation_tower", IrrigationTowerBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.lightLevel(state -> 1).noOcclusion().isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), new ResourceLocation(Ratatouille.MOD_ID, "block/irrigation_tower/item")))
            .build()
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<SpreaderBlock> SPREADER_BLOCK = Ratatouille.REGISTRATE
            .block("spreader", SpreaderBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(pickaxeOnly())
            .transform(CRStress.setImpact(2.0))
            .blockstate(BlockStateGen.directionalBlockProvider(true))
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), new ResourceLocation(Ratatouille.MOD_ID, "block/spreader/item")))
            .build()
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<FrozenBlock> FROZEN_BLOCK = Ratatouille.REGISTRATE
            .block("frozen_block", FrozenBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties((p) -> BlockBehaviour.Properties.of().mapColor(MapColor.ICE).strength(2.8F).friction(0.989F).sound(SoundType.GLASS).randomTicks())
            .transform(pickaxeOnly())
            .blockstate(simpleCubeAll("frozen_block"))
            .item()
            .build()
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<CompostTowerBlock> COMPOST_TOWER_BLOCK = Ratatouille.REGISTRATE
            .block("compost_tower", CompostTowerBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.lightLevel(state -> 1).noOcclusion().isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .blockstate(new CompostTowerModel.CompostTowerGenerator()::generate)
            .onRegister(CreateRegistrate.blockModel(() -> CompostTowerModel::standard))
            .addLayer(() -> RenderType::cutoutMipped)
            .item(CompostTowerBlockItem::new)
            .model((c, p) -> p.withExistingParent(c.getName(), new ResourceLocation(Ratatouille.MOD_ID, "block/compost_tower/item")))
            .build()
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<AeratorBlock> AERATOR_BLOCK = Ratatouille.REGISTRATE
            .block("aerator", AeratorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties((p) -> p.mapColor(MapColor.PODZOL))
            .transform(pickaxeOnly())
            .transform(CRStress.setImpact(2.0))
            .blockstate((c, p) -> p.simpleBlock((Block)c.getEntry(), AssetLookup.partialBaseModel(c, p, new String[0])))
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), new ResourceLocation(Ratatouille.MOD_ID, "block/aerator/item")))
            .build()
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<FishpondWallBlock> FISHPOND_WALL = Ratatouille.REGISTRATE
            .block("fishpond_wall", FishpondWallBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(pickaxeOnly())
            .blockstate(simpleCubeAll("fishpond_wall/fishpond_wall"))
            .onRegister(CreateRegistrate.connectedTextures(() -> new SimpleCTBehaviour(CRSpriteShifts.FISHPOND_WALL)))
            .item()
            .build()
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<FishpondFluidInterfaceBlock> FISHPOND_FLUID_INTERFACE = Ratatouille.REGISTRATE
            .block("fishpond_fluid_interface", FishpondFluidInterfaceBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.PODZOL).noOcclusion())
            .transform(pickaxeOnly())
            .blockstate(simpleCubeAll("fishpond_fluid_interface"))
            .item()
            .build()
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<SludgeBlock> SLUDGE = Ratatouille.REGISTRATE
            .block("sludge", SludgeBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.DIRT).noOcclusion().strength(0.5F).sound(SoundType.MUD))
            .transform(pickaxeOnly())
            .blockstate((ctx, prov) -> {
                prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> {
                    int thickness = state.getValue(SludgeBlock.THICKNESS);
                    return ConfiguredModel.builder()
                            .modelFile(prov.models().getExistingFile(prov.modLoc("block/sludge/sludge_" + thickness)))
                            .build();
                });
            })
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), Ratatouille.asResource("block/sludge/item")))
            .build()
            .register();

    @SuppressWarnings("removal")
    public static final BlockEntry<FishingNetBlock> FISHING_NET = Ratatouille.REGISTRATE
            .block("fishing_net", FishingNetBlock::new)
            .initialProperties(SharedProperties::wooden)
            .properties(p -> p.mapColor(MapColor.SNOW)
                    .sound(SoundType.SCAFFOLDING)
                    .noOcclusion())
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.directionalBlock(c.getEntry(),
                    p.models().getExistingFile(new ResourceLocation(Ratatouille.MOD_ID, "block/fishing_net/block"))))
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(),
                    new ResourceLocation(Ratatouille.MOD_ID, "block/fishing_net/item")))
            .build()
            .register();



    public static void register() {}
}
