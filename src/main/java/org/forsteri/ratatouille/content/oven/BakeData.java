package org.forsteri.ratatouille.content.oven;

import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.forsteri.ratatouille.content.oven_fan.OvenFanBlock;
import org.forsteri.ratatouille.content.oven_fan.OvenFanBlockEntity;
import org.forsteri.ratatouille.util.Lang;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BakeData {
    public int fanLevel;
    public int sizeLevel;
    public int tempLevel;
    public int updateRequired;

    public void tick(OvenBlockEntity oven) {
        if (updateOven(oven))
            oven.notifyUpdate();

        processFood(oven);
    }

    public boolean updateOven(OvenBlockEntity controller) {
        assert controller.getLevel() != null;

        BlockPos controllerPos = controller.getBlockPos();
        Level level = controller.getLevel();
        updateRequired--;

        int prevTemp = tempLevel;
        tempLevel = 0;

        for (int xOffset = 0; xOffset < controller.radius; xOffset++) {
            for (int zOffset = 0; zOffset < controller.radius; zOffset++) {
                BlockPos pos = controllerPos.offset(xOffset, -1, zOffset);
                BlockState blockState = level.getBlockState(pos);

                float heat = BoilerHeater.findHeat(level, pos, blockState);
                if (heat > 0) {
                    tempLevel += (int) heat;
                }
            }
        }

        int fanLevelBefore = fanLevel;

        float newFanLevel = 0;

        for (int i = 0; i < controller.radius; i++) {
            for (int j = 0; j < controller.height; j++) {
                for (int k = 0; k < controller.radius; k++) {
                    for (Direction direction : Direction.values()) {
                        BlockPos pos = controller.getBlockPos().offset(i, j, k).relative(direction);
                        BlockState state = controller.getLevel().getBlockState(pos);
                        if (state.getBlock() instanceof OvenFanBlock
                                && state.getValue(OvenFanBlock.HORIZONTAL_FACING) == direction.getOpposite()) {
                            OvenFanBlockEntity fan = (OvenFanBlockEntity) controller.getLevel().getBlockEntity(pos);
                            if (fan == null)
                                continue;
                            if (Mth.abs(fan.getSpeed()) == 256f) {
                                newFanLevel += 1;
                            } else if (Mth.abs(fan.getSpeed()) >= 128f) {
                                newFanLevel += 0.5f;
                            }
                        }
                    }
                }
            }
        }

        fanLevel = (int) newFanLevel;
        fanLevel = Math.clamp(fanLevel, 0, 8);
        tempLevel = Math.clamp(tempLevel, 0, 8);

        return tempLevel != prevTemp || fanLevelBefore != fanLevel;
    }

    public void processFood(OvenBlockEntity oven) {
        List<List<List<OvenBlockEntity.Inventory>>> inventories = oven.inventories;

        if (inventories == null)
            return;

        for (List<List<OvenBlockEntity.Inventory>> x : inventories) {
            for (List<OvenBlockEntity.Inventory> y : x) {
                for (OvenBlockEntity.Inventory inventory : y) {
                    if (inventory == null)
                        continue;
                    if (inventory.tickTillFinishCooking < 0)
                        continue;

                    if (inventory.tickTillFinishCooking > 0)
                        inventory.tickTillFinishCooking -= Math.min(Math.min(fanLevel, sizeLevel), tempLevel);

                    if (inventory.tickTillFinishCooking <= 0) {
                        if (inventory.lastRecipe == null)
                            continue;
                        ItemStack resultStack = inventory.lastRecipe.getResultItem(RegistryAccess.EMPTY).copy();
                        resultStack.setCount(inventory.getStackInSlot(0).getCount());
                        inventory.setStackInSlot(0, resultStack);
                    }
                }
            }
        }

        oven.notifyUpdate();
    }

    public boolean evaluate(OvenBlockEntity oven) {
        assert oven.getLevel() != null;

        int sizeLevelBefore = sizeLevel;

        sizeLevel = oven.radius * oven.height * oven.radius / 4;
        sizeLevel = Math.clamp(sizeLevel, 0, 8);

        return sizeLevelBefore != sizeLevel;
    }

    public void clear() {
        fanLevel = 0;
        sizeLevel = 0;
        tempLevel = 0;
    }

    public void read(CompoundTag compound, boolean ignoredClientPacket) {
        fanLevel = compound.getInt("fanCount");
        sizeLevel = compound.getInt("sizeCount");
        tempLevel = compound.getInt("tempCount");
    }

    public void write(CompoundTag compound, boolean ignoredClientPacket) {
        compound.putInt("fanCount", fanLevel);
        compound.putInt("sizeCount", sizeLevel);
        compound.putInt("tempCount", tempLevel);
    }


    public MutableComponent getSizeComponent(boolean forGoggles, boolean useBlocksAsBars, ChatFormatting... styles) {
        return componentHelper("size", sizeLevel, forGoggles, useBlocksAsBars, styles);
    }

    public MutableComponent getHeatComponent(boolean forGoggles, boolean useBlocksAsBars, ChatFormatting... styles) {
        return componentHelper("heat", tempLevel, forGoggles, useBlocksAsBars, styles);
    }

    public MutableComponent getFanComponent(boolean forGoggles, boolean useBlocksAsBars, ChatFormatting... styles) {
        return componentHelper("fan", fanLevel, forGoggles, useBlocksAsBars, styles);
    }

    private MutableComponent componentHelper(String label, int level, boolean forGoggles, boolean useBlocksAsBars,
                                             ChatFormatting... styles) {
        MutableComponent base = useBlocksAsBars ? blockComponent(level) : barComponent(level);

        if (!forGoggles)
            return base;

        ChatFormatting style1 = styles.length >= 1 ? styles[0] : ChatFormatting.GRAY;
        ChatFormatting style2 = styles.length >= 2 ? styles[1] : ChatFormatting.DARK_GRAY;

        return Lang.translateDirect("oven." + label)
                .withStyle(style1)
                .append(Lang.translateDirect("oven." + label + "_dots")
                        .withStyle(style2))
                .append(base);
    }

    private MutableComponent blockComponent(int level) {
        int clamped = Mth.clamp(level, 0, 8);
        return Component.literal("\u2588".repeat(clamped) + "\u2591".repeat(8 - clamped));
    }

    private MutableComponent barComponent(int level) {
        return Component.empty()
                .append(bars(Math.max(0, 0 - 1), ChatFormatting.DARK_GREEN))
                .append(bars(0 > 0 ? 1 : 0, ChatFormatting.GREEN))
                .append(bars(Math.max(0, level - 0), ChatFormatting.DARK_GREEN))
                .append(bars(Math.max(0, 8 - level), ChatFormatting.DARK_RED))
                .append(bars(Math.max(0, Math.min(18 - 8, ((8 / 5 + 1) * 5) - 8)),
                        ChatFormatting.DARK_GRAY));

    }

    private MutableComponent bars(int level, ChatFormatting format) {
        return Component.literal(Strings.repeat('|', level))
                .withStyle(format);
    }

    public @NotNull MutableComponent getLevelComponent() {
        int ovenLevel = Math.min(this.fanLevel, Math.min(this.sizeLevel, this.tempLevel));
        if (ovenLevel == 0) {
            return Lang.translateDirect("oven.idle", new Object[0]);
        } else {
            return ovenLevel >= 8 ? Lang.translateDirect("oven.max_lvl", new Object[0]) : Lang.translateDirect("oven.lvl", new Object[0]).append(String.valueOf(ovenLevel));
        }
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean ignoredIsPlayerSneaking, int ovenSize) {
        Component indent = Component.literal("    ");
        Component indent2 = Component.literal("     ");

        tooltip.add(indent.copy()
                .append(Lang.translateDirect("oven.status")
                        .append(this.getLevelComponent().withStyle(ChatFormatting.GREEN))));

        tooltip.add(indent2.copy().append(this.getSizeComponent(true, false)));
        tooltip.add(indent2.copy().append(this.getFanComponent(true, false)));
        tooltip.add(indent2.copy().append(this.getHeatComponent(true, false)));

        return true;
    }
}
