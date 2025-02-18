package tfar.brewingcauldron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class PotionFluid extends Fluid {
    @Override
    public Item getBucket() {
        return Init.ModItems.POTION_BUCKET;
    }



    @Override
    protected boolean canBeReplacedWith(FluidState pState, BlockGetter pLevel, BlockPos pPos, Fluid pFluid, Direction pDirection) {
        return false;
    }

    @Override
    protected Vec3 getFlow(BlockGetter pBlockReader, BlockPos pPos, FluidState pFluidState) {
        return Vec3.ZERO;
    }

    @Override
    public int getTickDelay(LevelReader pLevel) {
        return 0;
    }

    @Override
    protected float getExplosionResistance() {
        return 0;
    }

    @Override
    public float getHeight(FluidState pState, BlockGetter pLevel, BlockPos pPos) {
        return 0;
    }

    @Override
    public float getOwnHeight(FluidState pState) {
        return 0;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState pState) {
        return Blocks.WATER.defaultBlockState();
    }

    @Override
    public boolean isSource(FluidState pState) {
        return false;
    }

    @Override
    public int getAmount(FluidState pState) {
        return 0;
    }

    @Override
    public VoxelShape getShape(FluidState pState, BlockGetter pLevel, BlockPos pPos) {
        return Shapes.empty();
    }

    @Override
    protected FluidAttributes createAttributes() {
        return new PotionAttributes(FluidAttributes.builder(new ResourceLocation("block/water_still"), new ResourceLocation("block/water_flow"))
                .overlay(new ResourceLocation("block/water_overlay"))
                .translationKey("block.minecraft.water")
                .color(0xff000000|0x3f76e4).sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY)
        ,this);
    }

    public static class PotionAttributes extends FluidAttributes {
        protected PotionAttributes(Builder builder, Fluid fluid) {
            super(builder, fluid);
        }

        @Override
        public String getTranslationKey(FluidStack stack) {
            if (stack.hasTag()) {
                CompoundTag tag = stack.getTag();
                Potion potion = PotionUtils.getPotion(tag);
                return potion.getName(Items.POTION.getDescriptionId() + ".effect.");
            }
            return super.getTranslationKey(stack);
        }
    }
}
