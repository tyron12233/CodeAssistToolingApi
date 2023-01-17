package com.tyron.tooling.packet;

import java.io.Serializable;

public class InvokePrimitiveResult extends Packet {

    private static final String TYPE = InvokePrimitiveResult.class.getName();

    private Serializable result;

    public InvokePrimitiveResult() {
        super(TYPE, null);
    }

    public InvokePrimitiveResult(Serializable result, String packetId) {
        super(TYPE, packetId);

        this.result = result;
    }

    public Serializable getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "InvokePrimitiveResult{" +
                "result=" + result +
                '}';
    }
}
