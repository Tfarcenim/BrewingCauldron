package tfar.brewingcauldron.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import tfar.brewingcauldron.BrewingCauldron;
import tfar.brewingcauldron.MLFluidStack;
import tfar.brewingcauldron.network.client.S2CModPacket;
import tfar.brewingcauldron.network.server.C2SModPacket;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandlerForge {


    public static SimpleChannel INSTANCE =  NetworkRegistry.newSimpleChannel(BrewingCauldron.id("packet"), () -> "1.0", s -> true, s -> true);

    public static <MSG extends S2CModPacket> BiConsumer<MSG, Supplier<NetworkEvent.Context>> wrapS2C() {
        return ((msg, contextSupplier) -> {
            contextSupplier.get().enqueueWork(msg::handleClient);
            contextSupplier.get().setPacketHandled(true);
        });
    }

    public static <MSG extends C2SModPacket> BiConsumer<MSG, Supplier<NetworkEvent.Context>> wrapC2S() {
        return ((msg, contextSupplier) -> {
            ServerPlayer player = contextSupplier.get().getSender();
            contextSupplier.get().enqueueWork(() -> msg.handleServer(player));
            contextSupplier.get().setPacketHandled(true);
        });
    }

    public static <MSG> void sendToClient(MSG packet, ServerPlayer player) {
        INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <MSG> void sendToServer(MSG packet) {
        INSTANCE.sendToServer(packet);
    }

    static int i;

    public static  <MSG extends S2CModPacket> void registerClientPacket(Class<MSG> packetLocation, Function<FriendlyByteBuf, MSG> reader) {
        PacketHandlerForge.INSTANCE.registerMessage(i++, packetLocation, MSG::write, reader, PacketHandlerForge.wrapS2C());
    }

    public static  <MSG extends C2SModPacket> void registerServerPacket(Class<MSG> packetLocation, Function<FriendlyByteBuf, MSG> reader) {
        PacketHandlerForge.INSTANCE.registerMessage(i++, packetLocation, MSG::write, reader, PacketHandlerForge.wrapC2S());
    }

    public static FluidStack convert(MLFluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getTag());
    }

    public static MLFluidStack convert(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return MLFluidStack.EMPTY;
        }
        return new MLFluidStack(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getTag());
    }

    public static Component getDisplayName(MLFluidStack fluidStack) {
        return convert(fluidStack).getDisplayName();
    }

    public static String getTranslationKey(MLFluidStack fluidStack) {
        return convert(fluidStack).getTranslationKey();
    }

}
