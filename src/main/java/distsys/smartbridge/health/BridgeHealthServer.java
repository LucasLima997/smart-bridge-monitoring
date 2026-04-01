package distsys.smartbridge.health;

import distsys.smartbridge.grpc.NamingServiceGrpc;
import distsys.smartbridge.grpc.RegisterServiceReq;
import distsys.smartbridge.grpc.RegisterServiceRes;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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

        registerWithNamingService("BridgeHealthService", "localhost", port,
                "Bridge health status and live alerts service");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down BridgeHealthServer...");
            BridgeHealthServer.this.stop();
            System.out.println("BridgeHealthServer shut down.");
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
        BridgeHealthServer server = new BridgeHealthServer();
        server.start();
        server.blockUntilShutdown();
    }
}