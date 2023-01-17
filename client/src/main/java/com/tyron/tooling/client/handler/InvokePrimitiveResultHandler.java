package com.tyron.tooling.client.handler;

import com.tyron.tooling.packet.InvokePrimitiveResult;
import com.tyron.tooling.packet.handler.PacketHandler;
import com.tyron.tooling.packet.handler.Result;

public class InvokePrimitiveResultHandler extends PacketHandler<InvokePrimitiveResult> {

    public InvokePrimitiveResultHandler() {
        super(InvokePrimitiveResult.class);
    }

    @Override
    public Result<?> handle(InvokePrimitiveResult packet) {
        return Result.success(packet.getResult());
    }
}
