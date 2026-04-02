package distsys.smartbridge.client;

import distsys.smartbridge.grpc.BridgeSensorServiceGrpc;
import distsys.smartbridge.grpc.DiscoverServiceReq;
import distsys.smartbridge.grpc.DiscoverServiceRes;
import distsys.smartbridge.grpc.NamingServiceGrpc;
import distsys.smartbridge.grpc.SensorType;
import distsys.smartbridge.grpc.ValidateReadingReq;
import distsys.smartbridge.grpc.ValidateReadingRes;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BridgeSensorClient {

    public static void main(String[] args) {
        ManagedChannel namingChannel = ManagedChannelBuilder
                .forAddress("localhost", 50050)
                .usePlaintext()
                .build();

        NamingServiceGrpc.NamingServiceBlockingStub namingStub =
                NamingServiceGrpc.newBlockingStub(namingChannel);

        DiscoverServiceRes serviceInfo = namingStub.discoverService(
                DiscoverServiceReq.newBuilder()
                        .setServiceName("BridgeSensorService")
                        .build()
        );

        namingChannel.shutdown();

        if (!serviceInfo.getFound()) {
            System.out.println("BridgeSensorService not found in Naming Service.");
            return;
        }

        ManagedChannel serviceChannel = ManagedChannelBuilder
                .forAddress(serviceInfo.getHost(), serviceInfo.getPort())
                .usePlaintext()
                .build();

        BridgeSensorServiceGrpc.BridgeSensorServiceBlockingStub blockingStub =
                BridgeSensorServiceGrpc.newBlockingStub(serviceChannel);

        ValidateReadingReq request = ValidateReadingReq.newBuilder()
                .setBridgeId("BR-001")
                .setSensorId("TEMP-01")
                .setSensorType(SensorType.TEMPERATURE)
                .setValue(22.5)
                .setUnit("C")
                .build();

        ValidateReadingRes response = blockingStub.validateReading(request);

        System.out.println("Service discovered via Naming Service:");
        System.out.println("Host: " + serviceInfo.getHost());
        System.out.println("Port: " + serviceInfo.getPort());
        System.out.println("Response received:");
        System.out.println("Valid: " + response.getIsValid());
        System.out.println("Message: " + response.getMessage());

        serviceChannel.shutdown();
    }
}