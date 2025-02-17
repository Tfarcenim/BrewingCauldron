package tfar.brewingcauldron;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class BrewingCauldronMenu extends AbstractContainerMenu {
    protected BrewingCauldronMenu(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return false;
    }
}
