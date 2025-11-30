package org.forsteri.ratatouille.entry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.content.compost_tower.CompostTowerBlockEntity;
import org.forsteri.ratatouille.content.compost_tower.CompostTowerRenderer;
import org.forsteri.ratatouille.content.demolder.DemolderVisual;
import org.forsteri.ratatouille.content.demolder.MechanicalDemolderBlockEntity;
import org.forsteri.ratatouille.content.demolder.MechanicalDemolderRenderer;
import org.forsteri.ratatouille.content.frozen_block.FrozenBlockEntity;
import org.forsteri.ratatouille.content.irrigation_tower.IrrigationTowerBlockEntity;
import org.forsteri.ratatouille.content.irrigation_tower.IrrigationTowerRenderer;
import org.forsteri.ratatouille.content.oven.OvenBlockEntity;
import org.forsteri.ratatouille.content.oven.OvenRenderer;
import org.forsteri.ratatouille.content.oven_fan.OvenFanBlockEntity;
import org.forsteri.ratatouille.content.oven_fan.OvenFanRenderer;
import org.forsteri.ratatouille.content.oven_fan.OvenFanVisual;
import org.forsteri.ratatouille.content.spreader.SpreaderBlockEntity;
import org.forsteri.ratatouille.content.spreader.SpreaderRenderer;
import org.forsteri.ratatouille.content.spreader.SpreaderVisual;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinBlockEntity;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinRenderer;
import org.forsteri.ratatouille.content.squeeze_basin.SqueezeBasinVisual;
import org.forsteri.ratatouille.content.thresher.ThresherBlockEntity;
import org.forsteri.ratatouille.content.thresher.ThresherRenderer;
import org.forsteri.ratatouille.content.thresher.ThresherVisual;
import org.forsteri.ratatouille.content.zinc_can.ZincCanBlockEntity;

public class CRBlockEntityTypes {
    public static final BlockEntityEntry<OvenBlockEntity> OVEN_ENTITY = Ratatouille.REGISTRATE
            .blockEntity("oven", OvenBlockEntity::new)
            .renderer(() -> OvenRenderer::new)
            .validBlock(CRBlocks.OVEN)
            .register();
    public static final BlockEntityEntry<ThresherBlockEntity> THRESHER_ENTITY = Ratatouille.REGISTRATE
            .blockEntity("thresher", ThresherBlockEntity::new)
            .visual(() -> ThresherVisual::new)
            .validBlock(CRBlocks.THRESHER)
            .renderer(() -> ThresherRenderer::new)
            .register();
    public static final BlockEntityEntry<OvenFanBlockEntity> OVEN_FAN_ENTITY = Ratatouille.REGISTRATE
            .blockEntity("oven_fan", OvenFanBlockEntity::new)
            .visual(() -> OvenFanVisual::new)
            .validBlock(CRBlocks.OVEN_FAN)
            .renderer(() -> OvenFanRenderer::new)
            .register();

    public static final BlockEntityEntry<SqueezeBasinBlockEntity> SQUEEZE_BASIN_ENTITY = Ratatouille.REGISTRATE
            .blockEntity("squeeze_basin", SqueezeBasinBlockEntity::new)
            .visual(() -> SqueezeBasinVisual::new)
            .validBlock(CRBlocks.SQUEEZE_BASIN)
            .renderer(() -> SqueezeBasinRenderer::new)
            .register();

    public static final BlockEntityEntry<MechanicalDemolderBlockEntity> MECHANICAL_DEMOLDER_ENTITY = Ratatouille.REGISTRATE
            .blockEntity("mechanical_demolder", MechanicalDemolderBlockEntity::new)
            .visual(() -> DemolderVisual::new)
            .validBlock(CRBlocks.MECHANICAL_DEMOLDER)
            .renderer(() -> MechanicalDemolderRenderer::new)
            .register();

    public static final BlockEntityEntry<IrrigationTowerBlockEntity> IRRIGATION_TOWER_BLOCK_ENTITY = Ratatouille.REGISTRATE
            .blockEntity("irrigation_tower", IrrigationTowerBlockEntity::new)
            .validBlock(CRBlocks.IRRIGATION_TOWER_BLOCK)
            .renderer(() -> IrrigationTowerRenderer::new)
            .register();

    public static final BlockEntityEntry<SpreaderBlockEntity> SPREADER_BLOCK_ENTITY = Ratatouille.REGISTRATE
            .blockEntity("spreader", SpreaderBlockEntity::new)
            .visual(() -> SpreaderVisual::new)
            .validBlock(CRBlocks.SPREADER_BLOCK)
            .renderer(() -> SpreaderRenderer::new)
            .register();

    public static final BlockEntityEntry<FrozenBlockEntity> FROZEN_BLOCK_ENTITY = Ratatouille.REGISTRATE
            .blockEntity("frozen_block", FrozenBlockEntity::new)
            .validBlock(CRBlocks.FROZEN_BLOCK)
            .register();

    public static final BlockEntityEntry<CompostTowerBlockEntity> COMPOST_TOWER_BLOCK_ENTITY = Ratatouille.REGISTRATE
            .blockEntity("compost_tower", CompostTowerBlockEntity::new)
            .validBlock(CRBlocks.COMPOST_TOWER_BLOCK)
            .renderer(() -> CompostTowerRenderer::new)
            .register();

    public static final BlockEntityEntry<ZincCanBlockEntity> ZINC_CAN_BLOCK_ENTITY = Ratatouille.REGISTRATE
            .blockEntity("zinc_can", ZincCanBlockEntity::new)
            .validBlock(CRBlocks.ZINC_CAN_BLOCK)
            .register();


    public static void register() {
    }
}
