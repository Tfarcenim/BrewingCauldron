package tfar.brewingcauldron;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import tfar.brewingcauldron.network.PacketHandlerForge;

import java.util.Objects;
import java.util.Optional;

//copied from forge and adapted for common
public class MLFluidStack {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final MLFluidStack EMPTY = new MLFluidStack(Fluids.EMPTY, 0);

    public static final Codec<MLFluidStack> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Registry.FLUID.byNameCodec().fieldOf("FluidName").forGetter(MLFluidStack::getFluid),
                    Codec.INT.fieldOf("Amount").forGetter(MLFluidStack::getAmount),
                    CompoundTag.CODEC.optionalFieldOf("Tag").forGetter(stack -> Optional.ofNullable(stack.getTag()))
            ).apply(instance, (fluid, amount, tag) -> {
                MLFluidStack stack = new MLFluidStack(fluid, amount);
                tag.ifPresent(stack::setTag);
                return stack;
            })
    );

    private boolean isEmpty;
    private int amount;
    private CompoundTag tag;
    private Fluid fluid;
    private boolean immutable;

    public MLFluidStack(Fluid fluid, int amount) {
        if (fluid == null) {
            LOGGER.fatal("Null fluid supplied to MLFluidStack. Did you try and create a stack for an unregistered fluid?");
            throw new IllegalArgumentException("Cannot create a MLFluidStack from a null fluid");
        } else {
            this.fluid = fluid;
        }
        this.amount = amount;

        updateEmpty();
    }

    public MLFluidStack(Fluid fluid, int amount, CompoundTag nbt) {
        this(fluid, amount);

        if (nbt != null) {
            tag = nbt.copy();
        }
    }

    public MLFluidStack(MLFluidStack stack, int amount) {
        this(stack.getFluid(), amount, stack.tag);
    }

    public MLFluidStack immutable() {
        immutable = true;
        return this;
    }

    /**
     * This provides a safe method for retrieving a MLFluidStack - if the Fluid is invalid, the stack
     * will return as null.
     */
    public static MLFluidStack fromNBT(CompoundTag nbt) {
        if (nbt == null) {
            return EMPTY;
        }
        if (!nbt.contains("FluidName", Tag.TAG_STRING)) {
            return EMPTY;
        }

        ResourceLocation fluidName = new ResourceLocation(nbt.getString("FluidName"));
        Fluid fluid = Registry.FLUID.get(fluidName);
        if (fluid == Fluids.EMPTY) {
            return EMPTY;
        }
        MLFluidStack stack = new MLFluidStack(fluid, nbt.getInt("Amount"));

        if (nbt.contains("Tag", Tag.TAG_COMPOUND)) {
            stack.tag = nbt.getCompound("Tag");
        }
        return stack;
    }

    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putString("FluidName", Registry.FLUID.getKey(getFluid()).toString());
        nbt.putInt("Amount", amount);

        if (tag != null) {
            nbt.put("Tag", tag);
        }
        return nbt;
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeVarInt(Registry.FLUID.getId(getFluid()));
        buf.writeVarInt(getAmount());
        buf.writeNbt(tag);
    }

    public static MLFluidStack readFromPacket(FriendlyByteBuf buf) {
        Fluid fluid = Registry.FLUID.byId(buf.readVarInt());
        int amount = buf.readVarInt();
        CompoundTag tag = buf.readNbt();
        if (fluid == Fluids.EMPTY) return EMPTY;
        return new MLFluidStack(fluid, amount, tag);
    }

    public final Fluid getFluid() {
        return isEmpty ? Fluids.EMPTY : fluid;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    protected void updateEmpty() {
        isEmpty = fluid == Fluids.EMPTY || amount <= 0;
    }

    public int getAmount() {
        return isEmpty ? 0 : amount;
    }

    public void setAmount(int amount) {
        if (fluid == Fluids.EMPTY) throw new IllegalStateException("Can't modify the empty stack.");
        if (immutable) throw new IllegalStateException("Illegal modification detected");
        this.amount = amount;
        updateEmpty();
    }

    public void grow(int amount) {
        setAmount(this.amount + amount);
    }

    public void shrink(int amount) {
        setAmount(this.amount - amount);
    }

    public boolean hasTag() {
        return tag != null;
    }

    public CompoundTag getTag() {
        return tag;
    }

    public MLFluidStack copyWithAmount(int amount) {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            MLFluidStack stack = this.copy();
            stack.setAmount(amount);
            return stack;
        }
    }

    public void setTag(CompoundTag tag) {
        if (fluid == Fluids.EMPTY) throw new IllegalStateException("Can't modify the empty stack.");
        if (immutable) throw new IllegalStateException("Illegal modification detected");
        this.tag = tag;
    }

    public CompoundTag getOrCreateTag() {
        if (tag == null)
            setTag(new CompoundTag());
        return tag;
    }

    public CompoundTag getChildTag(String childName) {
        if (tag == null)
            return null;
        return tag.getCompound(childName);
    }

    public CompoundTag getOrCreateChildTag(String childName) {
        getOrCreateTag();
        CompoundTag child = tag.getCompound(childName);
        if (!tag.contains(childName, Tag.TAG_COMPOUND)) {
            tag.put(childName, child);
        }
        return child;
    }

    public void removeChildTag(String childName) {
        if (tag != null)
            tag.remove(childName);
    }

    public Component getDisplayName() {
        return PacketHandlerForge.getDisplayName(this);
    }

    public String getTranslationKey() {
        return PacketHandlerForge.getTranslationKey(this);
    }

    /**
     * @return A copy of this MLFluidStack
     */
    public MLFluidStack copy() {
        if (isEmpty()) return EMPTY;
        return new MLFluidStack(getFluid(), amount, tag);
    }

    /**
     * Determines if the FluidIDs and NBT Tags are equal. This does not check amounts.
     *
     * @param other The MLFluidStack for comparison
     * @return true if the Fluids (IDs and NBT Tags) are the same
     */
    public boolean isFluidEqual(@NotNull MLFluidStack other) {
        return getFluid() == other.getFluid() && isTagEqual(other);
    }

    public static boolean areFluidsEqual(MLFluidStack stack1,MLFluidStack stack2) {
        return stack1.isFluidEqual(stack2);
    }

    private boolean isTagEqual(MLFluidStack other) {
        return tag == null ? other.tag == null : other.tag != null && tag.equals(other.tag);
    }

    /**
     * Determines if the NBT Tags are equal. Useful if the FluidIDs are known to be equal.
     */
    public static boolean areTagsEqual(@NotNull MLFluidStack stack1, @NotNull MLFluidStack stack2) {
        return stack1.isTagEqual(stack2);
    }

    /**
     * Determines if the Fluids are equal and this stack is larger.
     *
     * @return true if this MLFluidStack contains the other MLFluidStack (same fluid and >= amount)
     */
    public boolean containsFluid(@NotNull MLFluidStack other) {
        return isFluidEqual(other) && amount >= other.amount;
    }

    /**
     * Determines if the FluidIDs, Amounts, and NBT Tags are all equal.
     *
     * @param other - the MLFluidStack for comparison
     * @return true if the two CommonFluidStacks are exactly the same
     */
    boolean isFluidStackIdentical(MLFluidStack other) {
        return isFluidEqual(other) && amount == other.amount;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(fluid,amount,tag);
    }

    /**
     * Default equality comparison for a MLFluidStack. Same functionality as isFluidEqual().
     * <p>
     * This is included for use in data structures.
     */
    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof MLFluidStack other)) {
            return false;
        }
        return isFluidStackIdentical(other);
    }

}
