package tfar.brewingcauldron;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import tfar.brewingcauldron.block.WaterBrewingCauldronBlock;

import java.util.List;
import java.util.function.Predicate;

public class BrewingHandler extends ItemStackHandler implements IFluidHandlerModifiable {

    private final BrewingCauldronBlockEntity be;
    protected FluidStack fluidStack = FluidStack.EMPTY;

    public BrewingHandler(int slots,BrewingCauldronBlockEntity be) {
        super(slots);
        this.be = be;
    }

    Predicate<FluidStack> allowed = fluidStack1 -> {
        Fluid fluid = fluidStack1.getFluid();
        return fluid == Fluids.WATER || fluid == Init.ModFluids.POTION || fluid == Fluids.LAVA;
    };

    public static final int BOTTLE_INPUT = 0;
    public static final int BOTTLE_OUTPUT = 1;
    public static final int INGREDIENT = 2;
    public static final int FUEL = 3;

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return switch (slot) {
            case 0 -> BrewingStandMenu.PotionSlot.mayPlaceItem(stack);//potion bottles
            case 1 -> BrewingStandMenu.PotionSlot.mayPlaceItem(stack);
            case 2 -> BrewingRecipeRegistry.isValidIngredient(stack);
            case 3 -> BrewingStandMenu.FuelSlot.mayPlaceItem(stack);
            default -> super.isItemValid(slot, stack);
        };
    }

    public ItemStack getIngredient() {
        return getStackInSlot(INGREDIENT);
    }

    public ItemStack getFuel() {
        return getStackInSlot(FUEL);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return fluidStack;
    }

    @Override
    public int getTankCapacity(int tank) {
        return 1000;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return allowed.test(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!isFluidValid(0,resource) || resource.getAmount() < FluidAttributes.BUCKET_VOLUME || !fluidStack.isEmpty()) {
            return 0;
        }

        if (action.execute()) {
            fluidStack = resource.copy();
            fluidStack.setAmount(FluidAttributes.BUCKET_VOLUME);
            onContentsChanged(0);
        }


        return FluidAttributes.BUCKET_VOLUME;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.getAmount() < FluidAttributes.BUCKET_VOLUME || !isFull()) {
            return FluidStack.EMPTY;
        }

        if (!fluidStack.isEmpty() && fluidStack.isFluidEqual(resource)) {
            FluidStack copy = fluidStack.copy();
            if (action.execute()) {
                setFluidInSlot(0,FluidStack.EMPTY);
            }
            return copy;
        }

        return FluidStack.EMPTY;
    }

    boolean isFull() {
        BlockState state = be.getBlockState();
        if (state.getBlock() instanceof WaterBrewingCauldronBlock) {
            return state.getValue(WaterBrewingCauldronBlock.LEVEL) == 3;
        }
        return true;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain < FluidAttributes.BUCKET_VOLUME|| !isFull()) {
            return FluidStack.EMPTY;
        }

        if (!fluidStack.isEmpty()) {
            FluidStack copy = fluidStack.copy();
            if (action.execute()) {
                setFluidInSlot(0,FluidStack.EMPTY);
            }
            return copy;
        }

        return FluidStack.EMPTY;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = super.serializeNBT();
        compoundTag.put("fluid",fluidStack.writeToNBT(new CompoundTag()));
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        fluidStack = FluidStack.loadFluidStackFromNBT(nbt.getCompound("fluid"));
    }

    @Override
    public void setFluidInSlot(int slot, @NotNull FluidStack stack) {
        fluidStack = stack;
        onContentsChanged(0);
    }

    @Override
    public List<FluidStack> getFluids() {
        return List.of(fluidStack);
    }
}
