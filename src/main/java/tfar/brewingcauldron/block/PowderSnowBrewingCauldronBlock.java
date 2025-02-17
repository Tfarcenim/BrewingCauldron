package tfar.brewingcauldron.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Predicate;

public class PowderSnowBrewingCauldronBlock extends WaterBrewingCauldronBlock{

    public PowderSnowBrewingCauldronBlock(Properties pProperties, Predicate<Biome.Precipitation> pFillPredicate, Map<Item, CauldronInteraction> pInteractions) {
        super(pProperties, pFillPredicate, pInteractions);
    }

    protected void handleEntityOnFireInside(BlockState p_154294_, Level p_154295_, BlockPos p_154296_) {
        lowerFillLevel(Blocks.WATER_CAULDRON.defaultBlockState().setValue(LEVEL, p_154294_.getValue(LEVEL)), p_154295_, p_154296_);
    }
}
