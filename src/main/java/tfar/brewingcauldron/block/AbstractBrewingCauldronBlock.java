package tfar.brewingcauldron.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import org.jetbrains.annotations.Nullable;
import tfar.brewingcauldron.BrewingCauldronBlockEntity;
import tfar.brewingcauldron.BrewingCauldronConfig;
import tfar.brewingcauldron.BrewingHandler;

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
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        InteractionResult use = super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);

        if (use == InteractionResult.PASS) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof BrewingCauldronBlockEntity BCBE) {
                ItemStack itemstack = pPlayer.getItemInHand(pHand);
                if (BrewingStandMenu.FuelSlot.mayPlaceItem(itemstack) && BCBE.handler.getFuel().isEmpty()) {
                    if (!pLevel.isClientSide) {
                        BCBE.handler.setFuel(itemstack);
                        pPlayer.setItemInHand(pHand, ItemStack.EMPTY);
                    }
                    return InteractionResult.sidedSuccess(pLevel.isClientSide);
                }

                if (BrewingRecipeRegistry.isValidIngredient(itemstack) && BCBE.handler.getIngredient().isEmpty()) {
                    if (!pLevel.isClientSide) {
                        ItemStack remaining = BCBE.handler.insertItem(BrewingHandler.INGREDIENT,itemstack,false);
                        pPlayer.setItemInHand(pHand, remaining);
                    }
                    return InteractionResult.sidedSuccess(pLevel.isClientSide);
                }

                if (itemstack.isEmpty()&& !BCBE.handler.getIngredient().isEmpty() && pPlayer.isCrouching()) {
                    if (!pLevel.isClientSide) {
                        pPlayer.setItemInHand(pHand, BCBE.handler.getIngredient());
                        BCBE.handler.setIngredient(ItemStack.EMPTY);
                    }
                    return InteractionResult.sidedSuccess(pLevel.isClientSide);
                }
            }

            if (BrewingCauldronConfig.INSTANCE.HAS_GUI.get()) {
                if (!pLevel.isClientSide) {
                    pPlayer.openMenu(getMenuProvider(pState, pLevel, pPos));
                }
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            }
        }

        return use;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.hasBlockEntity() && (!(pNewState.getBlock() instanceof AbstractBrewingCauldronBlock)) || !pNewState.hasBlockEntity()) {
            pLevel.removeBlockEntity(pPos);
        }
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return (BrewingCauldronBlockEntity)pLevel.getBlockEntity(pPos);
    }

    @Override
    protected double getContentHeight(BlockState pState) {
        return super.getContentHeight(pState);
    }

    @Override
    public boolean isFull(BlockState pState) {
        return false;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? null :(pLevel1, pPos, pState1, pBlockEntity) -> BrewingCauldronBlockEntity.tickStatic(pLevel1, pPos, pState1, (BrewingCauldronBlockEntity)pBlockEntity);
    }
}
