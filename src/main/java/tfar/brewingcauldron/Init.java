package tfar.brewingcauldron;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Map;

public class Init {

    public static class ModItems{
        public static final Item BREWING_CAULDRON = new BlockItem(ModBlocks.BREWING_CAULDRON,new Item.Properties().tab(CreativeModeTab.TAB_BREWING));
    }

    public static class ModBlockEntityTypes {
        public static final BlockEntityType<BrewingCauldronBlockEntity> BREWING_CAULDRON = BlockEntityType.Builder
                .of(BrewingCauldronBlockEntity::new,ModBlocks.BREWING_CAULDRON).build(null);
    }

    public static class ModBlocks {
        public static final Block BREWING_CAULDRON = new BrewingCauldronBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON), Map.of());
    }

}
