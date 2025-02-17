package tfar.brewingcauldron;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraftforge.items.SlotItemHandler;

public class BrewingCauldronMenu extends AbstractContainerMenu {

    private final Slot ingredientSlot;

    public BrewingCauldronMenu(int containerId, Inventory inventory) {
        this(containerId,inventory,new BrewingHandler(4),new SimpleContainerData(2));
    }

    public BrewingCauldronMenu(int containerId, Inventory inventory,BrewingHandler handler, ContainerData data) {
        super(Init.ModMenuTypes.BREWING_CAULDRON, containerId);


        this.addSlot(new SlotItemHandler(handler, 0, 56, 51));
        this.addSlot(new SlotItemHandler(handler, 1, 79, 58));
        this.addSlot(new SlotItemHandler(handler, 2, 102, 51));
        this.ingredientSlot = this.addSlot(new SlotItemHandler(handler, 3, 79, 17));
        this.addSlot(new SlotItemHandler(handler, 4, 17, 17));


        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
        }

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;

    }
}
