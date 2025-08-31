package tfar.brewingcauldron.item;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.jetbrains.annotations.Nullable;
import tfar.brewingcauldron.PotionFluidBucketHandler;
import tfar.brewingcauldron.PotionType;
import tfar.brewingcauldron.PotionUtils2;

import java.util.List;
import java.util.function.Supplier;

public class PotionBucketItem extends BucketItem {
    public PotionBucketItem(Fluid pContent, Properties pProperties) {
        super(pContent, pProperties);
    }

    public PotionBucketItem(Supplier<? extends Fluid> supplier, Properties builder) {
        super(supplier, builder);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(iFluidHandlerItem -> {
            PotionFluidBucketHandler potionFluidBucketHandler = (PotionFluidBucketHandler) iFluidHandlerItem;
            FluidStack fluidStack = potionFluidBucketHandler.getFluid();
            PotionType potionType = PotionUtils2.getPotionType(fluidStack);
            pTooltipComponents.add(new TranslatableComponent(PotionUtils.getPotion(pStack).getName(potionType.item.getDescriptionId() + ".effect.")));
            PotionUtils.addPotionTooltip(pStack, pTooltipComponents, 1.0F);
        });
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new PotionFluidBucketHandler(stack);
    }

    public void fillItemCategory(CreativeModeTab pGroup, NonNullList<ItemStack> pItems) {
        if (this.allowdedIn(pGroup)) {
            for(Potion potion : Registry.POTION) {
                if (potion != Potions.EMPTY) {
                    pItems.add(PotionUtils.setPotion(new ItemStack(this), potion));
                }
            }
        }
    }
}
