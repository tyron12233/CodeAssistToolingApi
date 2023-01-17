package com.tyron.tooling;

import com.tyron.tooling.client.Client;
import com.tyron.tooling.model.CodeAssistGradleConnector;
import com.tyron.tooling.packet.Invoke;
import com.tyron.tooling.server.Server;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;


public class Main {

    /**
     * GradleConnector is not an interface thus, we create our own interface which the server implements
     * and this will then delegate calls to the real GradleConnector.
     */
    public static CodeAssistGradleConnector newGradleConnector(Client client) {
        Invoke invokePacket = new Invoke(null, Server.class.getName(), "newGradleConnector", "1");
        try {
            CompletableFuture<Object> methodFuture = client.sendPacket(invokePacket);
            return (CodeAssistGradleConnector) methodFuture.get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.start();
        Client client = new Client();
        client.start();

        // all classes here are now proxies

        ProjectConnection projectConnection = Main.newGradleConnector(client)
                .forProjectDirectory(new File(""))
                .connect();
        GradleProject model = projectConnection.getModel(GradleProject.class);

        assert "CodeAssistToolingApi".equals(model.getName());

        DomainObjectSet<? extends GradleTask> tasks = model.getTasks();

        for (GradleTask task : tasks) {
            System.out.println("Task: " + task.getName());
        }

    }
}
