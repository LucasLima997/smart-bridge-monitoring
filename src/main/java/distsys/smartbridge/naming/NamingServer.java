package distsys.smartbridge.naming;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class NamingServer {

    private Server server;

    public void start() throws IOException {
        int port = 50050;

        server = ServerBuilder.forPort(port)
                .addService(new NamingServiceImpl())
                .build()
                .start();

        System.out.println("NamingServer started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down NamingServer...");
            NamingServer.this.stop();
            System.out.println("NamingServer shut down.");
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
        NamingServer server = new NamingServer();
        server.start();
        server.blockUntilShutdown();
    }
}
