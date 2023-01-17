package com.tyron.tooling.server.handler;

import com.google.gson.internal.Primitives;
import com.tyron.tooling.packet.Invoke;
import com.tyron.tooling.packet.InvokePrimitiveResult;
import com.tyron.tooling.packet.InvokeResult;
import com.tyron.tooling.packet.handler.PacketHandler;
import com.tyron.tooling.packet.handler.Result;
import com.tyron.tooling.server.ObjectStorage;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InvokeHandler extends PacketHandler<Invoke> {

    public InvokeHandler() {
        super(Invoke.class);
    }

    @Override
    public Result<?> handle(Invoke invoke) {
        String objectId = invoke.getObjectId();
        String className = invoke.getContainingClass();
        String methodName = invoke.getMethodName();
        String packetId = invoke.getPacketId();
        Object[] arguments = getArguments(invoke.getArguments());
        Class<?>[] argumentTypes = Arrays.stream(arguments).map(Object::getClass).toArray(Class[]::new);

        Object object = null;
        Class<?> aClass;
        if (objectId != null && !objectId.isEmpty()) {
            object = ObjectStorage.getInstance().getObject(objectId);
            aClass = object.getClass();
        } else {
            try {
                aClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                return Result.failed(e);
            }
        }

        Method declaredMethod;
        try {
            declaredMethod = aClass.getMethod(methodName, argumentTypes);
            declaredMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            return Result.failed(e);
        }

        Object result;
        try {
            result = declaredMethod.invoke(object, arguments);
        } catch (IllegalAccessException e) {
            return Result.failed(e);
        } catch (InvocationTargetException e) {
            return Result.failed(e.getCause());
        }

        if (result == null) {
            // TODO: return InvokeNullResult
            return null;
        }

        boolean isPrimitive = Primitives.isPrimitive(result.getClass())
                || result.getClass().isAssignableFrom(String.class)
                || result.getClass().isAssignableFrom(Integer.class)
                || result.getClass().isAssignableFrom(Long.class)
                || result.getClass().isAssignableFrom(Short.class)
                || result.getClass().isAssignableFrom(Byte.class)
                || result.getClass().isAssignableFrom(Character.class)
                || result.getClass().isAssignableFrom(Double.class);
        if (isPrimitive) {
            return Result.success(new InvokePrimitiveResult((Serializable) result, packetId));
        } else {
            String resultObjectId = String.valueOf(System.identityHashCode(result));
            ObjectStorage.getInstance().putObject(resultObjectId, result);


            List<String> resultInterfaces = Arrays.stream(result.getClass().getInterfaces())
                    .map(Class::getName).collect(Collectors.toList());

            InvokeResult invokeResult = new InvokeResult(
                    result.getClass().getName(),
                    resultInterfaces,
                    resultObjectId,
                    packetId
            );
            return Result.success(invokeResult);
        }
    }

    private Object[] getArguments(List<Invoke.ArgumentHolder> argumentHolders) {
        return argumentHolders.stream().map(argumentHolder -> {
           if (argumentHolder.getObjectId() == null) {
               if ("null".equals(argumentHolder.getClassName())) {
                   return null;
               }
               return argumentHolder.getValue();
           } else {
               return ObjectStorage.getInstance().getObject(argumentHolder.getObjectId());
           }
        }).toArray();
    }
}
