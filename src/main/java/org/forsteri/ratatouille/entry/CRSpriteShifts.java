package org.forsteri.ratatouille.entry;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTType;
import net.minecraft.resources.ResourceLocation;
import org.forsteri.ratatouille.Ratatouille;

public class CRSpriteShifts {
    public static final CTSpriteShiftEntry OVEN_SPRITE = getCT("oven/oven"),
            OVEN_SPRITE_TOP = getCT("oven/oven_top"),
            OVEN_SPRITE_TOP_INNER = getCT("oven/oven_top_inner"),
            OVEN_SPRITE_BOTTOM = getCT("oven/oven_bottom"),
            OVEN_SPRITE_BOTTOM_INNER = getCT("oven/oven_bottom_inner"),
            OVEN_SPRITE_SHIFT_2x2 = getCT("oven/oven", "oven/oven_2x2"),
            COMPOST_TOWER_SPRITE = getCT("compost_tower/compost_tower"),
            COMPOST_TOWER_TOP = getCT("compost_tower/compost_tower_top"),
            COMPOST_TOWER_TOP_INNER = getCT("compost_tower/compost_tower_top_inner"),
            COMPOST_TOWER_BOTTOM = getCT("compost_tower/compost_tower_bottom"),
            COMPOST_TOWER_BOTTOM_INNER = getCT("compost_tower/compost_tower_bottom_inner"),
            COMPOST_TOWER_SHIFT_2x2 = getCT("compost_tower/compost_tower", "compost_tower/compost_tower_2x2"),
            FISHPOND_BLOCK = omni("fishpond_block");

    private static CTSpriteShiftEntry getCT(String blockTextureName, String connectedTextureName) {
        return CTSpriteShifter.getCT(AllCTTypes.RECTANGLE, new ResourceLocation(Ratatouille.MOD_ID, "block/" + blockTextureName),
                new ResourceLocation(Ratatouille.MOD_ID, "block/" + connectedTextureName + "_connected"));
    }

    private static CTSpriteShiftEntry getCT(String blockTextureName) {
        return getCT(blockTextureName, blockTextureName);
    }

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName, String connectedTextureName) {
        return CTSpriteShifter.getCT(type, Create.asResource("block/" + blockTextureName), Create.asResource("block/" + connectedTextureName + "_connected"));
    }

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName) {
        return getCT(type, blockTextureName, blockTextureName);
    }

    private static CTSpriteShiftEntry omni(String name) {
        return getCT(AllCTTypes.OMNIDIRECTIONAL, name);
    }

    public CRSpriteShifts() {}
    public static void register() {}
}
