package com.tyron.tooling.packet;

import java.util.UUID;

public class InvokeExceptionResult extends Packet {

    private static final String TYPE = InvokeExceptionResult.class.getName();

    private String message;

    public InvokeExceptionResult() {
        super(TYPE, UUID.randomUUID().toString());
    }

    public InvokeExceptionResult(String message, String packetId) {
        super(TYPE, packetId);

        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
