package tfar.brewingcauldron.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public class ModDataGenerator {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        if (event.includeServer()) {
            generator.addProvider(new ModLootTableProvider(generator));
            ModBlockTagProvider blockTags = new ModBlockTagProvider(generator, existingFileHelper);
            generator.addProvider(blockTags);
            generator.addProvider(new ModItemTagProvider(generator, blockTags, existingFileHelper));
        }
        if (event.includeClient()) {
            generator.addProvider(new ModBlockStateProvider(generator, existingFileHelper));
            generator.addProvider(new ModItemModelProvider(generator, existingFileHelper));
        }
    }
}
