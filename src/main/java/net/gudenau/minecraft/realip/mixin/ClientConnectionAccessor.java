package net.gudenau.minecraft.realip.mixin;

import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.net.SocketAddress;

@Mixin(ClientConnection.class)
public interface ClientConnectionAccessor {
    @Accessor void setAddress(SocketAddress realAddress);
}
