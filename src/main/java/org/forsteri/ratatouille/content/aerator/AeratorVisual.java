package org.forsteri.ratatouille.content.aerator;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.forsteri.ratatouille.content.oven_fan.OvenFanBlockEntity;
import org.forsteri.ratatouille.entry.CRPartialModels;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AeratorVisual extends KineticBlockEntityVisual<AeratorBlockEntity> {
    protected final RotatingInstance fan;
    final Direction direction;

    public AeratorVisual(VisualizationContext context, AeratorBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        this.fan = (RotatingInstance)instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(CRPartialModels.AERATOR_BLADE))
                .createInstance();
        this.direction = (Direction)this.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        this.fan.setup(blockEntity, this.getFanSpeed())
                .setPosition(getVisualPosition())
                .rotateToFace(Direction.DOWN, direction)
                .setChanged();
    }

    private float getFanSpeed() {
        float speed = ((AeratorBlockEntity)this.blockEntity).getSpeed() * 2.0F;
        if (speed > 0.0F) {
            speed = Mth.clamp(speed, 80.0F, 1280.0F);
        }

        if (speed < 0.0F) {
            speed = Mth.clamp(speed, -1280.0F, -80.0F);
        }

        return speed;
    }

    @Override
    public void updateLight(float partialTick) {
        BlockPos inFront = pos.relative(direction);
        relight(inFront, fan);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(fan);
    }

    @Override
    protected void _delete() {
        fan.delete();
    }
}
