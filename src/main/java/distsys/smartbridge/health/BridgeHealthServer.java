package distsys.smartbridge.health;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class BridgeHealthServer {

    private Server server;

    public void start() throws IOException {
        int port = 50052;

        server = ServerBuilder.forPort(port)
                .addService(new BridgeHealthServiceImpl())
                .build()
                .start();

        System.out.println("BridgeHealthServer started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down BridgeHealthServer...");
            BridgeHealthServer.this.stop();
            System.out.println("BridgeHealthServer shut down.");
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        BridgeHealthServer server = new BridgeHealthServer();
        server.start();
        server.blockUntilShutdown();
    }
}