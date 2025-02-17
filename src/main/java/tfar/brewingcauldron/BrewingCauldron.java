package tfar.brewingcauldron;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import tfar.brewingcauldron.block.CauldronInteractions;
import tfar.brewingcauldron.datagen.ModDataGenerator;


@Mod(BrewingCauldron.MOD_ID)
public class BrewingCauldron {
    public static final String MOD_ID = "brewingcauldron";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public BrewingCauldron() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        bus.addListener(this::setup);
        bus.addGenericListener(Block.class,this::registerBlocks);
        bus.addGenericListener(BlockEntityType.class,this::registerBlockEntities);
        bus.addGenericListener(Item.class,this::registerItems);

        bus.addListener(ModDataGenerator::gatherData);
        MinecraftForge.EVENT_BUS.addListener(this::rightClick);
    }

    public static ResourceLocation id(String s) {
        return new ResourceLocation(MOD_ID,s);
    }

    void rightClick(PlayerInteractEvent.EntityInteract event) {
    }

    void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                Init.ModBlocks.BREWING_CAULDRON.setRegistryName("brewing_cauldron"),
                Init.ModBlocks.WATER_BREWING_CAULDRON.setRegistryName("water_brewing_cauldron"),
                Init.ModBlocks.LAVA_BREWING_CAULDRON.setRegistryName("lava_brewing_cauldron"),
                Init.ModBlocks.POWDER_SNOW_BREWING_CAULDRON.setRegistryName("powder_snow_brewing_cauldron"));
    }

    void registerBlockEntities(RegistryEvent.Register<BlockEntityType<?>> event) {

    }

    void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                Init.ModItems.BREWING_CAULDRON.setRegistryName("brewing_cauldron")
        );
    }

    private void setup(final FMLCommonSetupEvent event) {
        CauldronInteractions.bootStrap();
    }
}
