package tfar.brewingcauldron;

import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SimpleFluidHandler implements IFluidHandlerModifiable {

    private final int slots;

    public SimpleFluidHandler(int slots) {
        this.slots = slots;
    }

    FluidStack stack = FluidStack.EMPTY;

    @Override
    public void setFluidInSlot(int slot, @NotNull FluidStack stack) {
        this.stack = stack;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return stack;
    }

    @Override
    public int getTankCapacity(int tank) {
        return FluidAttributes.BUCKET_VOLUME;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return null;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return null;
    }

    @Override
    public List<FluidStack> getFluids() {
        return List.of(stack);
    }
}
