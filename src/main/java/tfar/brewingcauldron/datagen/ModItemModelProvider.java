package tfar.brewingcauldron.datagen;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.loaders.DynamicBucketModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import tfar.brewingcauldron.BrewingCauldron;
import tfar.brewingcauldron.Init;


public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, BrewingCauldron.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        makeSimpleBlockItem(Init.ModItems.BREWING_CAULDRON,mcLoc("item/cauldron"));

        withExistingParent(Init.ModItems.POTION_BUCKET.getRegistryName().getPath(), new ResourceLocation("forge","item/bucket"))
                .customLoader(DynamicBucketModelBuilder::begin)
                .fluid(Init.ModFluids.POTION)
                .applyTint(false)
                .end();
    }

    private void generatedItem(String path) {
        singleTexture(path, new ResourceLocation("item/generated"),
                "layer0", modLoc("item/" + path));
    }

    protected void makeSimpleBlockItem(Item item, ResourceLocation loc) {
        String s = Registry.ITEM.getKey(item).toString();
        getBuilder(s)
                .parent(getExistingFile(loc));
    }

    protected void makeSimpleBlockItem(Item item) {
        makeSimpleBlockItem(item, modLoc("block/" + Registry.ITEM.getKey(item).getPath()));
    }

    private void generatedItem(RegistryObject<? extends Item> item) {
        generatedItem(item.getId().getPath());
    }

}
