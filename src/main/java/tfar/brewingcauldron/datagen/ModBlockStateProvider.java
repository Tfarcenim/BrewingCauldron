package tfar.brewingcauldron.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import tfar.brewingcauldron.BrewingCauldron;
import tfar.brewingcauldron.Init;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, BrewingCauldron.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(Init.ModBlocks.BREWING_CAULDRON,models().getExistingFile(mcLoc("block/cauldron")));
    }
}
