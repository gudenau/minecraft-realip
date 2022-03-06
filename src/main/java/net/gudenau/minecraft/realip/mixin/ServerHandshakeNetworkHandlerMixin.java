package net.gudenau.minecraft.realip.mixin;

import net.gudenau.minecraft.realip.duck.HandshakeC2SPacketDuck;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakeNetworkHandler.class)
public abstract class ServerHandshakeNetworkHandlerMixin {
    @Shadow @Final private ClientConnection connection;
    
    @Inject(
        method = "onHandshake",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onHandshake(HandshakeC2SPacket packet, CallbackInfo ci){
        // Get the real address from the handshake packet
        var realAddress = ((HandshakeC2SPacketDuck)packet).realip_realAddress();
        if(realAddress == null){
            // This is really weird, disconnect them because it is unexpected
            connection.disconnect(Text.of("Invalid handshake"));
            ci.cancel();
        }else{
            // Set the address
            ((ClientConnectionAccessor)connection).setAddress(realAddress);
        }
    }
}
