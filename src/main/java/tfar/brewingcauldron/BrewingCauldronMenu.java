package tfar.brewingcauldron;

import com.google.common.base.Suppliers;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import tfar.brewingcauldron.network.PacketHandlerForge;
import tfar.brewingcauldron.network.client.S2CInitialSyncFluidInventoryPacket;
import tfar.brewingcauldron.network.client.S2CSetFluidSlotPacket;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class BrewingCauldronMenu extends AbstractContainerMenu {

    private final Slot ingredientSlot;

    private final NonNullList<FluidStack> lastFluidSlots = NonNullList.create();
    public final NonNullList<FluidSlot> fluidSlots = NonNullList.create();
    private final NonNullList<FluidStack> remoteFluidSlots = NonNullList.create();
    private final Player player;
    private final IFluidHandlerModifiable fluidHandler;
    private final ContainerData data;

    public BrewingCauldronMenu(int containerId, Inventory inventory) {
        this(containerId,inventory,new BrewingHandler(4,null),new SimpleFluidHandler(1),new SimpleContainerData(2));
    }

    public BrewingCauldronMenu(int containerId, Inventory inventory, ItemStackHandler handler,IFluidHandlerModifiable fluidHandler, ContainerData data) {
        super(Init.ModMenuTypes.BREWING_CAULDRON, containerId);

        player = inventory.player;
        this.fluidHandler = fluidHandler;
        this.data = data;

        this.addSlot(new SlotItemHandler(handler, 0, 56-12, 51));
       // this.addSlot(new SlotItemHandler(handler, 1, 79, 58));
        this.addSlot(new SlotItemHandler(handler, 1, 102+12+2, 51));
        this.ingredientSlot = this.addSlot(new SlotItemHandler(handler, 2, 79, 17));
        this.addSlot(new SlotItemHandler(handler, 3, 17, 17));


        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
        }

        addDataSlots(data);

        addFluidSlot(new FluidSlot(fluidHandler,0,79,51));

    }

    protected FluidSlot addFluidSlot(FluidSlot slot) {
        slot.index = this.fluidSlots.size();
        this.fluidSlots.add(slot);
        this.lastFluidSlots.add(FluidStack.EMPTY);
        this.remoteFluidSlots.add(FluidStack.EMPTY);
        return slot;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        //the remote inventory needs to know about locked slots
        for(int i = 0; i < this.fluidSlots.size(); ++i) {
            FluidStack fluid = this.fluidSlots.get(i).getFluid();
            Supplier<FluidStack> supplier = Suppliers.memoize(fluid::copy);
            //this.triggerSlotListeners(i, fluid, supplier);
            this.synchronizeFluidSlotToRemote(i, fluid,supplier);
        }
    }

    private void synchronizeFluidSlotToRemote(int slot, FluidStack stack, Supplier<FluidStack> supplier) {
        FluidStack remoteFluid = this.remoteFluidSlots.get(slot);
        if (!Objects.equals(remoteFluid,stack)) {
            FluidStack copy = supplier.get();
            this.remoteFluidSlots.set(slot, copy);
            PacketHandlerForge.sendToClient(new S2CSetFluidSlotPacket(incrementStateId(), containerId, slot, stack), (ServerPlayer) player);
        }
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();

        for(int i = 0; i < this.fluidSlots.size(); i++) {
            FluidSlot fluidSlot = fluidSlots.get(i);
            this.remoteFluidSlots.set(i, fluidSlot.getFluid().copy());
        }


        PacketHandlerForge.sendToClient(new S2CInitialSyncFluidInventoryPacket(incrementStateId(), containerId, fluidHandler.getFluids()), (ServerPlayer) player);
    }

    public FluidSlot getFluidSlot(int slot) {
        return this.fluidSlots.get(slot);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;

    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if ((pIndex < 0 || pIndex > 1) && pIndex != 3 && pIndex != 4) {
                if (BrewingStandMenu.FuelSlot.mayPlaceItem(itemstack)) {
                    if (this.moveItemStackTo(itemstack1, 3, 4, false) || this.ingredientSlot.mayPlace(itemstack1) && !this.moveItemStackTo(itemstack1, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.ingredientSlot.mayPlace(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (BrewingStandMenu.PotionSlot.mayPlaceItem(itemstack)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (pIndex >= 4 && pIndex < 31) {
                    if (!this.moveItemStackTo(itemstack1, 31, 40, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (pIndex >= 31 && pIndex < 40) {
                    if (!this.moveItemStackTo(itemstack1, 4, 31, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, 4, 40, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, itemstack1);
        }

        return itemstack;
    }

    public void initializeFluids(int stateID, List<FluidStack> stacks) {
        for(int i = 0; i < stacks.size(); ++i) {
            FluidSlot fluidSlot = getFluidSlot(i);
            fluidSlot.setFluid(stacks.get(i));
        }
        this.stateId = stateID;
    }

    public void setFluid(int slot, int stateId, FluidStack stack) {
        this.getFluidSlot(slot).setFluid(stack);
        this.stateId = stateId;
    }

    public int getFuel() {
        return this.data.get(1);
    }

    public int getBrewingTicks() {
        return this.data.get(0);
    }
}
