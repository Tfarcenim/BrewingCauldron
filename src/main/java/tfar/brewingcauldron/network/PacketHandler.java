package tfar.brewingcauldron.network;

import net.minecraft.resources.ResourceLocation;
import tfar.brewingcauldron.BrewingCauldron;
import tfar.brewingcauldron.network.client.S2CInitialSyncFluidInventoryPacket;
import tfar.brewingcauldron.network.client.S2CSetFluidSlotPacket;

import java.util.Locale;

public class PacketHandler {

    public static void registerPackets() {
        PacketHandlerForge.registerClientPacket(S2CInitialSyncFluidInventoryPacket.class, S2CInitialSyncFluidInventoryPacket::new);
        PacketHandlerForge.registerClientPacket(S2CSetFluidSlotPacket.class, S2CSetFluidSlotPacket::new);

    }

    public static ResourceLocation packet(Class<?> clazz) {
        return BrewingCauldron.id(clazz.getName().toLowerCase(Locale.ROOT));
    }

}
