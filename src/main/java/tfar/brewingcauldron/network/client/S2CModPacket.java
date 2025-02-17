package tfar.brewingcauldron.network.client;


import tfar.brewingcauldron.network.ModPacket;

public interface S2CModPacket extends ModPacket {

    void handleClient();

}
