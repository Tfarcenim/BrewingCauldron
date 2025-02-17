package tfar.brewingcauldron.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import tfar.brewingcauldron.BrewingCauldron;

import javax.annotation.Nullable;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(final DataGenerator generatorIn, @Nullable final ExistingFileHelper existingFileHelper) {
        super(generatorIn, BrewingCauldron.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {

    }
}
