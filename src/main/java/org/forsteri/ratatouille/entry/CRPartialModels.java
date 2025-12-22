package org.forsteri.ratatouille.entry;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import org.forsteri.ratatouille.Ratatouille;

public class CRPartialModels {

    public static final PartialModel THRESHER_BLADE = block("block/thresher/partial");
    public static final PartialModel OVEN_FAN_BLADE = block("block/oven_fan/partial");
    public static final PartialModel SQUEEZE_BASIN_COVER = block("block/squeeze_basin/partial");
    public static final PartialModel MECHANICAL_DEMOLDER_HEAD = block("block/mechanical_demolder/head");
    public static final PartialModel CHEF_HAT = block("block/chef_hat");
    public static final PartialModel CHEF_HAT_WITH_GOGGLES = block("block/chef_hat_with_goggles");
    public static final PartialModel AERATOR_BLADE = block("block/aerator/partial");


    private static PartialModel block(@SuppressWarnings("SameParameterValue") String path) {
        return PartialModel.of(new ResourceLocation(Ratatouille.MOD_ID, path));
    }

    public CRPartialModels() {}
    public static void register() {}
}
