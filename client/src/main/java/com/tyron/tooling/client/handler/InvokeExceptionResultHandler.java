package com.tyron.tooling.client.handler;

import com.tyron.tooling.packet.InvokeExceptionResult;
import com.tyron.tooling.packet.handler.PacketHandler;
import com.tyron.tooling.packet.handler.Result;

public class InvokeExceptionResultHandler extends PacketHandler<InvokeExceptionResult> {

    public InvokeExceptionResultHandler() {
        super(InvokeExceptionResult.class);
    }

    @Override
    public Result<?> handle(InvokeExceptionResult packet) {
        return Result.failed(new Exception(packet.getMessage()));
    }
}
