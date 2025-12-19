package org.forsteri.ratatouille.content.compost_tower;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.ForgeCatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;

public class CompostTowerRenderer extends SafeBlockEntityRenderer<CompostTowerBlockEntity> {
    public CompostTowerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(CompostTowerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        if (!be.isController())
            return;

        renderFluids(be, partialTicks, ms, bufferSource, light);
        renderItems(be, partialTicks, ms, bufferSource, light, overlay);
    }

    private void renderFluids(CompostTowerBlockEntity be, float partialTicks,
                              PoseStack ms, MultiBufferSource bufferSource, int light) {

        float capHeight = 1 / 5f;
        float tankHullWidth = 1 / 128f;
        float minPuddleHeight = 1 / 16f;

        float totalHeight = be.height - 2 * capHeight - minPuddleHeight;

        var sortedFluids = be.tankInventory.getSortedFluids();

        float accumulatedFluidHeight = 0;
        float accumulatedGasHeight = 0;

        for (var fluid : sortedFluids) {
            boolean isGas = fluid.getFluidType().isLighterThanAir();

            LerpedFloat levelValue =
                    (isGas ? be.gasLevels : be.fluidLevels).get(fluid);
            if (levelValue == null)
                continue;

            float ratio = levelValue.getValue(partialTicks);
            if (ratio < 1 / (512f * totalHeight))
                continue;

            float fluidHeight = ratio * totalHeight;

            float yStart;
            float yEnd;

            if (isGas) {
                yStart = totalHeight - accumulatedGasHeight - fluidHeight
                        + capHeight + minPuddleHeight;
                yEnd   = totalHeight - accumulatedGasHeight
                        + capHeight + minPuddleHeight;
                accumulatedGasHeight += fluidHeight;
            } else {
                yStart = accumulatedFluidHeight
                        + capHeight + minPuddleHeight;
                yEnd   = accumulatedFluidHeight + fluidHeight
                        + capHeight + minPuddleHeight;
                accumulatedFluidHeight += fluidHeight;
            }

            float xMin = tankHullWidth;
            float xMax = xMin + be.radius - 2 * tankHullWidth;
            float zMin = tankHullWidth;
            float zMax = zMin + be.radius - 2 * tankHullWidth;

            ms.pushPose();
            ForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
                    fluid.defaultFluidState(),
                    xMin, yStart, zMin,
                    xMax, yEnd, zMax,
                    bufferSource, ms, light,
                    false, true
            );
            ms.popPose();
//            if (top) {
//                accumulatedGasHeight += initialLevel;
//            } else {
//                accumulatedFluidHeight += initialLevel;
//            }
//            break;
        }
    }


    private void renderItems(CompostTowerBlockEntity be, float partialTicks,
                             PoseStack ms, MultiBufferSource buffer,
                             int light, int overlay) {

        float fluidSurfaceY = getFluidSurfaceLocalY(be, partialTicks);
        if (fluidSurfaceY >= be.getHeight() - 3/16f)
            return;

        renderInventoryItems(be, be.inputInventory, fluidSurfaceY, partialTicks,
                ms, buffer, light, overlay, 0f);
        renderInventoryItems(be, be.outputInventory, fluidSurfaceY, partialTicks,
                ms, buffer, light, overlay, 90f);
    }

    private void renderInventoryItems(CompostTowerBlockEntity be,
                                      ItemStackHandler inventory,
                                      float fluidSurfaceY,
                                      float partialTicks,
                                      PoseStack ms,
                                      MultiBufferSource buffer,
                                      int light,
                                      int overlay,
                                      float angleOffset) {

        int radius = be.getWidth();
        RandomSource random = RandomSource.create(be.getBlockPos().hashCode());

        int itemCount = 0;
        for (int i = 0; i < inventory.getSlots(); i++)
            if (!inventory.getStackInSlot(i).isEmpty())
                itemCount++;

        if (itemCount == 0)
            return;

        float baseY;
        boolean hasFluid = fluidSurfaceY > 0.01f;

        if (hasFluid) {
            baseY = fluidSurfaceY - 2/16f;
        } else {
            baseY = 1 / 16f;
        }

        float angleStep = 360f / itemCount;
        int index = 0;

        ms.pushPose();
        ms.translate(radius / 2f, 0, radius / 2f);

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty())
                continue;

            ms.pushPose();

            float angle = angleStep * index++ + angleOffset;

            if (hasFluid) {
                float bob =
                        (Mth.sin(
                                AnimationTickHolder.getRenderTime(be.getLevel()) / 12f
                                        + angle) + 1.5f)
                                * (1 / 32f);
                ms.translate(0, bob, 0);
            }

            Vec3 offset = VecHelper.rotate(
                    new Vec3(radius * 0.2f, baseY, 0),
                    angle,
                    Direction.Axis.Y
            );

            ms.translate(offset.x, offset.y, offset.z);

            TransformStack.of(ms)
                    .rotateYDegrees(angle + 35)
                    .rotateXDegrees(65);

            int renderCount = Math.max(1, stack.getCount() / 8);
            for (int i = 0; i < renderCount; i++) {
                ms.pushPose();
                Vec3 jitter = VecHelper.offsetRandomly(Vec3.ZERO, random, 1 / 16f);
                ms.translate(jitter.x, jitter.y, jitter.z);
                renderItem(ms, buffer, light, overlay, stack);
                ms.popPose();
            }

            ms.popPose();
        }

        ms.popPose();
    }


    protected void renderItem(PoseStack ms, MultiBufferSource buffer, int light, int overlay, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, ms, buffer, mc.level, 0);
    }

    private float getFluidSurfaceLocalY(CompostTowerBlockEntity be, float partialTicks) {
        float capHeight = 1 / 5f;
        float minPuddleHeight = 1 / 16f;

        float totalHeight = be.height - 2 * capHeight - minPuddleHeight;
        float accumulatedFluidHeight = 0f;

        for (var fluid : be.tankInventory.getSortedFluids()) {
            if (fluid.getFluidType().isLighterThanAir())
                continue;

            LerpedFloat lerp = be.fluidLevels.get(fluid);
            if (lerp == null)
                continue;

            float ratio = lerp.getValue(partialTicks);
            accumulatedFluidHeight += ratio * totalHeight;
        }

        return accumulatedFluidHeight + capHeight + minPuddleHeight;
    }



    @Override
    public boolean shouldRenderOffScreen(CompostTowerBlockEntity be) {
        return be.isController();
    }

}