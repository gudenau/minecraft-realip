package net.gudenau.minecraft.realip.duck;

import java.net.SocketAddress;

/**
 * Allows access to the added address field in the handshake packet.
 */
public interface HandshakeC2SPacketDuck {
    /**
     * Retrieves the real IP of the handshaking user as reported by TCP Shield's custom handshake packet.
     *
     * @return The real address of the user
     */
    SocketAddress realip_realAddress();
}
