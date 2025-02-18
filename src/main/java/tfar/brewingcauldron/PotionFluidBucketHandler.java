package tfar.brewingcauldron;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.NotNull;

public class PotionFluidBucketHandler extends FluidBucketWrapper {
    public PotionFluidBucketHandler(@NotNull ItemStack container) {
        super(container);
    }

    @NotNull
    @Override
    public FluidStack getFluid() {
        FluidStack fluid = super.getFluid();
        if (fluid.getFluid() == Init.ModFluids.POTION) {
            fluid.setTag(container.getTag());
        }
        return fluid;
    }
}
