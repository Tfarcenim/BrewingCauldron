package tfar.brewingcauldron;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BrewingCauldronBlockEntity extends BlockEntity {

    public BrewingCauldronBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(Init.ModBlockEntityTypes.BREWING_CAULDRON, pPos, pBlockState);
    }

    public BrewingCauldronBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }
}
