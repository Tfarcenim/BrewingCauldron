package tfar.brewingcauldron.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tfar.brewingcauldron.BrewingCauldronBlockEntity;

import java.util.Map;

public abstract class AbstractBrewingCauldronBlock extends AbstractCauldronBlock implements EntityBlock {
    public AbstractBrewingCauldronBlock(Properties pProperties, Map<Item, CauldronInteraction> pInteractions) {
        super(pProperties, pInteractions);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BrewingCauldronBlockEntity(pPos,pState);
    }

    @Override
    protected double getContentHeight(BlockState pState) {
        return super.getContentHeight(pState);
    }

    @Override
    public boolean isFull(BlockState pState) {
        return false;
    }
}
