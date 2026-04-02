package distsys.smartbridge.sensor;

import distsys.smartbridge.grpc.NamingServiceGrpc;
import distsys.smartbridge.grpc.RegisterServiceReq;
import distsys.smartbridge.grpc.RegisterServiceRes;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import distsys.smartbridge.common.ServerLoggingInterceptor;
import distsys.smartbridge.common.ClientIdInterceptor;
import io.grpc.ClientInterceptors;

public class BridgeSensorServer {

    private Server server;

    public void start() throws IOException {
        int port = 50051;

        server = ServerBuilder.forPort(port)
        .addService(io.grpc.ServerInterceptors.intercept(
                new BridgeSensorServiceImpl(),
                new ServerLoggingInterceptor()))
        .build()
        .start();

        System.out.println("BridgeSensorServer started on port " + port);

        registerWithNamingService("BridgeSensorService", "localhost", port,
                "Sensor data upload and validation service");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down BridgeSensorServer...");
            BridgeSensorServer.this.stop();
            System.out.println("BridgeSensorServer shut down.");
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
        BridgeSensorServer server = new BridgeSensorServer();
        server.start();
        server.blockUntilShutdown();
    }
}
