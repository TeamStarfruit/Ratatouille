package org.forsteri.ratatouille.content.aerator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.forsteri.ratatouille.content.oven_fan.OvenFanBlockEntity;
import org.forsteri.ratatouille.entry.CRPartialModels;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class AeratorRenderer extends KineticBlockEntityRenderer<AeratorBlockEntity> {
    public AeratorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected void renderSafe(OvenFanBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) return;

        Direction direction = be.getBlockState()
                .getValue(HORIZONTAL_FACING);
        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
        LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(direction.getOpposite()));
        int lightBehind = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(direction.getOpposite()));
        int lightInFront = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(direction));

        SuperByteBuffer cogwheel =
                CachedBuffers.partialFacingVertical(CRPartialModels.AERATOR_BLADE, be.getBlockState(), direction.getOpposite());
        //SuperByteBuffer fanInner =
        //        CachedBuffers.partialFacing(CRPartialModels.OVEN_FAN_BLADE, be.getBlockState(), direction.getOpposite());
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float speed = be.getSpeed() * 5;
        if (speed > 0)
            speed = Mth.clamp(speed, 80, 64 * 20);
        if (speed < 0)
            speed = Mth.clamp(speed, -64 * 20, -80);
        float angle = (time * speed * 3 / 10f) % 360;
        angle = angle / 180f * (float) Math.PI;

        standardKineticRotationTransform(cogwheel, be, lightInFront).renderInto(ms, vb);
        //kineticRotationTransform(fanInner, be, direction.getAxis(), angle, lightInFront).renderInto(ms, vb);
    }
}
