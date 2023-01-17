package com.tyron.tooling.packet;

public class Packet {

    private String type;

    private String packetId;

    public Packet() {

    }

    public Packet(String type, String packetId) {
        this.type = type;
        this.packetId = packetId;
    }

    public String getPacketId() {
        return packetId;
    }

    public String getType() {
        return type;
    }
}
