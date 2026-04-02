package distsys.smartbridge.client;

import distsys.smartbridge.grpc.DiscoverServiceReq;
import distsys.smartbridge.grpc.DiscoverServiceRes;
import distsys.smartbridge.grpc.ListServicesReq;
import distsys.smartbridge.grpc.ListServicesRes;
import distsys.smartbridge.grpc.NamingServiceGrpc;
import distsys.smartbridge.grpc.ServiceInfo;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class NamingClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .usePlaintext()
                .build();

        NamingServiceGrpc.NamingServiceBlockingStub blockingStub =
                NamingServiceGrpc.newBlockingStub(channel);

        ListServicesRes listResponse = blockingStub.listServices(ListServicesReq.newBuilder().build());

        System.out.println("Registered services:");
        for (ServiceInfo service : listResponse.getServicesList()) {
            System.out.println("-----------------------------------");
            System.out.println("Service Name: " + service.getServiceName());
            System.out.println("Host: " + service.getHost());
            System.out.println("Port: " + service.getPort());
            System.out.println("Description: " + service.getDescription());
        }

        System.out.println("-----------------------------------");

        DiscoverServiceRes sensorResponse = blockingStub.discoverService(
                DiscoverServiceReq.newBuilder()
                        .setServiceName("BridgeSensorService")
                        .build()
        );

        System.out.println("Discover BridgeSensorService:");
        System.out.println("Found: " + sensorResponse.getFound());
        System.out.println("Host: " + sensorResponse.getHost());
        System.out.println("Port: " + sensorResponse.getPort());

        DiscoverServiceRes healthResponse = blockingStub.discoverService(
                DiscoverServiceReq.newBuilder()
                        .setServiceName("BridgeHealthService")
                        .build()
        );

        System.out.println("Discover BridgeHealthService:");
        System.out.println("Found: " + healthResponse.getFound());
        System.out.println("Host: " + healthResponse.getHost());
        System.out.println("Port: " + healthResponse.getPort());

        DiscoverServiceRes maintenanceResponse = blockingStub.discoverService(
                DiscoverServiceReq.newBuilder()
                        .setServiceName("BridgeMaintenanceService")
                        .build()
        );

        System.out.println("Discover BridgeMaintenanceService:");
        System.out.println("Found: " + maintenanceResponse.getFound());
        System.out.println("Host: " + maintenanceResponse.getHost());
        System.out.println("Port: " + maintenanceResponse.getPort());

        channel.shutdown();
    }
}
