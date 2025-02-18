package tfar.brewingcauldron;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PotionUtils2 {
    public static CompoundTag saveAllEffects(CompoundTag tag, Potion potion, List<MobEffectInstance> customEffects, Integer color) {
        saveAllEffects(tag, potion, customEffects);
        if (color != null) {
            saveColor(tag,color);
        }
        return tag;
    }

    public static CompoundTag saveAllEffects(CompoundTag tag, Potion potion, List<MobEffectInstance> customEffects) {
        savePotion(tag,potion);
        saveCustomEffects(tag,customEffects);
        return tag;
    }

    private static CompoundTag savePotion(CompoundTag tag, Potion potion) {
        tag.putString(PotionUtils.TAG_POTION, Registry.POTION.getKey(potion).toString());
        return tag;
    }

    private static CompoundTag saveCustomEffects(CompoundTag tag, Collection<MobEffectInstance> pEffects) {
        if (!pEffects.isEmpty()) {
            ListTag listtag = tag.getList(PotionUtils.TAG_CUSTOM_POTION_EFFECTS, 9);
            for (MobEffectInstance mobeffectinstance : pEffects) {
                listtag.add(mobeffectinstance.save(new CompoundTag()));
            }
            tag.put(PotionUtils.TAG_CUSTOM_POTION_EFFECTS, listtag);
        }
        return tag;
    }

    private static CompoundTag saveColor(CompoundTag tag, int color) {
        tag.putInt(PotionUtils.TAG_CUSTOM_POTION_COLOR,color);
        return tag;
    }

    /**
     * Gets the integer color of an {@code ItemStack} as defined by it's stored potion color tag
     * @param pStack the passed {@code ItemStack}
     */
    public static int getColor(CompoundTag tag) {
        if (tag != null && tag.contains("CustomPotionColor", 99)) {
            return tag.getInt("CustomPotionColor");
        } else {
            return PotionUtils.getPotion(tag) == Potions.EMPTY ? 0xf800f8 : PotionUtils.getColor(PotionUtils.getAllEffects(tag));
        }
    }


    private static final Component NO_EFFECT = (new TranslatableComponent("effect.none")).withStyle(ChatFormatting.GRAY);

    /**
     * Adds the tooltip of the {@code Potion} stored on the {@code ItemStack} along a "durationFactor"
     * @param pStack the passed {@code ItemStack}
     * @param pTooltips the passed list of current {@code Component} tooltips
     * @param pDurationFactor the passed durationFactor of the {@code Potion}
     */
    public static void addPotionTooltip(CompoundTag tag, List<Component> pTooltips, float pDurationFactor) {
        List<MobEffectInstance> list = PotionUtils.getAllEffects(tag);
        List<Pair<Attribute, AttributeModifier>> list1 = Lists.newArrayList();
        if (list.isEmpty()) {
            pTooltips.add(NO_EFFECT);
        } else {
            for(MobEffectInstance mobeffectinstance : list) {
                MutableComponent mutablecomponent = new TranslatableComponent(mobeffectinstance.getDescriptionId());
                MobEffect mobeffect = mobeffectinstance.getEffect();
                Map<Attribute, AttributeModifier> map = mobeffect.getAttributeModifiers();
                if (!map.isEmpty()) {
                    for(Map.Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                        AttributeModifier attributemodifier = entry.getValue();
                        AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), mobeffect.getAttributeModifierValue(mobeffectinstance.getAmplifier(), attributemodifier), attributemodifier.getOperation());
                        list1.add(new Pair<>(entry.getKey(), attributemodifier1));
                    }
                }

                if (mobeffectinstance.getAmplifier() > 0) {
                    mutablecomponent = new TranslatableComponent("potion.withAmplifier", mutablecomponent, new TranslatableComponent("potion.potency." + mobeffectinstance.getAmplifier()));
                }

                if (mobeffectinstance.getDuration() > 20) {
                    mutablecomponent = new TranslatableComponent("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(mobeffectinstance, pDurationFactor));
                }

                pTooltips.add(mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting()));
            }
        }

        if (!list1.isEmpty()) {
            pTooltips.add(TextComponent.EMPTY);
            pTooltips.add((new TranslatableComponent("potion.whenDrank")).withStyle(ChatFormatting.DARK_PURPLE));

            for(Pair<Attribute, AttributeModifier> pair : list1) {
                AttributeModifier attributemodifier2 = pair.getSecond();
                double d0 = attributemodifier2.getAmount();
                double d1;
                if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    d1 = attributemodifier2.getAmount();
                } else {
                    d1 = attributemodifier2.getAmount() * 100.0D;
                }

                if (d0 > 0.0D) {
                    pTooltips.add((new TranslatableComponent("attribute.modifier.plus." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(pair.getFirst().getDescriptionId()))).withStyle(ChatFormatting.BLUE));
                } else if (d0 < 0.0D) {
                    d1 *= -1.0D;
                    pTooltips.add((new TranslatableComponent("attribute.modifier.take." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(pair.getFirst().getDescriptionId()))).withStyle(ChatFormatting.RED));
                }
            }
        }
    }

    public static boolean haveSameEffects(ItemStack stack, FluidStack fluidStack) {
        Potion itemPotion = PotionUtils.getPotion(stack);
        Potion fluidPotion = PotionUtils.getPotion(fluidStack.getTag());

        List<MobEffectInstance> customEffectsItem = PotionUtils.getCustomEffects(stack);
        List<MobEffectInstance> customEffectsFluid = PotionUtils.getCustomEffects(fluidStack.getTag());

        return (itemPotion == fluidPotion && Objects.equals(customEffectsItem,customEffectsFluid));

    }

}
