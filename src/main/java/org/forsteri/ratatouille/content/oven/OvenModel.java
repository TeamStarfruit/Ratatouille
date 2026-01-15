package org.forsteri.ratatouille.content.oven;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour;
import com.simibubi.create.content.fluids.tank.FluidTankGenerator;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlock;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import org.forsteri.ratatouille.entry.CRSpriteShifts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OvenModel extends CTModel {
    protected static final ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

    public OvenModel(BakedModel originalModel, CTSpriteShiftEntry side, CTSpriteShiftEntry top,
                     CTSpriteShiftEntry topInner, CTSpriteShiftEntry bottom, CTSpriteShiftEntry bottomInner, CTSpriteShiftEntry shift2x2) {
        super(originalModel, new OvenCTBehavior(side, top, topInner, bottom, bottomInner, shift2x2));
    }

    public static OvenModel standard(BakedModel originalModel) {
        return new OvenModel(originalModel,
                CRSpriteShifts.OVEN_SPRITE, CRSpriteShifts.OVEN_SPRITE_TOP, CRSpriteShifts.OVEN_SPRITE_TOP_INNER, CRSpriteShifts.OVEN_SPRITE_BOTTOM, CRSpriteShifts.OVEN_SPRITE_BOTTOM_INNER, CRSpriteShifts.OVEN_SPRITE_SHIFT_2x2);
    }

    @Override
    protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
                                                ModelData blockEntityData) {
        super.gatherModelData(builder, world, pos, state, blockEntityData);
        CullData cullData = new CullData();
        for (Direction d : Iterate.directions) {
            cullData.setCulled(d, ConnectivityHandler.isConnected(world, pos, pos.relative(d)) ||
                    (world.getBlockState(pos.relative(d)).getBlock() instanceof EncasedFanBlock &&
                            world.getBlockState(pos.relative(d)).getValue(EncasedFanBlock.FACING) == d.getOpposite()));
        }

        cullData.setCulled(null, !(ConnectivityHandler.isConnected(world, pos, pos.above()) ||
                ConnectivityHandler.isConnected(world, pos, pos.below())));

        return builder.with(CULL_PROPERTY, cullData);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
        if (side != null)
            return Collections.emptyList();

        List<BakedQuad> quads = new ArrayList<>();
        for (Direction d : Iterate.directions) {
            if (extraData.has(CULL_PROPERTY) && Objects.requireNonNull(extraData.get(CULL_PROPERTY))
                    .isCulled(d))
                continue;
            quads.addAll(super.getQuads(state, d, rand, extraData, renderType));
        }

        if (extraData.has(CULL_PROPERTY) && !Objects.requireNonNull(extraData.get(CULL_PROPERTY))
                .isCulled(null))
            quads.addAll(super.getQuads(state, null, rand, extraData, renderType));
        return quads;
    }

    public static class OvenCTBehavior extends FluidTankCTBehaviour {
        private final CTSpriteShiftEntry bottomShift;
        private final CTSpriteShiftEntry bottomInnerShift;
        private final CTSpriteShiftEntry shift2x2;

        public OvenCTBehavior(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift, CTSpriteShiftEntry innerShift,
                              CTSpriteShiftEntry bottom, CTSpriteShiftEntry bottomInner, CTSpriteShiftEntry shift2x2) {
            super(layerShift, topShift, innerShift);
            this.bottomShift = bottom;
            this.bottomInnerShift = bottomInner;
            this.shift2x2 = shift2x2;
        }

        @Override
        public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            if (sprite != null && direction.getAxis() == Direction.Axis.Y && bottomShift.getOriginal() == sprite)
                return bottomShift;
            if (sprite != null && direction.getAxis() == Direction.Axis.Y && bottomInnerShift.getOriginal() == sprite)
                return bottomInnerShift;
            CTSpriteShiftEntry shift = super.getShift(state, direction, sprite);
            if (shift == CRSpriteShifts.OVEN_SPRITE && state.getValue(OvenBlock.IS_2x2))
                return shift2x2;
            return shift;
        }
    }

    private static class CullData {
        boolean[] culledFaces;

        public CullData() {
            culledFaces = new boolean[7];
            Arrays.fill(culledFaces, false);
        }

        void setCulled(Direction face, boolean cull) {
            if (face == null) {
                culledFaces[6] = cull;
                return;
            }
            culledFaces[face.get3DDataValue()] = cull;
        }

        boolean isCulled(Direction face) {
            if (face == null) {
                return culledFaces[6];
            }
            return culledFaces[face.get3DDataValue()];
        }
    }

    public static class OvenGenerator extends FluidTankGenerator {
        protected ModelFile model = null;

        @Override
        public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                    BlockState state) {
            if (model != null)
                return model;
            return model = prov.models()
                    .getExistingFile(prov.modLoc(ctx.getName()));
        }
    }
}
