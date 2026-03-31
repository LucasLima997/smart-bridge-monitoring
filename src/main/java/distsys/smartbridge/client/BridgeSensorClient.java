package distsys.smartbridge.client;

import distsys.smartbridge.grpc.BridgeSensorServiceGrpc;
import distsys.smartbridge.grpc.SensorType;
import distsys.smartbridge.grpc.ValidateReadingReq;
import distsys.smartbridge.grpc.ValidateReadingRes;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BridgeSensorClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        BridgeSensorServiceGrpc.BridgeSensorServiceBlockingStub blockingStub =
                BridgeSensorServiceGrpc.newBlockingStub(channel);

        ValidateReadingReq request = ValidateReadingReq.newBuilder()
                .setBridgeId("BR-001")
                .setSensorId("TEMP-01")
                .setSensorType(SensorType.TEMPERATURE)
                .setValue(22.5)
                .setUnit("C")
                .build();

        ValidateReadingRes response = blockingStub.validateReading(request);

        System.out.println("Response received:");
        System.out.println("Valid: " + response.getIsValid());
        System.out.println("Message: " + response.getMessage());

        channel.shutdown();
    }
}