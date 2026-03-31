package distsys.smartbridge.client;

import distsys.smartbridge.grpc.BridgeMaintenanceServiceGrpc;
import distsys.smartbridge.grpc.CreateWorkOrderReq;
import distsys.smartbridge.grpc.CreateWorkOrderRes;
import distsys.smartbridge.grpc.Priority;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BridgeMaintenanceClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50053)
                .usePlaintext()
                .build();

        BridgeMaintenanceServiceGrpc.BridgeMaintenanceServiceBlockingStub blockingStub =
                BridgeMaintenanceServiceGrpc.newBlockingStub(channel);

        CreateWorkOrderReq request = CreateWorkOrderReq.newBuilder()
                .setBridgeId("BR-001")
                .setIssueCode("CRACK_SUSPECTED")
                .setDescription("Possible crack detected near joint A")
                .setPriority(Priority.P2_HIGH)
                .setRequestedBy("BridgeHealthService")
                .build();

        CreateWorkOrderRes response = blockingStub.createWorkOrder(request);

        System.out.println("Response received:");
        System.out.println("Work order ID: " + response.getWorkOrderId());
        System.out.println("Bridge ID: " + response.getBridgeId());
        System.out.println("Status: " + response.getStatus());
        System.out.println("Created at: " + response.getCreatedAt());
        System.out.println("Message: " + response.getMessage());

        channel.shutdown();
    }
}
