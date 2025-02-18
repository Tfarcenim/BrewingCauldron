package tfar.brewingcauldron;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import java.util.Collection;
import java.util.List;

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
}
