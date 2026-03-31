package distsys.smartbridge.client;

import distsys.smartbridge.grpc.BridgeSensorServiceGrpc;
import distsys.smartbridge.grpc.SensorType;
import distsys.smartbridge.grpc.UploadReadingsReq;
import distsys.smartbridge.grpc.UploadReadingsRes;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class BridgeSensorStreamClient {

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        BridgeSensorServiceGrpc.BridgeSensorServiceStub asyncStub =
                BridgeSensorServiceGrpc.newStub(channel);

        StreamObserver<UploadReadingsRes> responseObserver = new StreamObserver<UploadReadingsRes>() {
            @Override
            public void onNext(UploadReadingsRes response) {
                System.out.println("Summary received:");
                System.out.println("Bridge ID: " + response.getBridgeId());
                System.out.println("Total received: " + response.getTotalReceived());
                System.out.println("Accepted: " + response.getAccepted());
                System.out.println("Rejected: " + response.getRejected());
                System.out.println("Min value: " + response.getMinValue());
                System.out.println("Max value: " + response.getMaxValue());
                System.out.println("Status: " + response.getStatus());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Stream completed.");
            }
        };

        StreamObserver<UploadReadingsReq> requestObserver = asyncStub.uploadReadings(responseObserver);

        requestObserver.onNext(UploadReadingsReq.newBuilder()
                .setBridgeId("BR-001")
                .setSensorId("VIB-01")
                .setSensorType(SensorType.VIBRATION)
                .setValue(0.85)
                .setUnit("g")
                .setTimestamp("2026-03-31T13:40:00")
                .build());

        requestObserver.onNext(UploadReadingsReq.newBuilder()
                .setBridgeId("BR-001")
                .setSensorId("VIB-02")
                .setSensorType(SensorType.VIBRATION)
                .setValue(1.20)
                .setUnit("g")
                .setTimestamp("2026-03-31T13:40:05")
                .build());

        requestObserver.onNext(UploadReadingsReq.newBuilder()
                .setBridgeId("BR-001")
                .setSensorId("VIB-03")
                .setSensorType(SensorType.VIBRATION)
                .setValue(6.50)
                .setUnit("g")
                .setTimestamp("2026-03-31T13:40:10")
                .build());

        requestObserver.onCompleted();

        Thread.sleep(2000);
        channel.shutdown();
    }
}