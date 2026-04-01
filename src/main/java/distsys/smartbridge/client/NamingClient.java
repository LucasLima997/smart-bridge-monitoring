package distsys.smartbridge.client;

import distsys.smartbridge.grpc.DiscoverServiceReq;
import distsys.smartbridge.grpc.DiscoverServiceRes;
import distsys.smartbridge.grpc.ListServicesReq;
import distsys.smartbridge.grpc.ListServicesRes;
import distsys.smartbridge.grpc.NamingServiceGrpc;
import distsys.smartbridge.grpc.RegisterServiceReq;
import distsys.smartbridge.grpc.RegisterServiceRes;
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

        RegisterServiceReq registerRequest = RegisterServiceReq.newBuilder()
                .setServiceName("BridgeSensorService")
                .setHost("localhost")
                .setPort(50051)
                .setDescription("Sensor data upload and validation service")
                .build();

        RegisterServiceRes registerResponse = blockingStub.registerService(registerRequest);
        System.out.println("Register response: " + registerResponse.getMessage());

        DiscoverServiceReq discoverRequest = DiscoverServiceReq.newBuilder()
                .setServiceName("BridgeSensorService")
                .build();

        DiscoverServiceRes discoverResponse = blockingStub.discoverService(discoverRequest);
        System.out.println("Discovered: " + discoverResponse.getFound());
        System.out.println("Host: " + discoverResponse.getHost());
        System.out.println("Port: " + discoverResponse.getPort());

        ListServicesRes listResponse = blockingStub.listServices(ListServicesReq.newBuilder().build());
        System.out.println("Registered services:");
        for (ServiceInfo service : listResponse.getServicesList()) {
            System.out.println(service.getServiceName() + " -> " + service.getHost() + ":" + service.getPort());
        }

        channel.shutdown();
    }
}
