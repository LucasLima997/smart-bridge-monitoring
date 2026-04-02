package distsys.smartbridge.client;

import distsys.smartbridge.grpc.BridgeMaintenanceServiceGrpc;
import distsys.smartbridge.grpc.CreateWorkOrderReq;
import distsys.smartbridge.grpc.CreateWorkOrderRes;
import distsys.smartbridge.grpc.DiscoverServiceReq;
import distsys.smartbridge.grpc.DiscoverServiceRes;
import distsys.smartbridge.grpc.NamingServiceGrpc;
import distsys.smartbridge.grpc.Priority;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BridgeMaintenanceClient {

    public static void main(String[] args) {
        ManagedChannel namingChannel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .usePlaintext()
                .build();

        NamingServiceGrpc.NamingServiceBlockingStub namingStub =
                NamingServiceGrpc.newBlockingStub(namingChannel);

        DiscoverServiceRes serviceInfo = namingStub.discoverService(
                DiscoverServiceReq.newBuilder()
                        .setServiceName("BridgeMaintenanceService")
                        .build()
        );

        namingChannel.shutdown();

        if (!serviceInfo.getFound()) {
            System.out.println("BridgeMaintenanceService not found in Naming Service.");
            return;
        }

        ManagedChannel serviceChannel = ManagedChannelBuilder
                .forAddress(serviceInfo.getHost(), serviceInfo.getPort())
                .usePlaintext()
                .build();

        BridgeMaintenanceServiceGrpc.BridgeMaintenanceServiceBlockingStub blockingStub =
                BridgeMaintenanceServiceGrpc.newBlockingStub(serviceChannel);

        CreateWorkOrderReq request = CreateWorkOrderReq.newBuilder()
                .setBridgeId("BR-001")
                .setIssueCode("CRACK_SUSPECTED")
                .setDescription("Possible crack detected near joint A")
                .setPriority(Priority.P2_HIGH)
                .setRequestedBy("BridgeHealthService")
                .build();

        CreateWorkOrderRes response = blockingStub.createWorkOrder(request);

        System.out.println("Service discovered via Naming Service:");
        System.out.println("Host: " + serviceInfo.getHost());
        System.out.println("Port: " + serviceInfo.getPort());
        System.out.println("Response received:");
        System.out.println("Work order ID: " + response.getWorkOrderId());
        System.out.println("Bridge ID: " + response.getBridgeId());
        System.out.println("Status: " + response.getStatus());
        System.out.println("Created at: " + response.getCreatedAt());
        System.out.println("Message: " + response.getMessage());

        serviceChannel.shutdown();
    }
}
