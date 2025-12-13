package org.forsteri.ratatouille.entry;

import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.content.compost_tower.CompostTowerBlock;
import org.forsteri.ratatouille.content.compost_tower.CompostTowerBlockItem;
import org.forsteri.ratatouille.content.compost_tower.CompostTowerModel;
import org.forsteri.ratatouille.content.demolder.MechanicalDemolderBlock;
import org.forsteri.ratatouille.content.frozen_block.FrozenBlock;
import org.forsteri.ratatouille.content.irrigation_tower.IrrigationTowerBlock;
import org.forsteri.ratatouille.content.oven.OvenBlock;
import org.forsteri.ratatouille.content.oven.OvenBlockItem;
import org.forsteri.ratatouille.content.oven.OvenModel;
import org.forsteri.ratatouille.content.oven_fan.OvenFanBlock;
import org.forsteri.ratatouille.content.spreader.SpreaderBlock;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinBlock;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinGenerator;
import org.forsteri.ratatouille.content.thresher.ThresherBlock;

import static com.simibubi.create.foundation.data.BlockStateGen.simpleCubeAll;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

@SuppressWarnings("removal")
public class CRBlocks {

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
    public static final BlockEntry<ThresherBlock> THRESHER = Ratatouille.REGISTRATE
            .block("thresher", ThresherBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.lightLevel(state -> 1).noOcclusion().isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p), 270))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(CRStress.setImpact((double) 4.0F))
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), Ratatouille.asResource("block/thresher/item")))
            .build()
            .register();
    public static final BlockEntry<OvenFanBlock> OVEN_FAN = Ratatouille.REGISTRATE
            .block("oven_fan", OvenFanBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.STONE))
            .transform(pickaxeOnly())
            //.transform(CRStress.setImpact(2.0f))
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(CRStress.setImpact((double) 2.0F))
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), Ratatouille.asResource("block/oven_fan/item")))
            .build()
            .register();
    public static final BlockEntry<SqueezeBasinBlock> SQUEEZE_BASIN = Ratatouille.REGISTRATE
            .block("squeeze_basin", SqueezeBasinBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.STONE))
            .transform(pickaxeOnly())
            .blockstate(new SqueezeBasinGenerator()::generate)
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), Ratatouille.asResource("block/squeeze_basin/item")))
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
            .model((c, p) -> p.withExistingParent(c.getName(), Ratatouille.asResource("block/irrigation_tower/item")))
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
            .model((c, p) -> p.withExistingParent(c.getName(), Ratatouille.asResource("block/spreader/item")))
            .build()
            .register();
    public static final BlockEntry<FrozenBlock> FROZEN_BLOCK = Ratatouille.REGISTRATE
            .block("frozen_block", FrozenBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties((p) -> BlockBehaviour.Properties.of().mapColor(MapColor.ICE).strength(2.8F).friction(0.989F).sound(SoundType.GLASS).randomTicks())
            .transform(pickaxeOnly())
            .blockstate(simpleCubeAll("frozen_block"))
            .item()
            .build()
            .register();
    public static final BlockEntry<CompostTowerBlock> COMPOST_TOWER_BLOCK = Ratatouille.REGISTRATE
            .block("compost_tower", CompostTowerBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.lightLevel(state -> 1).noOcclusion().isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .blockstate(new CompostTowerModel.CompostTowerGenerator()::generate)
            .onRegister(CreateRegistrate.blockModel(() -> CompostTowerModel::standard))
            .addLayer(() -> RenderType::cutoutMipped)
            .item(CompostTowerBlockItem::new)
            .model((c, p) -> p.withExistingParent(c.getName(), Ratatouille.asResource("block/compost_tower/item")))
            .build()
            .register();

//    static {
//        Ratatouille.REGISTRATE.setCreativeTab(CRCreativeModeTabs.BASE_CREATIVE_TAB);
//    }

    public static void register() {
    }
}
