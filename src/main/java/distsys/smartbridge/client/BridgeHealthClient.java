package distsys.smartbridge.client;

import distsys.smartbridge.grpc.BridgeHealthServiceGrpc;
import distsys.smartbridge.grpc.GetHealthStatusReq;
import distsys.smartbridge.grpc.GetHealthStatusRes;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BridgeHealthClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        BridgeHealthServiceGrpc.BridgeHealthServiceBlockingStub blockingStub =
                BridgeHealthServiceGrpc.newBlockingStub(channel);

        GetHealthStatusReq request = GetHealthStatusReq.newBuilder()
                .setBridgeId("BR-001")
                .build();

        GetHealthStatusRes response = blockingStub.getHealthStatus(request);

        System.out.println("Response received:");
        System.out.println("Bridge ID: " + response.getBridgeId());
        System.out.println("Health score: " + response.getHealthScore());
        System.out.println("Risk level: " + response.getRiskLevel());
        System.out.println("Recommendation: " + response.getRecommendation());
        System.out.println("Computed at: " + response.getComputedAt());

        channel.shutdown();
    }
}