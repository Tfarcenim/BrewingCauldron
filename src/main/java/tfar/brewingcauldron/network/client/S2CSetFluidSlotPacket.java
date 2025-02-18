package tfar.brewingcauldron.network.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidStack;
import tfar.brewingcauldron.BrewingCauldronMenu;
import tfar.brewingcauldron.client.ModClient;

public class S2CSetFluidSlotPacket implements S2CModPacket{

    private final int containerId;
    private final int stateId;
    private final int slot;
    private final FluidStack stack;

    public S2CSetFluidSlotPacket(int pStateId, int pContainerId, int pSlot, FluidStack stack) {
        this.containerId = pContainerId;
        this.stateId = pStateId;
        this.slot = pSlot;
        this.stack = stack;
    }

    public S2CSetFluidSlotPacket(FriendlyByteBuf pBuffer) {
        this.containerId = pBuffer.readByte();
        this.stateId = pBuffer.readVarInt();
        this.slot = pBuffer.readShort();
        this.stack = FluidStack.readFromPacket(pBuffer);
    }

    @Override
    public void handleClient() {
        Player player = ModClient.getLocalPlayer();
        if (containerId == player.containerMenu.containerId && player.containerMenu instanceof BrewingCauldronMenu tankMenu) {
            tankMenu.setFluid(slot, stateId, stack);
        }
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(this.containerId);
        buffer.writeVarInt(this.stateId);
        buffer.writeShort(this.slot);
        stack.writeToPacket(buffer);
    }
}
