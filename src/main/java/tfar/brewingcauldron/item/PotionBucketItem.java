package tfar.brewingcauldron.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import tfar.brewingcauldron.PotionFluidBucketHandler;

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
        pTooltipComponents.add(new TranslatableComponent(PotionUtils.getPotion(pStack).getName(Items.POTION.getDescriptionId() + ".effect.")));
        PotionUtils.addPotionTooltip(pStack, pTooltipComponents, 1.0F);

    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new PotionFluidBucketHandler(stack);
    }
}
