package tfar.brewingcauldron.network.client;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import tfar.brewingcauldron.BrewingCauldronMenu;
import tfar.brewingcauldron.client.ModClient;

import java.util.List;


public class S2CInitialSyncFluidInventoryPacket implements S2CModPacket {

    private final int stateID;
    private final int containerID;
    private final List<FluidStack> stacks;

    public S2CInitialSyncFluidInventoryPacket(int stateID, int containerID, NonNullList<FluidStack> stacks) {
        this.stateID = stateID;
        this.containerID = containerID;
        this.stacks = stacks;
    }

    public S2CInitialSyncFluidInventoryPacket(FriendlyByteBuf buf) {
        stateID = buf.readInt();
        containerID = buf.readInt();
        stacks = buf.readList(FluidStack::readFromPacket);
    }

    @Override
    public void handleClient() {
        Player player = ModClient.getLocalPlayer();
        if (player != null && player.containerMenu instanceof BrewingCauldronMenu tankMenu && containerID == player.containerMenu.containerId) {
            tankMenu.initializeFluids(stateID, stacks);
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(stateID);
        buf.writeInt(containerID);
        buf.writeCollection(stacks, IForgeFriendlyByteBuf::writeFluidStack);
    }
}