package de.uniks.stp.wedoit.accord.client.network.spotify;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractSpotifyAuthorization implements HttpHandler {

    public ExecutorService executorService;
    private HttpServer server;

    public AbstractSpotifyAuthorization() {
        this.executorService = Executors.newCachedThreadPool();
    }

    protected void authorize() {
        try {
            this.server = HttpServer.create(new InetSocketAddress("localhost", 3000), 0);
            this.server.createContext("/spotify", this);
            this.server.setExecutor(executorService);
            this.server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void stopServer() {
        server.stop(0);
        executorService.shutdown();
    }
}
