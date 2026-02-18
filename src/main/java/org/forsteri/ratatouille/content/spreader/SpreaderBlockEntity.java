package org.forsteri.ratatouille.content.spreader;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.content.kinetics.fan.NozzleBlockEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.forsteri.ratatouille.entry.CRItems;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import java.util.ArrayList;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class SpreaderBlockEntity  extends KineticBlockEntity implements IAirCurrentSource {
    public int timer;
    public ItemStackHandler inventory;
    public LazyOptional<IItemHandler> capability;
    public AirCurrent airCurrent;
    protected int airCurrentUpdateCooldown;
    protected int entitySearchCooldown;
    protected boolean updateAirFlow;

    public SpreaderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        airCurrent = new AirCurrent(this);
        updateAirFlow = true;
        timer = 1000;
        this.inventory = new ItemStackHandler(1);
        this.capability = LazyOptional.of(SpreaderBlockEntity.SpreaderInventoryHandler::new);
    }

    public void invalidate() {
        super.invalidate();
        this.capability.invalidate();
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if (clientPacket)
            airCurrent.rebuild();
        this.timer = compound.getInt("Timer");
        this.inventory.deserializeNBT(compound.getCompound("Inventory"));
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("Timer", this.timer);
        compound.put("Inventory", this.inventory.serializeNBT());
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (isItemHandlerCap(cap))
            return capability.cast();
        return super.getCapability(cap, side);
    }

    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(this.level, this.worldPosition, this.inventory);
    }

    @Override
    public AirCurrent getAirCurrent() {
        return airCurrent;
    }

    @Nullable
    @Override
    public Level getAirCurrentWorld() {
        return level;
    }

    @Override
    public BlockPos getAirCurrentPos() {
        return worldPosition;
    }

    @Override
    public Direction getAirflowOriginSide() {
        return this.getBlockState()
                .getValue(FACING);
    }

    @Override
    public Direction getAirFlowDirection() {
        float speed = getSpeed();
        if (speed == 0)
            return null;
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        speed = convertToDirection(speed, facing);
        return speed > 0 ? facing : facing.getOpposite();
    }

    @Override
    public void remove() {
        super.remove();
        updateChute();
    }

    @Override
    public boolean isSourceRemoved() {
        return remove;
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        updateAirFlow = true;
        updateChute();
    }

    public void updateChute() {
        Direction direction = getBlockState().getValue(FACING);
        if (!direction.getAxis()
                .isVertical())
            return;
        BlockEntity poweredChute = level.getBlockEntity(worldPosition.relative(direction));
        if (!(poweredChute instanceof ChuteBlockEntity))
            return;
        ChuteBlockEntity chuteBE = (ChuteBlockEntity) poweredChute;
        if (direction == Direction.DOWN)
            chuteBE.updatePull();
        else
            chuteBE.updatePush(1);
    }

    public void blockInFrontChanged() {
        updateAirFlow = true;
    }

    public int getProcessingSpeed() {
        return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
    }

    public int getProcessingLevel() {
        int speed = getProcessingSpeed();
        if (1 <= speed && speed < 4) {
            return 2;
        } else if (4 <= speed && speed < 8) {
            return 3;
        } else if (8 <= speed && speed < 12) {
            return 4;
        } else {
            return 5;
        }
    }

    public void addCorpBuffer(ArrayList<BlockPos> corps, BlockPos corpPos) {
        BlockState corpState = level.getBlockState(corpPos);
        if (!(corpState.getBlock() instanceof AirBlock)
            && !(corpState.getBlock() instanceof GrassBlock)
            && !(corpState.getBlock() instanceof TallGrassBlock)
            && corpState.getBlock() instanceof BonemealableBlock growable
            && growable.isValidBonemealTarget(level, corpPos, corpState, false)
        ) {
            corps.add(corpPos);
        }
    }

    @Override
    public void tick() {
        super.tick();

        boolean server = !level.isClientSide || isVirtual();

        if (server && airCurrentUpdateCooldown-- <= 0) {
            airCurrentUpdateCooldown = AllConfigs.server().kinetics.fanBlockCheckRate.get();
            updateAirFlow = true;
        }

        if (updateAirFlow) {
            updateAirFlow = false;
            airCurrent.rebuild();
            sendData();
        }

        if (getSpeed() == 0)
            return;

        if (entitySearchCooldown-- <= 0) {
            entitySearchCooldown = 5;
            airCurrent.findEntities();
        }

        airCurrent.tick();

        if (!level.isClientSide && !inventory.getStackInSlot(0).isEmpty() && level.getServer() != null) {
            if (timer > 0) {
                timer -= getProcessingSpeed();
            } else {
                timer = 1000;
                ArrayList<BlockPos> corps = new ArrayList<>();
                ArrayList<Animal> adultAnimals = new ArrayList<>();
                ArrayList<AgeableMob> babyAnimals = new ArrayList<>();

                BlockPos facingPos = getBlockPos().relative(getBlockState().getValue(FACING));
                boolean hasNozzle = level.getBlockEntity(facingPos) instanceof NozzleBlockEntity;
                int range = hasNozzle ? (int) (getMaxDistance() / 2) : (int) airCurrent.maxDistance;

                if (hasNozzle) {
                    BlockPos min = facingPos.offset(-range, -range, -range);
                    BlockPos max = facingPos.offset(range, range, range);
                    for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
                        addCorpBuffer(corps, new BlockPos(pos));
                    }

                    var aabb = new net.minecraft.world.phys.AABB(min, max);
                    level.getEntitiesOfClass(Animal.class, aabb).forEach(animal -> {
                        if (!animal.isBaby() && !animal.isInLove()) adultAnimals.add(animal);
                        if (animal.isBaby()) babyAnimals.add(animal);
                    });
                } else {
                    for (int i = 1; i < range; i++) {
                        BlockPos corpPos = getBlockPos().relative(airCurrent.direction, i);
                        addCorpBuffer(corps, corpPos);

                        var aabb = new net.minecraft.world.phys.AABB(corpPos).inflate(0.5);
                        level.getEntitiesOfClass(Animal.class, aabb).forEach(animal -> {
                            if (!animal.isBaby() && !animal.isInLove()) adultAnimals.add(animal);
                            if (animal.isBaby()) babyAnimals.add(animal);
                        });
                    }
                }

                ItemStack stack = inventory.getStackInSlot(0);
                if (stack.is(CRItems.RIPEN_MATTER.get())) {
                    if (!corps.isEmpty()) {
                        stack.shrink(1);
                        int count = getProcessingLevel();
                        while (count > 0) {
                            BlockPos corpPos = corps.get(level.random.nextInt(0, corps.size()));
                            BlockState corpState = level.getBlockState(corpPos);
                            if (corpState.getBlock() instanceof BonemealableBlock growable
                                    && growable.isValidBonemealTarget(level, corpPos, corpState, false)
                                    && ForgeHooks.onCropsGrowPre(level, corpPos, corpState, true)
                            ) {
                                growable.performBonemeal((ServerLevel) level, level.random, corpPos, corpState);
                                level.levelEvent(2005, corpPos, 0);
                                ForgeHooks.onCropsGrowPost(level, corpPos, corpState);
                            }
                            count--;
                        }
                    }
                } else if (stack.is(CRItems.MATURE_MATTER.get())) {
                    if (!adultAnimals.isEmpty() || !babyAnimals.isEmpty()) {
                        stack.shrink(1);
                        int count = getProcessingLevel();
                        while (count > 0) {
                            boolean tryAdultFirst = level.random.nextBoolean();

                            if (tryAdultFirst) {
                                if (!adultAnimals.isEmpty()) {
                                    Animal target = adultAnimals.get(level.random.nextInt(adultAnimals.size()));
                                    if (!target.isInLove()) {
                                        target.setInLove(null);
                                    }
                                } else if (!babyAnimals.isEmpty()) {
                                    AgeableMob target = babyAnimals.get(level.random.nextInt(babyAnimals.size()));
                                    target.ageUp(Animal.getSpeedUpSecondsWhenFeeding(-target.getAge()), true);
                                }
                            } else {
                                if (!babyAnimals.isEmpty()) {
                                    AgeableMob target = babyAnimals.get(level.random.nextInt(babyAnimals.size()));
                                    target.ageUp(Animal.getSpeedUpSecondsWhenFeeding(-target.getAge()), true);
                                } else if (!adultAnimals.isEmpty()) {
                                    Animal target = adultAnimals.get(level.random.nextInt(adultAnimals.size()));
                                    if (!target.isInLove()) {
                                        target.setInLove(null);
                                    }
                                }
                            }
                            count--;
                        }
                    }
                }
            }
        }
    }

    private class SpreaderInventoryHandler extends CombinedInvWrapper {
        public SpreaderInventoryHandler() {
            super(new IItemHandlerModifiable[]{SpreaderBlockEntity.this.inventory});
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(CRItems.RIPEN_MATTER.get()) || stack.is(CRItems.MATURE_MATTER.get());
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!this.isItemValid(slot, stack))
                return stack;
            return super.insertItem(slot, stack, simulate);
        }

    }

    @Override
    public float calculateStressApplied() {
        return 2.0f;
    }

    public int getParticleColor() {
        var stack = inventory.getStackInSlot(0);
        if (stack.is(CRItems.MATURE_MATTER.get())) {
            return 0xc53500;
        } else if (stack.is(CRItems.RIPEN_MATTER.get())) {
            return 0xa1bd61;
        } else {
            return 0xffffff;
        }
    }
}
