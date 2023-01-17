package com.tyron.tooling.server;

import com.google.gson.Gson;
import com.tyron.tooling.model.CodeAssistGradleConnector;
import com.tyron.tooling.packet.Invoke;
import com.tyron.tooling.packet.InvokeExceptionResult;
import com.tyron.tooling.packet.Packet;
import com.tyron.tooling.packet.handler.PacketHandler;
import com.tyron.tooling.packet.handler.Result;
import com.tyron.tooling.packet.serializer.GsonHelper;
import com.tyron.tooling.server.handler.InvokeHandler;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends Packet>, PacketHandler> packetHandlerMap = new ConcurrentHashMap<>();

    static {
        packetHandlerMap.put(Invoke.class, new InvokeHandler());
    }

    private final ServerSocket serverSocket = new ServerSocket(255);
    private DataOutputStream socketOutputStream;


    public Server() throws IOException {

    }

    public void start() {
        new Thread(() -> {
            try {
                startInternal();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startInternal() throws IOException {
        try (Socket socket = serverSocket.accept()) {

            DataInputStream din = new DataInputStream(socket.getInputStream());
            socketOutputStream = new DataOutputStream(socket.getOutputStream());

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
                        // all packets exist on both classpath
                        // thus, it is considered as a bug if it doesn't
                        throw new IllegalArgumentException("Unknown packet: " + packet.getType());
                    }

                    Result<?> handleResult = handle(packet);
                    if (handleResult == null) {
                        continue;
                    }
                    if (handleResult.isSuccessful()) {
                        Object result = handleResult.getResult();
                        if (result instanceof Packet) {
                            sendPacket(((Packet) result));
                        }
                    } else {
                        Throwable exception = handleResult.getException();
                        InvokeExceptionResult exceptionPacket = new InvokeExceptionResult(exception.getMessage(), packet.getPacketId());
                        sendPacket(exceptionPacket);
                    }
                }
            }
        }
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    private Result<?> handle(Packet packet) {
        PacketHandler packetHandler = packetHandlerMap.get(packet.getClass());
        if (packetHandler == null) {
            System.out.println("No packet.handler for packet: " + packet);
            return null;
        }

        return packetHandler.handle(packet);
    }

    private void sendPacket(Packet packet) throws IOException {
        System.out.println("[server.Server] Send packet: " + packet);

        String gsonLine = GsonHelper.getGson().toJson(packet);
        socketOutputStream.writeUTF(gsonLine);
    }

    public static CodeAssistGradleConnector newGradleConnector() {
        return new CodeAssistGradleConnector() {
            private final GradleConnector internal = GradleConnector.newConnector();

            @Override
            public CodeAssistGradleConnector forProjectDirectory(File projectDir) {
                internal.forProjectDirectory(projectDir);
                return this;
            }

            @Override
            public ProjectConnection connect() {
                return internal.connect();
            }
        };

    }

}
