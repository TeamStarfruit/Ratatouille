package org.forsteri.ratatouille.content.compost_tower;

import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.recipe.trie.AbstractVariant;
import com.simibubi.create.foundation.recipe.trie.RecipeTrie;
import com.simibubi.create.foundation.recipe.trie.RecipeTrieFinder;
import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.forsteri.ratatouille.util.Lang;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CompostData {
    public int sizeLevel;
    public int tempLevel;
    public int updateRequired;
    public int timer;
    public CompostingRecipe lastRecipe;
    private static final Object compostingRecipesKey = new Object();

    public void clear() {
        sizeLevel = 0;
        tempLevel = 0;
        timer = 0;
        lastRecipe = null;
    }

    public boolean updateCompostTower(CompostTowerBlockEntity controller) {
        assert controller.getLevel() != null;

        BlockPos controllerPos = controller.getBlockPos();
        Level level = controller.getLevel();
        updateRequired--;

        int prevTemp = tempLevel;
        tempLevel = 0;

        for (int xOffset = 0; xOffset < controller.getWidth(); xOffset++) {
            for (int zOffset = 0; zOffset < controller.getWidth(); zOffset++) {
                BlockPos pos = controllerPos.offset(xOffset, -1, zOffset);
                BlockState blockState = level.getBlockState(pos);

                float heat = BoilerHeater.findHeat(level, pos, blockState);
                if (heat > 0) {
                    tempLevel += (int) heat;
                }
            }
        }
        tempLevel = Mth.clamp(tempLevel, 0, 8);
        return tempLevel != prevTemp;
    }

    public void tick(CompostTowerBlockEntity controller) {
        if (updateCompostTower(controller))
            controller.notifyUpdate();

        assert controller.getLevel() != null;

        var inputInventory = controller.inputInventory;
        var fluidHandler = controller.tankInventory;

        if (timer > 0) {
            timer -= getProcessingSpeed();
            if (controller.getLevel().isClientSide) return;

            if (timer <= 0) {
                if (!CompostingRecipe.match(controller, lastRecipe)
                        || !canOutput(inputInventory, fluidHandler)) {
                    updateLastRecipe(controller);
                    return;
                }

                CompostingRecipe.apply(controller, lastRecipe);
                controller.notifyUpdate();
            }
        } else  {
            updateLastRecipe(controller);
        }
    }

    private boolean canOutput(ItemStackHandler outputInventory, IFluidHandler fluidHandler) {
        for (ItemStack outputStack : lastRecipe.rollResults()) {
            if (outputStack.isEmpty()) continue;
            if (!ItemHandlerHelper.insertItemStacked(outputInventory, outputStack, true).isEmpty()) {
                return false;
            }
        }
        for (FluidStack fluidStack : lastRecipe.getFluidResults()) {
            if (fluidStack.isEmpty()) continue;
            if (fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) < fluidStack.getAmount()) {
                return false;
            }
        }
        return true;
    }

    public boolean evaluate(CompostTowerBlockEntity tower) {
        assert tower.getLevel() != null;

        int sizeLevelBefore = sizeLevel;

        sizeLevel = tower.getWidth() * tower.getHeight() * tower.getWidth() / 4;
        sizeLevel = Mth.clamp(sizeLevel, 0, 8);
        return sizeLevelBefore != sizeLevel;
    }


    public void read(CompoundTag compound, boolean ignoredClientPacket) {
        sizeLevel = compound.getInt("sizeCount");
        tempLevel = compound.getInt("tempCount");
    }

    public void write(CompoundTag compound, boolean ignoredClientPacket) {
        compound.putInt("sizeCount", sizeLevel);
        compound.putInt("tempCount", tempLevel);
    }

    public MutableComponent getSizeComponent(boolean forGoggles, boolean useBlocksAsBars, ChatFormatting... styles) {
        return componentHelper("size", sizeLevel, forGoggles, useBlocksAsBars, styles);
    }

    public MutableComponent getHeatComponent(boolean forGoggles, boolean useBlocksAsBars, ChatFormatting... styles) {
        return componentHelper("heat", tempLevel, forGoggles, useBlocksAsBars, styles);
    }

    private MutableComponent componentHelper(String label, int level, boolean forGoggles, boolean useBlocksAsBars,
                                             ChatFormatting... styles) {
        MutableComponent base = useBlocksAsBars ? blockComponent(level) : barComponent(level);

        if (!forGoggles)
            return base;

        ChatFormatting style1 = styles.length >= 1 ? styles[0] : ChatFormatting.GRAY;
        ChatFormatting style2 = styles.length >= 2 ? styles[1] : ChatFormatting.DARK_GRAY;

        return Lang.translateDirect("compost_tower." + label)
                .withStyle(style1)
                .append(Lang.translateDirect("compost_tower." + label + "_dots")
                        .withStyle(style2))
                .append(base);
    }

    private MutableComponent blockComponent(int level) {
        int clamped = Mth.clamp(level, 0, 8);
        return Component.literal("\u2588".repeat(clamped) + "\u2591".repeat(8 - clamped));}

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
        int compostTowerLevel = Math.min(this.sizeLevel, this.tempLevel);
        if (compostTowerLevel == 0) {
            return Lang.translateDirect("compost_tower.idle", new Object[0]);
        } else {
            return compostTowerLevel >= 8 ? Lang.translateDirect("compost_tower.max_lvl", new Object[0]) : Lang.translateDirect("compost_tower.lvl", new Object[0]).append(String.valueOf(compostTowerLevel));
        }
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean ignoredIsPlayerSneaking) {
        Component indent = Component.literal("    ");
        Component indent2 = Component.literal("     ");

        tooltip.add(indent.copy()
                .append(Lang.translateDirect("compost_tower.status")
                        .append(this.getLevelComponent().withStyle(ChatFormatting.GREEN))));

        tooltip.add(indent2.copy().append(this.getSizeComponent(true, false)));
        tooltip.add(indent2.copy().append(this.getHeatComponent(true, false)));

        return true;
    }

    public int getProcessingSpeed() {
        if(Math.min(this.sizeLevel, this.tempLevel) == 0 )
            return 16;
        else
            return (int) (sizeLevel / 8.0 * tempLevel / 8.0 * 512);
    }

    private void updateLastRecipe(CompostTowerBlockEntity controller) {
        if (CompostingRecipe.match(controller, lastRecipe)) {
            timer = lastRecipe.getProcessingDuration();
            controller.notifyUpdate();
            return;
        }

        List<CompostingRecipe> list = new ArrayList<>();
        try {
            RecipeTrie<?> trie = RecipeTrieFinder.get(getRecipeCacheKey(), controller.getLevel(), this::matchStaticFilters);
            Set<AbstractVariant> availableVariants = RecipeTrie.getVariants(controller.inputInventory, controller.tankInventory);

            for (Recipe<?> r : trie.lookup(availableVariants))
                if (CompostingRecipe.match(controller, r))
                    list.add((CompostingRecipe) r);
        } catch (Exception e) {
            list.clear();

            for (Recipe<?> r : RecipeFinder.get(getRecipeCacheKey(), controller.getLevel(), this::matchStaticFilters))
                if (CompostingRecipe.match(controller, r))
                    list.add((CompostingRecipe) r);
        }

        list.sort((r1, r2) -> r2.getIngredients().size() - r1.getIngredients().size());

        if (!list.isEmpty()) {
            lastRecipe = list.get(0);
            timer = lastRecipe.getProcessingDuration();
            controller.notifyUpdate();
        } else {
            lastRecipe = null;
            timer = 100;
            controller.notifyUpdate();
        }
    }

    private boolean matchStaticFilters(Recipe<?> recipe) {
        return true;
    }

    protected Object getRecipeCacheKey() {
        return compostingRecipesKey;
    }
}
