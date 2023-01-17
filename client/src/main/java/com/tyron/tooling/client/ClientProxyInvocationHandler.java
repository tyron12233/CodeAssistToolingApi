package com.tyron.tooling.client;

import com.tyron.tooling.packet.Invoke;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An invocation handler which sends method calls to a server.
 */
public class ClientProxyInvocationHandler implements InvocationHandler {

    private final Client client;
    private final String objectId;
    private final String className;

    public ClientProxyInvocationHandler(Client client, String objectId, String className) {
        this.client = client;
        this.objectId = objectId;
        this.className = className;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Invoke invoke = new Invoke(
                objectId,
                className,
                method.getName(),
                convertToHolder(method, args),
                UUID.randomUUID().toString()
        );
        Object o = client.sendPacket(invoke).get();
        if (o instanceof Throwable) {
            throw (Throwable) o;
        }
        return o;
    }

    private static List<Invoke.ArgumentHolder> convertToHolder(Method method, Object[] args) {
        if (args == null) {
            return Collections.emptyList();
        }
        int current = 0;
        Parameter[] parameters = method.getParameters();

        return IntStream.range(0, args.length).mapToObj(index -> {
            Object argument = args[index];

            Parameter parameter = parameters[index];
            if (parameter.getType().isPrimitive()) {
                return new Invoke.ArgumentHolder(
                        parameter.getType().toString(),
                        null,
                        (Serializable) argument
                );
            }

            if (Proxy.isProxyClass(argument.getClass())) {
                InvocationHandler invocationHandler = Proxy.getInvocationHandler(argument);
                assert invocationHandler instanceof ClientProxyInvocationHandler;

                String argumentObjectId = ((ClientProxyInvocationHandler) invocationHandler).objectId;
                return new Invoke.ArgumentHolder(null, argumentObjectId, null);
            } else if (argument instanceof Serializable) {
                return new Invoke.ArgumentHolder(argument.getClass().getName(), null, (Serializable) argument);
            } else {
                throw new IllegalArgumentException(argument + " is not serializable nor a proxy.");
            }
        }).collect(Collectors.toList());
    }
}
