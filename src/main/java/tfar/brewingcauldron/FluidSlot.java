package tfar.brewingcauldron;

import net.minecraftforge.fluids.FluidStack;

public class FluidSlot {
    private final int slot;
    public final IFluidHandlerModifiable inventory;
    public int index;
    public final int x;
    public final int y;

    public FluidSlot(IFluidHandlerModifiable inventory, int pSlot, int pX, int pY) {
        this.inventory = inventory;
        this.slot = pSlot;
        this.x = pX;
        this.y = pY;
    }

    public boolean mayPlace(FluidStack stack) {
        return true;
    }

    public FluidStack getFluid() {
        return inventory.getFluidInTank(slot);
    }

    public boolean hasFluid() {
        return !getFluid().isEmpty();
    }


    public void setFluid(FluidStack pStack) {
        this.inventory.setFluidInSlot(this.slot, pStack);
        this.setChanged();
    }

    /**
     * Called when the stack in a Slot changes
     */
    public void setChanged() {
        //this.inventory.setDirty();
    }
}
