package tfar.brewingcauldron;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BrewingCauldronBlockEntity extends BlockEntity implements MenuProvider {

    protected Potion potion = Potions.EMPTY;
    protected Integer customPotionColor;
    protected List<MobEffectInstance> customEffects = new ArrayList<>();

    int brewTime;
    int fuel;
    private Item ingredient;

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int p_59038_) {
            return switch (p_59038_) {
                case 0 -> brewTime;
                case 1 -> fuel;
                default -> 0;
            };
        }

        public void set(int p_59040_, int p_59041_) {
            switch(p_59040_) {
                case 0:
                    brewTime = p_59041_;
                    break;
                case 1:
                    fuel = p_59041_;
            }

        }

        public int getCount() {
            return 2;
        }
    };

    public static void tickStatic(Level pLevel1, BlockPos pPos, BlockState pState1, BrewingCauldronBlockEntity pBlockEntity) {
        pBlockEntity.serverTick();
    }

    public void serverTick() {
        ItemStack itemstack = itemStackHandler.getStackInSlot(BrewingHandler.FUEL);
        if (fuel <= 0 && itemstack.is(Items.BLAZE_POWDER)) {
            fuel = 20;
            itemstack.shrink(1);
            setChanged();
        }

        boolean flag = isBrewable(pBlockEntity.items);
        boolean flag1 = pBlockEntity.brewTime > 0;
        ItemStack itemstack1 = pBlockEntity.items.get(3);
        if (flag1) {
            --brewTime;
            boolean flag2 = brewTime == 0;
            if (flag2 && flag) {
                doBrew(pLevel, pPos, pBlockEntity.items);
                setChanged(pLevel, pPos, pState);
            } else if (!flag || !itemstack1.is(pBlockEntity.ingredient)) {
                pBlockEntity.brewTime = 0;
                setChanged(pLevel, pPos, pState);
            }
        } else if (flag && pBlockEntity.fuel > 0) {
            --fuel;
            brewTime = 400;
            ingredient = itemstack1.getItem();
            setChanged();
        }

        boolean[] aboolean = pBlockEntity.getPotionBits();
        if (!Arrays.equals(aboolean, pBlockEntity.lastPotionCount)) {
            pBlockEntity.lastPotionCount = aboolean;
            BlockState blockstate = pState;
            if (!(pState.getBlock() instanceof BrewingStandBlock)) {
                return;
            }


           // level.setBlock(worldPosition, blockstate, 2);
        }
    }

    boolean isBrewable(){

    }

    protected BrewingHandler itemStackHandler = new BrewingHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    public BrewingCauldronBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(Init.ModBlockEntityTypes.BREWING_CAULDRON, pPos, pBlockState);
    }

    public BrewingCauldronBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }
    @Nonnull
    public Potion getPotion() {
        return potion;
    }

    public void setPotion(@Nonnull Potion potion) {
        this.potion = potion;
        setChanged();
    }

    public List<MobEffectInstance> getCustomEffects() {
        return customEffects;
    }

    public void setCustomEffects(List<MobEffectInstance> customEffects) {
        this.customEffects = customEffects;
        setChanged();
    }

    public int getColor() {
        if (customPotionColor != null) {
            return customPotionColor;
        }
        else if (potion == Potions.WATER) {
            return BiomeColors.getAverageWaterColor(level, worldPosition);
        } else {
            return PotionUtils.getColor(potion);
        }
    }

    @Nullable
    public Integer getCustomPotionColor() {
        return customPotionColor;
    }

    public void setCustomPotionColor(@Nullable Integer customPotionColor) {
        this.customPotionColor = customPotionColor;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void load(CompoundTag nbt) {
        potion = PotionUtils.getPotion(nbt);
        customEffects = PotionUtils.getCustomEffects(nbt);
        if (nbt.contains(PotionUtils.TAG_CUSTOM_POTION_COLOR)) {
            customPotionColor = nbt.getInt(PotionUtils.TAG_CUSTOM_POTION_COLOR);
        }
        super.load(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        PotionUtils2.saveAllEffects(compound, potion, customEffects,customPotionColor);
        super.saveAdditional(compound);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new BrewingCauldronMenu(pContainerId,pPlayerInventory,itemStackHandler,dataAccess);
    }
}
