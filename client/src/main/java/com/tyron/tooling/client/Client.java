package com.tyron.tooling.client;


import com.google.gson.Gson;
import com.tyron.tooling.client.handler.InvokeExceptionResultHandler;
import com.tyron.tooling.client.handler.InvokePrimitiveResultHandler;
import com.tyron.tooling.client.handler.InvokeResultHandler;
import com.tyron.tooling.packet.InvokeExceptionResult;
import com.tyron.tooling.packet.InvokePrimitiveResult;
import com.tyron.tooling.packet.InvokeResult;
import com.tyron.tooling.packet.Packet;
import com.tyron.tooling.packet.handler.PacketHandler;
import com.tyron.tooling.packet.handler.Result;
import com.tyron.tooling.packet.serializer.GsonHelper;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Client {

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends Packet>, PacketHandler> packetHandlerMap = new ConcurrentHashMap<>();


    private final Socket socket = new Socket("localhost", 255);
    DataInputStream din = new DataInputStream(socket.getInputStream());
    DataOutputStream dout = new DataOutputStream(socket.getOutputStream());

    private final Map<String, CompletableFuture<Object>> packetConsumers = new ConcurrentHashMap<>();

    private boolean listening = true;

    public Client() throws IOException {
        packetHandlerMap.put(InvokeResult.class, new InvokeResultHandler(this));
        packetHandlerMap.put(InvokePrimitiveResult.class, new InvokePrimitiveResultHandler());
        packetHandlerMap.put(InvokeExceptionResult.class, new InvokeExceptionResultHandler());
    }

    public void start() {
        new Thread(() -> {
            try {
                listen();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void listen() throws IOException {

        String currentLine;
        while (socket.isConnected()) {
            currentLine = din.readUTF();

            if (!currentLine.isEmpty()) {
                Packet packet = GsonHelper.getGson().fromJson(currentLine, Packet.class);
                try {
                    //noinspection unchecked
                    Class<? extends Packet> packetClass = (Class<? extends Packet>) Class.forName(packet.getType());

                    packet = GsonHelper.getGson().fromJson(currentLine, packetClass);
                } catch (ClassNotFoundException e) {
                    System.out.println("Unknown packet: " + packet.getType());
                    throw new IllegalArgumentException("Unknown packet: " + packet.getType());
                }
                handle(packet);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void handle(Packet packet) {
        PacketHandler packetHandler = packetHandlerMap.get(packet.getClass());
        if (packetHandler == null) {
            System.out.println("No packet handler for: " + packet.getType());
            return;
        }

        Result<?> handle = packetHandler.handle(packet);

        String packetId = packet.getPacketId();
        CompletableFuture<Object> future = packetConsumers.get(packetId);
        if (future == null) {
            System.out.println("Warning: No consumers registered for " + packetId);
            return;
        }

        if (handle.isSuccessful()) {
            future.complete(handle.getResult());
        } else {
            future.completeExceptionally(handle.getException());
        }
    }

    public CompletableFuture<Object> sendPacket(Packet packet) throws IOException {
        if (packetConsumers.containsKey(packet.getPacketId())) {
            throw new IllegalArgumentException("Duplicate packet ID");
        }

        System.out.println("[Client] Send packet: " + packet);

        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        packetConsumers.put(packet.getPacketId(), completableFuture);

        String gsonLine = GsonHelper.getGson().toJson(packet);
        dout.writeUTF(gsonLine);

        return completableFuture;
    }
}
