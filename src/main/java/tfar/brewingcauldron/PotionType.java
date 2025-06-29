package tfar.brewingcauldron;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum PotionType {
    REGULAR(Items.POTION), SPLASH(Items.SPLASH_POTION), LINGERING(Items.LINGERING_POTION);

    final Item item;

    PotionType(Item item) {
        this.item = item;
    }

    public boolean isValid(Item item) {
        return item == this.item;
    }

    public static PotionType fromItem(Item item) {
        if (item == Items.POTION) return REGULAR;
        else if (item == Items.SPLASH_POTION) return SPLASH;
        else if (item == Items.LINGERING_POTION) return LINGERING;
        return null;
    }
}
