package tfar.brewingcauldron;

import com.google.common.base.Suppliers;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
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
