package tfar.brewingcauldron.network.server;

import net.minecraft.server.level.ServerPlayer;
import tfar.brewingcauldron.network.ModPacket;

public interface C2SModPacket extends ModPacket {

    void handleServer(ServerPlayer player);

}
