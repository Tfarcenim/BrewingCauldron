package tfar.brewingcauldron.network;

import net.minecraft.network.FriendlyByteBuf;

public interface ModPacket {

    void write(FriendlyByteBuf to);

}
