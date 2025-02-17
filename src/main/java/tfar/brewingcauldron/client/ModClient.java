package tfar.brewingcauldron.client;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tfar.brewingcauldron.BrewingCauldronBlockEntity;
import tfar.brewingcauldron.BrewingCauldronScreen;
import tfar.brewingcauldron.Init;

public class ModClient {
    public static void init(IEventBus bus){
        bus.addListener(ModClient::setup);
        bus.addListener(ModClient::blockColors);
    }

       static BlockColor color =   (state, level, pos, i) -> {
        if (level != null && pos != null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BrewingCauldronBlockEntity brewingCauldronBlockEntity) {
                return brewingCauldronBlockEntity.getColor();
            }
        }
        return  0xffffffff;
    };


    static void blockColors(ColorHandlerEvent.Block event) {
        event.getBlockColors().register(color, Init.ModBlocks.WATER_BREWING_CAULDRON);
    }

    static void setup(FMLClientSetupEvent event) {
        MenuScreens.register(Init.ModMenuTypes.BREWING_CAULDRON, BrewingCauldronScreen::new);
    }
}
