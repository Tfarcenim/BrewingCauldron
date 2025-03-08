package tfar.brewingcauldron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfar.brewingcauldron.block.WaterBrewingCauldronBlock;

import javax.annotation.Nonnull;
import java.util.List;

public class BrewingCauldronBlockEntity extends BlockEntity implements MenuProvider {

    int brewTime;
    int fuel;
    private Item ingredient;

    boolean tryFillBottle;

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int p_59038_) {
            return switch (p_59038_) {
                case 0 -> brewTime;
                case 1 -> fuel;
                case 2 -> handler.bottles;
                default -> 0;
            };
        }

        public void set(int p_59040_, int value) {
            switch(p_59040_) {
                case 0:
                    brewTime = value;
                    break;
                case 1:
                    fuel = value;
                case 2:
                    handler.bottles = value;
            }

        }

        public int getCount() {
            return 3;
        }
    };

    public static void tickStatic(Level pLevel1, BlockPos pPos, BlockState pState1, BrewingCauldronBlockEntity pBlockEntity) {
        pBlockEntity.serverTick();
    }

    public void serverTick() {
        ItemStack itemstack = handler.getStackInSlot(BrewingHandler.FUEL);
        if (fuel <= 0 && itemstack.is(Items.BLAZE_POWDER)) {
            fuel = 20;
            itemstack.shrink(1);
            setChanged();
        }

        boolean flag = isBrewable();
        boolean flag1 = brewTime > 0;
        ItemStack itemstack1 = handler.getStackInSlot(BrewingHandler.INGREDIENT);
        if (flag1) {
            ((ServerLevel)level).sendParticles(ParticleTypes.BUBBLE, (double)worldPosition.getX() + level.random.nextDouble(), (double)(worldPosition.getY() + 1), (double)worldPosition.getZ() + level.random.nextDouble(), 1, 0.0D, 0.01D, 0.0D, 0.2D);
            --brewTime;
            boolean flag2 = brewTime == 0;
            if (flag2 && flag) {
                doBrew();
                setChanged();
            } else if (!flag || !itemstack1.is(ingredient)) {
                brewTime = 0;
                setChanged();
            }
        } else if (flag && fuel > 0) {
            --fuel;
            brewTime = 400;
            ingredient = itemstack1.getItem();
            setChanged();
        }

        if (tryFillBottle) {
            tryBottles();
            tryFillBottle = false;
        }

           // level.setBlock(worldPosition, blockstate, 2);

    }

    protected void tryBottles() {
        tryFill();
    }

    //bottle -> cauldron
    protected void tryFill() {
        ItemStack input = handler.getStackInSlot(BrewingHandler.BOTTLE_INPUT);
        if (!input.isEmpty()) {
            ItemStack output = handler.getStackInSlot(BrewingHandler.BOTTLE_OUTPUT);
            if (output.isEmpty()) {
                FluidStack fluidStack = handler.fluidStack;
                //empty cauldron
                if (fluidStack.isEmpty()) {
                    if (input.is(Items.POTION)) {
                        handler.bottles++;
                        handler.setFluidInSlot(0,new FluidStack(Init.ModFluids.POTION,FluidAttributes.BUCKET_VOLUME,input.getTag()));
                        handler.extractItem(BrewingHandler.BOTTLE_INPUT,1,false);
                        handler.setStackInSlot(BrewingHandler.BOTTLE_OUTPUT,new ItemStack(Items.GLASS_BOTTLE));
                    }
                } else {
                    //empty glass bottles
                    if (input.is(Items.GLASS_BOTTLE)) {
                        if (fluidStack.getFluid() == Fluids.WATER || fluidStack.getFluid() == Init.ModFluids.POTION) {
                            ItemStack potionStack = Items.POTION.getDefaultInstance();
                            if (fluidStack.hasTag()) {
                                potionStack.setTag(fluidStack.getTag());
                            }
                            handler.setStackInSlot(BrewingHandler.BOTTLE_OUTPUT,potionStack);
                            handler.extractItem(BrewingHandler.BOTTLE_INPUT,1,false);
                            handler.bottles--;
                            if (handler.bottles <= 0) {
                                handler.setFluidInSlot(0,FluidStack.EMPTY);
                            } else {
                                setChanged();
                            }
                        }
                    } else if (input.is(Items.POTION)) {
                        if (fluidStack.getFluid() == Fluids.WATER || fluidStack.getFluid() == Init.ModFluids.POTION) {
                            if (handler.bottles < 3 && PotionUtils2.haveSameEffects(input,fluidStack) || fluidStack.getFluid() == Fluids.WATER && PotionUtils.getPotion(input) == Potions.WATER) {
                                handler.bottles++;
                                handler.extractItem(BrewingHandler.BOTTLE_INPUT, 1, false);
                                handler.setStackInSlot(BrewingHandler.BOTTLE_OUTPUT, new ItemStack(Items.GLASS_BOTTLE));
                            }
                        }
                    }
                }
            } else {

            }
        }
    }

    private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, 4};


    private void doBrew() {
        NonNullList<ItemStack> wrapper = makeWrapper();
        if (net.minecraftforge.event.ForgeEventFactory.onPotionAttemptBrew(wrapper)) return;
        ItemStack ingredient = wrapper.get(3);

        net.minecraftforge.common.brewing.BrewingRecipeRegistry.brewPotions(wrapper, ingredient, SLOTS_FOR_SIDES);
        net.minecraftforge.event.ForgeEventFactory.onPotionBrewed(wrapper);
        if (ingredient.hasContainerItem()) {
            ItemStack itemstack1 = ingredient.getContainerItem();
            ingredient.shrink(1);
            if (ingredient.isEmpty()) {
                ingredient = itemstack1;
            } else {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), itemstack1);
            }
        }
        else ingredient.shrink(1);
        wrapper.set(3, ingredient);

        ItemStack stack = wrapper.get(0);
        FluidStack fluidStack = new FluidStack(Init.ModFluids.POTION, FluidAttributes.BUCKET_VOLUME);
        fluidStack.setTag(stack.getTag());

        handler.setFluidInSlot(0,fluidStack);

        handler.setStackInSlot(2,ingredient);

        level.levelEvent(LevelEvent.SOUND_BREWING_STAND_BREW, worldPosition, 0);
    }

    boolean isBrewable(){
        FluidStack fluidStack = handler.fluidStack;
        if (fluidStack.getFluid() == Fluids.WATER || fluidStack.getFluid() == Init.ModFluids.POTION) {
            Potion potion;
            if (fluidStack.getFluid() == Init.ModFluids.POTION) {
                potion = PotionUtils.getPotion(fluidStack.getTag());
            } else {
                 potion= Potions.WATER;
            }

            ItemStack potionStack = PotionUtils.setPotion(Items.POTION.getDefaultInstance(),potion);
            return BrewingRecipeRegistry.hasOutput(potionStack,handler.getStackInSlot(BrewingHandler.INGREDIENT));
        } else {
            return false;
        }
    }

    protected NonNullList<ItemStack> makeWrapper() {
        NonNullList<ItemStack> stacks = NonNullList.withSize(5,ItemStack.EMPTY);
        FluidStack fluidStack = handler.fluidStack;
        ItemStack potionStack = ItemStack.EMPTY;
        if (fluidStack.getFluid() == Fluids.WATER || fluidStack.getFluid() == Init.ModFluids.POTION) {
            Potion potion;
            if (fluidStack.getFluid() == Init.ModFluids.POTION) {
                potion = PotionUtils.getPotion(fluidStack.getTag());
            } else {
                potion = Potions.WATER;
            }
            potionStack = PotionUtils.setPotion(Items.POTION.getDefaultInstance(),potion);
        }

        if (!potionStack.isEmpty()) {
            int potionLevel = getBlockState().getValue(WaterBrewingCauldronBlock.LEVEL);
            for (int i = 0; i < potionLevel;i++) {
                stacks.set(i,potionStack);
            }
        }

        stacks.set(3,handler.getIngredient());
        stacks.set(4,handler.getFuel());

        return stacks;
    }

    public void updateAppearance() {
        FluidStack stack = handler.fluidStack;
        if (stack.isEmpty() && getBlockState().getBlock() != Init.ModBlocks.BREWING_CAULDRON) {
            level.setBlock(worldPosition,Init.ModBlocks.BREWING_CAULDRON.defaultBlockState(),Block.UPDATE_ALL);
        }

        if ((stack.getFluid() == Fluids.WATER || stack.getFluid() == Init.ModFluids.POTION)) {
            level.setBlock(worldPosition,Init.ModBlocks.WATER_BREWING_CAULDRON.defaultBlockState().setValue(WaterBrewingCauldronBlock.LEVEL,handler.bottles),Block.UPDATE_ALL);
        }

        if (stack.getFluid() == Fluids.LAVA && getBlockState().getBlock() != Init.ModBlocks.LAVA_BREWING_CAULDRON) {
            level.setBlock(worldPosition,Init.ModBlocks.LAVA_BREWING_CAULDRON.defaultBlockState(),Block.UPDATE_ALL);
        }
    }

    public BrewingHandler handler = new BrewingHandler(4,this) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            tryFillBottle = true;
            setChanged();
        }
    };

    public BrewingCauldronBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(Init.ModBlockEntityTypes.BREWING_CAULDRON, pPos, pBlockState);
    }

    public BrewingCauldronBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public int getColor() {
        return handler.fluidStack.getFluid().getAttributes().getColor(handler.fluidStack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        updateAppearance();
        level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void load(CompoundTag nbt) {

        handler.deserializeNBT(nbt.getCompound("handler"));
        this.brewTime = nbt.getShort("BrewTime");
        this.fuel = nbt.getByte("Fuel");
        super.load(nbt);
        if (hasLevel())
            level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {

        compound.put("handler",handler.serializeNBT());
        compound.putShort("BrewTime", (short)this.brewTime);
        compound.putByte("Fuel", (byte)this.fuel);
        super.saveAdditional(compound);
    }

    @Override
    public Component getDisplayName() {
        return Init.ModBlocks.BREWING_CAULDRON.getName();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new BrewingCauldronMenu(pContainerId,pPlayerInventory, handler,handler,dataAccess);
    }

    AutomationWrapper wrapper = new AutomationWrapper();
    LazyOptional<AutomationWrapper> optional = LazyOptional.of(() -> wrapper);


    class AutomationWrapper implements IFluidHandlerModifiable, IItemHandlerModifiable {



        @Override
        public void setFluidInSlot(int slot, @NotNull FluidStack stack) {
            handler.setFluidInSlot(slot, stack);
        }

        @Override
        public List<FluidStack> getFluids() {
            return handler.getFluids();
        }

        @Override
        public int getTanks() {
            return handler.getTanks();
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return handler.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return handler.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return handler.isFluidValid(tank,stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return handler.fill(resource,action);
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return handler.drain(resource,action);
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return handler.drain(maxDrain, action);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            handler.setStackInSlot(slot,stack);
        }

        @Override
        public int getSlots() {
            return handler.getSlots();
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return handler.getStackInSlot(slot);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot != BrewingHandler.BOTTLE_OUTPUT) {
                return handler.insertItem(slot, stack, simulate);
            }return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == BrewingHandler.BOTTLE_OUTPUT) {
                return handler.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return handler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return handler.isItemValid(slot, stack);
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return optional.cast();
        }
        return super.getCapability(cap, side);
    }
}
