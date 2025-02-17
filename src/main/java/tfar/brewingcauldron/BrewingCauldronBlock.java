package tfar.brewingcauldron;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class BrewingCauldronBlock extends AbstractCauldronBlock {
    public BrewingCauldronBlock(Properties pProperties, Map<Item, CauldronInteraction> pInteractions) {
        super(pProperties, pInteractions);
    }

    @Override
    public boolean isFull(BlockState pState) {
        return false;
    }
}
