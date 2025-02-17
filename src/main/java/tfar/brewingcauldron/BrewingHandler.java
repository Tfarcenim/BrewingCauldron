package tfar.brewingcauldron;

import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class BrewingHandler extends ItemStackHandler {
    public BrewingHandler(int slots) {
        super(slots);
    }

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
}
