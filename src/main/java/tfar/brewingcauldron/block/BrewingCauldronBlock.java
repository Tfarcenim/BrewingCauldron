package tfar.brewingcauldron.block;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;

import java.util.Map;

public class BrewingCauldronBlock extends AbstractBrewingCauldronBlock{
    public BrewingCauldronBlock(Properties pProperties, Map<Item, CauldronInteraction> pInteractions) {
        super(pProperties, pInteractions);
    }
}
