package distsys.smartbridge.sensor;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class BridgeSensorServer {

    private Server server;

    public void start() throws IOException {
        int port = 50051;

        server = ServerBuilder.forPort(port)
                .addService(new BridgeSensorServiceImpl())
                .build()
                .start();

        System.out.println("BridgeSensorServer started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down BridgeSensorServer...");
            BridgeSensorServer.this.stop();
            System.out.println("BridgeSensorServer shut down.");
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
        BridgeSensorServer server = new BridgeSensorServer();
        server.start();
        server.blockUntilShutdown();
    }
}
