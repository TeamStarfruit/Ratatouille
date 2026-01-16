package org.forsteri.ratatouille.content.compost_tower;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.forsteri.ratatouille.entry.CRFluids;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CompostFluidTank implements IFluidHandler, INBTSerializable<CompoundTag> {
    protected int capacity;
    protected int index;
    protected final BiMap<Fluid, Integer> fluidIds;
    protected final HashMap<Fluid, Integer> tanks;
    protected final Consumer<HashMap<Fluid, Integer>> updateCallback;

    public CompostFluidTank(BiMap<Fluid, Integer> fluidIds, HashMap<Fluid, Integer> tanks,
                            Consumer<HashMap<Fluid, Integer>> updateCallback, int index, int capacity) {
        this.fluidIds = fluidIds;
        this.tanks = tanks;
        this.updateCallback = updateCallback;
        this.index = index;
        this.capacity = capacity;
    }

    public CompostFluidTank(int capacity, Consumer<HashMap<Fluid, Integer>> updateCallback) {
        this.capacity = capacity;
        this.updateCallback = updateCallback;
        this.tanks = new HashMap<>();
        this.fluidIds = HashBiMap.create();
        this.index = 0;
    }

    public void onContentsChanged() {
        updateCallback.accept(tanks);
    }

    public float getFilledPercentage(Fluid fluid) {
        float existingAmount = tanks.getOrDefault(fluid, 0);
        return existingAmount / capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getFilledAmount() {
        return tanks.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getRemainingAmount() {
        return capacity - getFilledAmount();
    }

    @Override
    public int getTanks() {
        return index + 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        var fluid = this.fluidIds.inverse().get(tank);
        if (fluid == null) return FluidStack.EMPTY;
        return new FluidStack(fluid, this.tanks.get(fluid));
    }

    @Override
    public int getTankCapacity(int tank) {
        var fluid = this.fluidIds.inverse().get(tank);
        if (fluid == null) return getRemainingAmount();
        return getRemainingAmount() + this.tanks.get(fluid);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        var fluid = this.fluidIds.inverse().get(tank);
        if (fluid == null) return true;
        return stack.getFluid().isSame(fluid);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        var resourceFluid = resource.getFluid();
        var resourceAmount = resource.getAmount();

        var existingAmount = this.tanks.getOrDefault(resourceFluid, 0);

        int filled = Math.min(getRemainingAmount(), resourceAmount);
        if (action.execute()) {
            this.tanks.put(resourceFluid, existingAmount + filled);
            this.onContentsChanged();
            if (!this.fluidIds.containsKey(resourceFluid))
                this.fluidIds.put(resourceFluid, this.index++);
        }
        return filled;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        var resourceFluid = resource.getFluid();
        var resourceAmount = resource.getAmount();

        var existingAmount = this.tanks.getOrDefault(resourceFluid, 0);
        int drained = Math.min(existingAmount, resourceAmount);
        if (action.execute()) {
            this.tanks.put(resourceFluid, existingAmount - drained);
            this.onContentsChanged();
        }

        return new FluidStack(resourceFluid, drained);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0 || tanks.isEmpty()) return FluidStack.EMPTY;

        Fluid maxFluid = tanks.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(HashMap.Entry::getKey)
                .orElse(null);

        if (maxFluid == null) return FluidStack.EMPTY;

        int available = tanks.get(maxFluid);
        int drained = Math.min(available, maxDrain);

        if (action.execute()) {
            tanks.put(maxFluid, available - drained);
            onContentsChanged();
        }

        return new FluidStack(maxFluid, drained);
    }

    @Override
    @ParametersAreNonnullByDefault
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        tag.putInt("Capacity", capacity);
        tag.putInt("Index", index);

        var tankList = new net.minecraft.nbt.ListTag();
        for (Fluid fluid : tanks.keySet()) {
            CompoundTag fluidTag = new CompoundTag();
            var fluidStack = new FluidStack(fluid, tanks.get(fluid));
            if (fluidStack.isEmpty()) continue;
            fluidTag.put("FluidStack", fluidStack.save(provider));
            fluidTag.putInt("Id", fluidIds.get(fluid));
            tankList.add(fluidTag);
        }

        tag.put("Tanks", tankList);
        return tag;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.capacity = nbt.getInt("Capacity");
        this.index = nbt.getInt("Index");

        this.tanks.clear();
        this.fluidIds.clear();

        var tankList = nbt.getList("Tanks", Tag.TAG_COMPOUND);
        for (int i = 0; i < tankList.size(); i++) {
            CompoundTag fluidTag = tankList.getCompound(i);

            FluidStack stack = FluidStack.parseOptional(provider, fluidTag.getCompound("FluidStack"));
            if (stack.isEmpty()) continue;

            int id = fluidTag.getInt("Id");
            Fluid fluid = stack.getFluid();

            this.tanks.put(fluid, stack.getAmount());
            this.fluidIds.put(fluid, id);
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public Fluid[] getSortedFluids() {
        return tanks.keySet().stream()
                .sorted(Comparator.comparingInt((Fluid f) -> f.getFluidType().getDensity()).reversed())
                .toArray(Fluid[]::new);
    }

    public void clearOverflow() {
        int overflow = -getRemainingAmount();
        if (overflow <= 0) return;

        for (Fluid fluid : tanks.keySet()) {
            int amount = tanks.getOrDefault(fluid, 0);
            if (amount <= 0) continue;

            int toDrain = Math.min(overflow, amount);
            tanks.put(fluid, amount - toDrain);
            overflow -= toDrain;

            if (overflow <= 0) break;
        }
    }

    public FluidStack[] getFluidStacks() {
        return tanks.entrySet().stream()
                .map(entry -> new FluidStack(entry.getKey(), entry.getValue()))
                .toArray(FluidStack[]::new);
    }

    public void fillFrom(CompostFluidTank otherTank) {
        for (FluidStack stack : otherTank.getFluidStacks()) {
            this.fill(stack, FluidAction.EXECUTE);
        }
    }

    public void clear() {
        tanks.replaceAll((f, v) -> 0);
        onContentsChanged();
    }

    private boolean isValidInputFluid(Fluid resource) {
        return resource.isSame(CRFluids.COMPOST_FLUID.get());
    }

    public Fluid getFluidAtBlockHeight(int blockHeight, int towerHeight) {
        if (tanks.isEmpty() || towerHeight <= 0) return FluidStack.EMPTY.getFluid();

        List<Fluid> liquids = tanks.keySet().stream()
                .filter(f -> !f.getFluidType().isLighterThanAir())
                .sorted(Comparator.comparingInt((Fluid f) -> f.getFluidType().getDensity()).reversed())
                .toList();

        List<Fluid> gases = tanks.keySet().stream()
                .filter(f -> f.getFluidType().isLighterThanAir())
                .sorted(Comparator.comparingInt((Fluid f) -> f.getFluidType().getDensity()))
                .toList();

        float accumulatedHeight = 0;
        for (Fluid fluid : liquids) {
            accumulatedHeight += getFilledPercentage(fluid);
            double height = Math.floor(accumulatedHeight * towerHeight);
            if (tanks.getOrDefault(fluid, 0) == 0 || isValidInputFluid(fluid)) continue;
            if (blockHeight <= height)
                return fluid;
        }

        accumulatedHeight = 0;
        for (Fluid fluid : gases) {
            accumulatedHeight += getFilledPercentage(fluid);
            double height = Math.ceil(accumulatedHeight * towerHeight);
            if (tanks.getOrDefault(fluid, 0) == 0 || isValidInputFluid(fluid)) continue;
            if (blockHeight >= towerHeight - height)
                return fluid;
        }

        return FluidStack.EMPTY.getFluid();
    }
}
