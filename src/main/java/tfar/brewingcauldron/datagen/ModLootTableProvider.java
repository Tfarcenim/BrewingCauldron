package tfar.brewingcauldron.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import tfar.brewingcauldron.Init;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(Pair.of(ModEntityLootTables::new, LootContextParamSets.BLOCK));
    }

    public static class ModEntityLootTables extends BlockLoot {

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(Init.ModBlocks.BREWING_CAULDRON,
                    Init.ModBlocks.WATER_BREWING_CAULDRON,
                    Init.ModBlocks.LAVA_BREWING_CAULDRON,
                    Init.ModBlocks.POWDER_SNOW_BREWING_CAULDRON);
        }

        @Override
        protected void addTables() {
            dropSelf(Init.ModBlocks.BREWING_CAULDRON);
            this.dropOther(Init.ModBlocks.WATER_BREWING_CAULDRON, Init.ModBlocks.BREWING_CAULDRON);
            this.dropOther(Init.ModBlocks.LAVA_BREWING_CAULDRON, Init.ModBlocks.BREWING_CAULDRON);
            this.dropOther(Init.ModBlocks.POWDER_SNOW_BREWING_CAULDRON, Init.ModBlocks.BREWING_CAULDRON);
        }
    }
}
