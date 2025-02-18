package tfar.brewingcauldron;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
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
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import tfar.brewingcauldron.block.WaterBrewingCauldronBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
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
        ItemStack itemstack = handler.getStackInSlot(BrewingHandler.FUEL);
        if (fuel <= 0 && itemstack.is(Items.BLAZE_POWDER)) {
            fuel = 20;
            itemstack.shrink(1);
            setChanged();
        }

        boolean flag = isBrewable();
        boolean flag1 = brewTime > 0;
        ItemStack itemstack1 = handler.getStackInSlot(BrewingHandler.INGREDIENT);
        if (flag1) {
            --brewTime;
            boolean flag2 = brewTime == 0;
            if (flag2 && flag) {
                doBrew();
                setChanged();
            } else if (!flag || !itemstack1.is(ingredient)) {
                brewTime = 0;
                setChanged();
            }
        } else if (flag && fuel > 0) {
            --fuel;
            brewTime = 400;
            ingredient = itemstack1.getItem();
            setChanged();
        }

           // level.setBlock(worldPosition, blockstate, 2);

    }

    private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, 4};


    private void doBrew() {
        NonNullList<ItemStack> wrapper = makeWrapper();
        if (net.minecraftforge.event.ForgeEventFactory.onPotionAttemptBrew(wrapper)) return;
        ItemStack ingredient = wrapper.get(3);

        net.minecraftforge.common.brewing.BrewingRecipeRegistry.brewPotions(wrapper, ingredient, SLOTS_FOR_SIDES);
        net.minecraftforge.event.ForgeEventFactory.onPotionBrewed(wrapper);
        if (ingredient.hasContainerItem()) {
            ItemStack itemstack1 = ingredient.getContainerItem();
            ingredient.shrink(1);
            if (ingredient.isEmpty()) {
                ingredient = itemstack1;
            } else {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), itemstack1);
            }
        }
        else ingredient.shrink(1);
        wrapper.set(3, ingredient);

        ItemStack stack = wrapper.get(0);
        FluidStack fluidStack = new FluidStack(Init.ModFluids.POTION, FluidAttributes.BUCKET_VOLUME);
        fluidStack.setTag(stack.getTag());

        handler.setFluidInSlot(0,fluidStack);

        handler.setStackInSlot(2,ingredient);

        level.levelEvent(LevelEvent.SOUND_BREWING_STAND_BREW, worldPosition, 0);
    }

    boolean isBrewable(){
        FluidStack fluidStack = handler.fluidStack;
        if (fluidStack.getFluid() == Fluids.WATER || fluidStack.getFluid() == Init.ModFluids.POTION) {
            Potion potion;
            if (fluidStack.getFluid() == Init.ModFluids.POTION) {
                potion = PotionUtils.getPotion(fluidStack.getTag());
            } else {
                 potion= Potions.WATER;
            }

            ItemStack potionStack = PotionUtils.setPotion(Items.POTION.getDefaultInstance(),potion);
            return BrewingRecipeRegistry.hasOutput(potionStack,handler.getStackInSlot(BrewingHandler.INGREDIENT));
        } else {
            return false;
        }
    }

    protected NonNullList<ItemStack> makeWrapper() {
        NonNullList<ItemStack> stacks = NonNullList.withSize(5,ItemStack.EMPTY);
        FluidStack fluidStack = handler.fluidStack;
        ItemStack potionStack = ItemStack.EMPTY;
        if (fluidStack.getFluid() == Fluids.WATER || fluidStack.getFluid() == Init.ModFluids.POTION) {
            Potion potion;
            if (fluidStack.getFluid() == Init.ModFluids.POTION) {
                potion = PotionUtils.getPotion(fluidStack.getTag());
            } else {
                potion = Potions.WATER;
            }
            potionStack = PotionUtils.setPotion(Items.POTION.getDefaultInstance(),potion);
        }

        if (!potionStack.isEmpty()) {
            int potionLevel = getBlockState().getValue(WaterBrewingCauldronBlock.LEVEL);
            for (int i = 0; i < potionLevel;i++) {
                stacks.set(i,potionStack);
            }
        }

        stacks.set(3,handler.getIngredient());
        stacks.set(4,handler.getFuel());

        return stacks;
    }

    public void updateAppearance() {
        FluidStack stack = handler.fluidStack;
        if (stack.isEmpty() && getBlockState().getBlock() != Init.ModBlocks.BREWING_CAULDRON) {
            level.setBlock(worldPosition,Init.ModBlocks.BREWING_CAULDRON.defaultBlockState(),Block.UPDATE_ALL);
        }

        if ((stack.getFluid() == Fluids.WATER || stack.getFluid() == Init.ModFluids.POTION) && getBlockState().getBlock() != Init.ModBlocks.WATER_BREWING_CAULDRON) {
            level.setBlock(worldPosition,Init.ModBlocks.WATER_BREWING_CAULDRON.defaultBlockState().setValue(WaterBrewingCauldronBlock.LEVEL,3),Block.UPDATE_ALL);
        }

        if (stack.getFluid() == Fluids.LAVA && getBlockState().getBlock() != Init.ModBlocks.LAVA_BREWING_CAULDRON) {
            level.setBlock(worldPosition,Init.ModBlocks.LAVA_BREWING_CAULDRON.defaultBlockState(),Block.UPDATE_ALL);
        }
    }

    public BrewingHandler handler = new BrewingHandler(4,this) {
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
        updateAppearance();
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

        handler.deserializeNBT(nbt.getCompound("handler"));

        super.load(nbt);
        if (hasLevel())
            level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        PotionUtils2.saveAllEffects(compound, potion, customEffects,customPotionColor);
        compound.put("handler",handler.serializeNBT());
        super.saveAdditional(compound);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new BrewingCauldronMenu(pContainerId,pPlayerInventory, handler,handler,dataAccess);
    }
}
