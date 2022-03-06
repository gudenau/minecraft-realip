package net.gudenau.minecraft.realip.mixin;

import net.gudenau.minecraft.realip.ValidationHelper;
import net.gudenau.minecraft.realip.duck.HandshakeC2SPacketDuck;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Mixin(HandshakeC2SPacket.class)
public abstract class HandshakeC2SPacketMixin implements HandshakeC2SPacketDuck {
	@Shadow @Mutable @Final private String address;
	
	// Storage of the real address for the user
	@Unique private SocketAddress realip_realAddress;
	
	@Inject(
		method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V",
		at = @At("TAIL")
	)
	private void init(PacketByteBuf buf, CallbackInfo ci) throws IOException {
		// The format for this is
		// real hostname///real ip:real port///timestamp///signature
		var split = address.split("///");
		if(split.length != 4){
			throw new IOException("Bad connection: Expected a TCP Shield connection but got a vanilla one");
		}
		
		try {
			// Extract the different parts of the payload
			var hostname = split[0];
			var timestamp = Integer.parseInt(split[2]);
			var signature = split[3];
			
			// Construct the real address of the user based on what TCP Shield reports
			var socketInfo = split[1].split(":");
			var host = socketInfo[0];
			var port = Integer.parseInt(socketInfo[1]);
			realip_realAddress = new InetSocketAddress(host, port);
			
			// If the timestamp is widely off it is likely a replay attack, prevent that here.
			if(!ValidationHelper.validateTimestamp(timestamp)){
				throw new IOException("Timestamp did not pass validation");
			}
			
			// Prevent modification of the payload by validating it against their public key
			var signaturePayload = hostname + "///" + host + ':' + port + "///" + timestamp;
			if(!ValidationHelper.validateSignature(signaturePayload, signature)){
				throw new IOException("Signature did not pass validation");
			}
			
			// Everything passes, override the "vanilla" hostname with the one TCP Shield provided
			this.address = hostname;
		} catch (NumberFormatException e){
			throw new IOException("Bad connection: Malformed TCP Shield hostname", e);
		}
	}
	
	@Unique
	@Override
	public SocketAddress realip_realAddress() {
		return realip_realAddress;
	}
}
