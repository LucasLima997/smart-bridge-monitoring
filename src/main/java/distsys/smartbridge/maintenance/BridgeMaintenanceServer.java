package distsys.smartbridge.maintenance;

import distsys.smartbridge.grpc.NamingServiceGrpc;
import distsys.smartbridge.grpc.RegisterServiceReq;
import distsys.smartbridge.grpc.RegisterServiceRes;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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

        registerWithNamingService("BridgeMaintenanceService", "localhost", port,
                "Maintenance work order creation and triage service");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down BridgeMaintenanceServer...");
            BridgeMaintenanceServer.this.stop();
            System.out.println("BridgeMaintenanceServer shut down.");
        }));
    }

    private void registerWithNamingService(String serviceName, String host, int port, String description) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .usePlaintext()
                .build();

        try {
            NamingServiceGrpc.NamingServiceBlockingStub blockingStub =
                    NamingServiceGrpc.newBlockingStub(channel);

            RegisterServiceReq request = RegisterServiceReq.newBuilder()
                    .setServiceName(serviceName)
                    .setHost(host)
                    .setPort(port)
                    .setDescription(description)
                    .build();

            RegisterServiceRes response = blockingStub.registerService(request);
            System.out.println("Naming registration: " + response.getMessage());
        } finally {
            channel.shutdown();
        }
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