package com.tyron.tooling.packet;

import java.util.UUID;

public class Ping extends Packet {

    public Ping() {
        super(Ping.class.getName(), UUID.randomUUID().toString());
    }
}
