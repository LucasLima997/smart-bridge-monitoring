package distsys.smartbridge.maintenance;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class BridgeMaintenanceServer {

    private Server server;

    public void start() throws IOException {
        int port = 50053;

        server = ServerBuilder.forPort(port)
                .addService(new BridgeMaintenanceServiceImpl())
                .build()
                .start();

        System.out.println("BridgeMaintenanceServer started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down BridgeMaintenanceServer...");
            BridgeMaintenanceServer.this.stop();
            System.out.println("BridgeMaintenanceServer shut down.");
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
        BridgeMaintenanceServer server = new BridgeMaintenanceServer();
        server.start();
        server.blockUntilShutdown();
    }
}