package tfar.brewingcauldron.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.wrapper.InvWrapper;
import tfar.brewingcauldron.BrewingCauldronBlockEntity;
import tfar.brewingcauldron.Init;
import tfar.brewingcauldron.PotionType;
import tfar.brewingcauldron.PotionUtils2;

import java.util.Map;
import java.util.function.Predicate;

public class CauldronInteractions {

    public static final Map<Item, CauldronInteraction> EMPTY_BREWING = CauldronInteraction.newInteractionMap();
    public static final Map<Item, CauldronInteraction> WATER_BREWING = CauldronInteraction.newInteractionMap();
    public static final Map<Item, CauldronInteraction> LAVA_BREWING = CauldronInteraction.newInteractionMap();
    public static final Map<Item, CauldronInteraction> POWDER_SNOW_BREWING = CauldronInteraction.newInteractionMap();

    static CauldronInteraction FILL_WATER = (p_175683_, p_175684_, p_175685_, p_175686_, p_175687_, p_175688_) -> {
        return emptyBucket(p_175684_, p_175685_, p_175686_, p_175687_, p_175688_, Init.ModBlocks.WATER_BREWING_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY);
    };
    static CauldronInteraction FILL_LAVA = (p_175676_, p_175677_, p_175678_, p_175679_, p_175680_, p_175681_) -> {
        return emptyBucket(p_175677_, p_175678_, p_175679_, p_175680_, p_175681_, Init.ModBlocks.LAVA_BREWING_CAULDRON.defaultBlockState(), SoundEvents.BUCKET_EMPTY_LAVA);
    };
    static CauldronInteraction FILL_POWDER_SNOW = (p_175669_, p_175670_, p_175671_, p_175672_, p_175673_, p_175674_) -> {
        return emptyBucket(p_175670_, p_175671_, p_175672_, p_175673_, p_175674_, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
    };

    static CauldronInteraction SHULKER_BOX = (p_175662_, p_175663_, p_175664_, p_175665_, p_175666_, stack) -> {
        Block block = Block.byItem(stack.getItem());
        if (!(block instanceof ShulkerBoxBlock)) {
            return InteractionResult.PASS;
        } else {
            if (!p_175663_.isClientSide) {
                ItemStack itemstack = new ItemStack(Blocks.SHULKER_BOX);
                if (stack.hasTag()) {
                    itemstack.setTag(stack.getTag().copy());
                }

                p_175665_.setItemInHand(p_175666_, itemstack);
                p_175665_.awardStat(Stats.CLEAN_SHULKER_BOX);
                WaterBrewingCauldronBlock.lowerFillLevel(p_175662_, p_175663_, p_175664_);
            }

            return InteractionResult.sidedSuccess(p_175663_.isClientSide);
        }
    };
    static CauldronInteraction BANNER = (p_175653_, p_175654_, p_175655_, p_175656_, p_175657_, p_175658_) -> {
        if (BannerBlockEntity.getPatternCount(p_175658_) <= 0) {
            return InteractionResult.PASS;
        } else {
            if (!p_175654_.isClientSide) {
                ItemStack itemstack = p_175658_.copy();
                itemstack.setCount(1);
                BannerBlockEntity.removeLastPattern(itemstack);
                if (!p_175656_.getAbilities().instabuild) {
                    p_175658_.shrink(1);
                }

                if (p_175658_.isEmpty()) {
                    p_175656_.setItemInHand(p_175657_, itemstack);
                } else if (p_175656_.getInventory().add(itemstack)) {
                    p_175656_.inventoryMenu.sendAllDataToRemote();
                } else {
                    p_175656_.drop(itemstack, false);
                }

                p_175656_.awardStat(Stats.CLEAN_BANNER);
                WaterBrewingCauldronBlock.lowerFillLevel(p_175653_, p_175654_, p_175655_);
            }

            return InteractionResult.sidedSuccess(p_175654_.isClientSide);
        }
    };
    static CauldronInteraction DYED_ITEM = (p_175629_, p_175630_, p_175631_, p_175632_, p_175633_, p_175634_) -> {
        Item item = p_175634_.getItem();
        if (!(item instanceof DyeableLeatherItem dyeableleatheritem)) {
            return InteractionResult.PASS;
        } else {
            if (!dyeableleatheritem.hasCustomColor(p_175634_)) {
                return InteractionResult.PASS;
            } else {
                if (!p_175630_.isClientSide) {
                    dyeableleatheritem.clearColor(p_175634_);
                    p_175632_.awardStat(Stats.CLEAN_ARMOR);
                    WaterBrewingCauldronBlock.lowerFillLevel(p_175629_, p_175630_, p_175631_);
                }

                return InteractionResult.sidedSuccess(p_175630_.isClientSide);
            }
        }
    };

    static InteractionResult emptyBucket(Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, ItemStack pFilledStack, BlockState pState, SoundEvent pEmptySound) {
        if (!pLevel.isClientSide) {
            Item item = pFilledStack.getItem();
           // pPlayer.setItemInHand(pHand, ItemUtils.createFilledResult(pFilledStack, pPlayer, new ItemStack(Items.BUCKET)));
            pPlayer.awardStat(Stats.FILL_CAULDRON);
            pPlayer.awardStat(Stats.ITEM_USED.get(item));
            //pLevel.setBlockAndUpdate(pPos, pState);

            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof BrewingCauldronBlockEntity brewingCauldronBlockEntity) {
                //brewingCauldronBlockEntity.
                FluidActionResult fluidActionResult = FluidUtil.tryEmptyContainerAndStow(pFilledStack, brewingCauldronBlockEntity.handler, new InvWrapper(pPlayer.getInventory()), 1000, pPlayer, true);
                if (fluidActionResult.isSuccess()) {
                    pPlayer.setItemInHand(pHand,fluidActionResult.getResult());
                }
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    public static boolean isWater(FluidStack stack) {
        if (stack.getFluid().isSame(Fluids.WATER)) return true;

        if (!stack.getFluid().isSame(Init.ModFluids.POTION)) return false;
        CompoundTag tag = stack.getTag();
        if (tag == null) return false;
        return PotionUtils.getPotion(tag) == Potions.WATER && PotionType.valueOf(tag.getString("PotionType")) == PotionType.REGULAR;
    }

    public static void bootStrap() {
        addDefaultInteractions(EMPTY_BREWING);
        EMPTY_BREWING.put(Init.ModItems.POTION_BUCKET,(pBlockState, pLevel, pBlockPos, pPlayer, pHand, pStack) -> {
            return emptyBucket(pLevel, pBlockPos, pPlayer, pHand,pStack, Init.ModBlocks.WATER_BREWING_CAULDRON.defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY);
        });
        EMPTY_BREWING.put(Items.POTION, (state, level, pos, player, hand, stack) -> emptyPotion(state,level,pos,player,hand,stack, PotionType.REGULAR));

        EMPTY_BREWING.put(Items.SPLASH_POTION, (state, level, pos, player, hand, stack) -> emptyPotion(state,level,pos,player,hand,stack, PotionType.SPLASH));
        EMPTY_BREWING.put(Items.LINGERING_POTION, (state, level, pos, player, hand, stack) -> emptyPotion(state,level,pos,player,hand,stack, PotionType.LINGERING));

        addDefaultInteractions(WATER_BREWING);
        WATER_BREWING.put(Items.BUCKET, (p_175725_, p_175726_, p_175727_, p_175728_, p_175729_, p_175730_)
                -> fillBucket(p_175725_, p_175726_, p_175727_, p_175728_, p_175729_, p_175730_, new ItemStack(Items.WATER_BUCKET),
                p_175660_ -> p_175660_.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL));
        WATER_BREWING.put(Items.GLASS_BOTTLE, (p_175718_, level, pos, player, hand, stack) -> {
            if (!level.isClientSide) {

                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof BrewingCauldronBlockEntity brewingBE) {
                    FluidStack fluidStack = brewingBE.handler.getFluidInTank(0);
                    if (fluidStack.getFluid() == Fluids.WATER || fluidStack.getFluid() == Init.ModFluids.POTION) {
                        Item item = stack.getItem();
                        ItemStack potionStack = switch (PotionUtils2.getPotionType(fluidStack)) {
                            case REGULAR -> Items.POTION.getDefaultInstance();
                            case SPLASH ->Items.SPLASH_POTION.getDefaultInstance();
                            case LINGERING -> Items.LINGERING_POTION.getDefaultInstance();
                        };
                        if (fluidStack.getFluid() == Fluids.WATER) {
                            PotionUtils.setPotion(potionStack, Potions.WATER);
                        } else  {
                            potionStack.setTag(fluidStack.getTag());
                        }
                        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, potionStack));
                        //player.setItemInHand(hand,potionStack);
                        brewingBE.handler.bottles--;
                        if (brewingBE.handler.bottles<=0) {
                            brewingBE.handler.setFluidInSlot(0,FluidStack.EMPTY);
                        } else {
                            brewingBE.setChanged();
                        }
                            player.awardStat(Stats.USE_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(item));


                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                    }
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        });
        WATER_BREWING.put(Items.POTION, (state, level, pos, player, hand, stack) -> emptyPotionAlt(state,level,pos,player,hand,stack));
        WATER_BREWING.put(Items.SPLASH_POTION, (state, level, pos, player, hand, stack) -> emptyPotionAlt(state,level,pos,player,hand,stack));
        WATER_BREWING.put(Items.LINGERING_POTION, (state, level, pos, player, hand, stack) -> emptyPotionAlt(state,level,pos,player,hand,stack));


        WATER_BREWING.put(Items.LEATHER_BOOTS, DYED_ITEM);
        WATER_BREWING.put(Items.LEATHER_LEGGINGS, DYED_ITEM);
        WATER_BREWING.put(Items.LEATHER_CHESTPLATE, DYED_ITEM);
        WATER_BREWING.put(Items.LEATHER_HELMET, DYED_ITEM);
        WATER_BREWING.put(Items.LEATHER_HORSE_ARMOR, DYED_ITEM);
        WATER_BREWING.put(Items.WHITE_BANNER, BANNER);
        WATER_BREWING.put(Items.GRAY_BANNER, BANNER);
        WATER_BREWING.put(Items.BLACK_BANNER, BANNER);
        WATER_BREWING.put(Items.BLUE_BANNER, BANNER);
        WATER_BREWING.put(Items.BROWN_BANNER, BANNER);
        WATER_BREWING.put(Items.CYAN_BANNER, BANNER);
        WATER_BREWING.put(Items.GREEN_BANNER, BANNER);
        WATER_BREWING.put(Items.LIGHT_BLUE_BANNER, BANNER);
        WATER_BREWING.put(Items.LIGHT_GRAY_BANNER, BANNER);
        WATER_BREWING.put(Items.LIME_BANNER, BANNER);
        WATER_BREWING.put(Items.MAGENTA_BANNER, BANNER);
        WATER_BREWING.put(Items.ORANGE_BANNER, BANNER);
        WATER_BREWING.put(Items.PINK_BANNER, BANNER);
        WATER_BREWING.put(Items.PURPLE_BANNER, BANNER);
        WATER_BREWING.put(Items.RED_BANNER, BANNER);
        WATER_BREWING.put(Items.YELLOW_BANNER, BANNER);
        WATER_BREWING.put(Items.WHITE_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.GRAY_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.BLACK_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.BLUE_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.BROWN_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.CYAN_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.GREEN_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.LIGHT_BLUE_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.LIGHT_GRAY_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.LIME_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.MAGENTA_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.ORANGE_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.PINK_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.PURPLE_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.RED_SHULKER_BOX, SHULKER_BOX);
        WATER_BREWING.put(Items.YELLOW_SHULKER_BOX, SHULKER_BOX);
        LAVA_BREWING.put(Items.BUCKET, (p_175697_, p_175698_, p_175699_, p_175700_, p_175701_, p_175702_) ->
                fillBucket(p_175697_, p_175698_, p_175699_, p_175700_, p_175701_, p_175702_, new ItemStack(Items.LAVA_BUCKET), p_175651_ -> true, SoundEvents.BUCKET_FILL_LAVA));
        addDefaultInteractions(LAVA_BREWING);
        POWDER_SNOW_BREWING.put(Items.BUCKET, (p_175690_, p_175691_, p_175692_, p_175693_, p_175694_, p_175695_) ->
                fillBucket(p_175690_, p_175691_, p_175692_, p_175693_, p_175694_, p_175695_, new ItemStack(Items.POWDER_SNOW_BUCKET),
                        p_175627_ -> p_175627_.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL_POWDER_SNOW));
        addDefaultInteractions(POWDER_SNOW_BREWING);
    }

    static InteractionResult emptyPotion(BlockState pBlockState, Level level, BlockPos pos, Player player, InteractionHand hand,
                                         ItemStack stack, PotionType potionType) {

        if (!level.isClientSide) {


            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BrewingCauldronBlockEntity brewingBE) {
                Item item = stack.getItem();
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                brewingBE.handler.bottles++;
                CompoundTag tag = stack.getTag() != null ? stack.getTag().copy() : new CompoundTag();
                tag.putString("PotionType",potionType.name());
                brewingBE.handler.setFluidInSlot(0,new FluidStack(Init.ModFluids.POTION, FluidAttributes.BUCKET_VOLUME,tag));
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    static InteractionResult emptyPotionAlt(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                            ItemStack stack) {
        if (state.getValue(LayeredCauldronBlock.LEVEL) != 3) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof BrewingCauldronBlockEntity brewingBE) {



                    if (PotionUtils2.haveSameEffects(stack, brewingBE.handler.getFluidInTank(0))) {
                        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                        player.awardStat(Stats.USE_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

                        brewingBE.handler.bottles++;
                        brewingBE.setChanged();

                        level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1, 1);
                        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                    }
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
            // return InteractionResult.PASS;
        } else {
            return InteractionResult.PASS;
        }
    }

    static void addDefaultInteractions(Map<Item, CauldronInteraction> pInteractionsMap) {
        pInteractionsMap.put(Items.LAVA_BUCKET, FILL_LAVA);
        pInteractionsMap.put(Items.WATER_BUCKET, FILL_WATER);
        pInteractionsMap.put(Items.POWDER_SNOW_BUCKET, FILL_POWDER_SNOW);
    }

    static InteractionResult fillBucket(BlockState pBlockState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, ItemStack pEmptyStack, ItemStack pFilledStack, Predicate<BlockState> pStatePredicate, SoundEvent pFillSound) {
        if (!pStatePredicate.test(pBlockState)) {
            return InteractionResult.PASS;
        } else {
            if (!pLevel.isClientSide) {
                Item item = pEmptyStack.getItem();
              //  pPlayer.setItemInHand(pHand, ItemUtils.createFilledResult(pEmptyStack, pPlayer, pFilledStack));
                pPlayer.awardStat(Stats.USE_CAULDRON);
                pPlayer.awardStat(Stats.ITEM_USED.get(item));

                BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
                if (blockEntity instanceof BrewingCauldronBlockEntity brewingCauldronBlockEntity) {
                    FluidActionResult fluidActionResult = FluidUtil.tryFillContainerAndStow(pEmptyStack, brewingCauldronBlockEntity.handler, new InvWrapper(pPlayer.getInventory()), 1000, pPlayer, true);
                    if (fluidActionResult.isSuccess()) {
                        pPlayer.setItemInHand(pHand,fluidActionResult.getResult());
                    }
                }
            }

            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
    }
}
