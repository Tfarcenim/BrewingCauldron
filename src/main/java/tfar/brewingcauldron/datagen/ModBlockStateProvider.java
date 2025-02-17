package tfar.brewingcauldron.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
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
        simpleBlock(Init.ModBlocks.LAVA_BREWING_CAULDRON,models().getExistingFile(mcLoc("block/lava_cauldron")));

        getVariantBuilder(Init.ModBlocks.WATER_BREWING_CAULDRON).forAllStatesExcept(state -> {

            ModelFile modelFile;

            if (state.getValue(LayeredCauldronBlock.LEVEL)<3){
                modelFile = models().getExistingFile(mcLoc("block/water_cauldron_level" + state.getValue(LayeredCauldronBlock.LEVEL)));
            } else {
                modelFile = models().getExistingFile(mcLoc("block/water_cauldron_full"));

            }


            return ConfiguredModel.builder().modelFile(modelFile).build();
        });

        getVariantBuilder(Init.ModBlocks.POWDER_SNOW_BREWING_CAULDRON).forAllStatesExcept(state -> {

            ModelFile modelFile;

            if (state.getValue(LayeredCauldronBlock.LEVEL)<3){
                modelFile = models().getExistingFile(mcLoc("block/powder_snow_cauldron_level" + state.getValue(LayeredCauldronBlock.LEVEL)));
            } else {
                modelFile = models().getExistingFile(mcLoc("block/powder_snow_cauldron_full"));

            }


            return ConfiguredModel.builder().modelFile(modelFile).build();
        });
    }
}
