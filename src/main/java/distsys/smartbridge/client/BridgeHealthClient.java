package distsys.smartbridge.client;

import distsys.smartbridge.grpc.BridgeHealthServiceGrpc;
import distsys.smartbridge.grpc.DiscoverServiceReq;
import distsys.smartbridge.grpc.DiscoverServiceRes;
import distsys.smartbridge.grpc.GetHealthStatusReq;
import distsys.smartbridge.grpc.GetHealthStatusRes;
import distsys.smartbridge.grpc.NamingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BridgeHealthClient {

    public static void main(String[] args) {
        ManagedChannel namingChannel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .usePlaintext()
                .build();

        NamingServiceGrpc.NamingServiceBlockingStub namingStub =
                NamingServiceGrpc.newBlockingStub(namingChannel);

        DiscoverServiceRes serviceInfo = namingStub.discoverService(
                DiscoverServiceReq.newBuilder()
                        .setServiceName("BridgeHealthService")
                        .build()
        );

        namingChannel.shutdown();

        if (!serviceInfo.getFound()) {
            System.out.println("BridgeHealthService not found in Naming Service.");
            return;
        }

        ManagedChannel serviceChannel = ManagedChannelBuilder
                .forAddress(serviceInfo.getHost(), serviceInfo.getPort())
                .usePlaintext()
                .build();

        BridgeHealthServiceGrpc.BridgeHealthServiceBlockingStub blockingStub =
                BridgeHealthServiceGrpc.newBlockingStub(serviceChannel);

        GetHealthStatusReq request = GetHealthStatusReq.newBuilder()
                .setBridgeId("BR-001")
                .build();

        GetHealthStatusRes response = blockingStub.getHealthStatus(request);

        System.out.println("Service discovered via Naming Service:");
        System.out.println("Host: " + serviceInfo.getHost());
        System.out.println("Port: " + serviceInfo.getPort());
        System.out.println("Response received:");
        System.out.println("Bridge ID: " + response.getBridgeId());
        System.out.println("Health score: " + response.getHealthScore());
        System.out.println("Risk level: " + response.getRiskLevel());
        System.out.println("Recommendation: " + response.getRecommendation());
        System.out.println("Computed at: " + response.getComputedAt());

        serviceChannel.shutdown();
    }
}