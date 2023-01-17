package com.tyron.tooling.client.handler;

import com.tyron.tooling.client.Client;
import com.tyron.tooling.client.ClientProxyInvocationHandler;
import com.tyron.tooling.packet.Invoke;
import com.tyron.tooling.packet.InvokeResult;
import com.tyron.tooling.packet.handler.PacketHandler;
import com.tyron.tooling.packet.handler.Result;

import java.io.Serializable;
import java.lang.constant.ConstantDesc;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class InvokeResultHandler extends PacketHandler<InvokeResult> {

    private final Client client;

    public InvokeResultHandler(Client client) {
        super(InvokeResult.class);

        this.client = client;
    }

    @Override
    public Result<?> handle(InvokeResult result) {
        String objectId = result.getResultObjectId();
        List<String> resultInterfaces = result.getResultInterfaces();
        Class<?>[] interfaces = new Class[resultInterfaces.size()];
        for (int i = 0; i < resultInterfaces.size(); i++) {
            String resultInterface = resultInterfaces.get(i);

            try {
                interfaces[i] = Class.forName(resultInterface);
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found: " + resultInterface);
                throw new RuntimeException(e);
            }
        }

        interfaces = Arrays.stream(interfaces)
                .filter(it -> !it.isSealed())
                .toArray(Class[]::new);

        return Result.success(
                Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        interfaces,
                        new ClientProxyInvocationHandler(client, objectId, result.getClassName())
                )
        );
    }
}
