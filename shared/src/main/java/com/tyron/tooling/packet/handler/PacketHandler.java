package com.tyron.tooling.packet.handler;

import com.tyron.tooling.packet.Packet;

public abstract class PacketHandler<T extends Packet> {

    private final Class<T> clazz;

    public PacketHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Class<T> getPacketClass() {
        return clazz;
    }

    public abstract Result<?> handle(T packet);
}
