package tfar.brewingcauldron.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import tfar.brewingcauldron.BrewingCauldron;
import tfar.brewingcauldron.Init;

import javax.annotation.Nullable;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(final DataGenerator generatorIn, @Nullable final ExistingFileHelper existingFileHelper) {
        super(generatorIn, BrewingCauldron.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(BlockTags.CAULDRONS).add(Init.ModBlocks.BREWING_CAULDRON,Init.ModBlocks.WATER_BREWING_CAULDRON, Init.ModBlocks.LAVA_BREWING_CAULDRON,Init.ModBlocks.POWDER_SNOW_BREWING_CAULDRON);
    }
}
