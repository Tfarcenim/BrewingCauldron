package tfar.brewingcauldron.network;

import net.minecraft.resources.ResourceLocation;
import tfar.brewingcauldron.BrewingCauldron;
import tfar.brewingcauldron.network.client.S2CTownInfoPacket;

import java.util.Locale;

public class PacketHandler {

    public static void registerPackets() {
        PacketHandlerForge.registerClientPacket(S2CTownInfoPacket.class, S2CTownInfoPacket::new);

    }

    public static ResourceLocation packet(Class<?> clazz) {
        return BrewingCauldron.id(clazz.getName().toLowerCase(Locale.ROOT));
    }

}
