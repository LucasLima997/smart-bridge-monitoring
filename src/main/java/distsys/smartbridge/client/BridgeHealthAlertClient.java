package distsys.smartbridge.client;

import distsys.smartbridge.grpc.BridgeHealthServiceGrpc;
import distsys.smartbridge.grpc.SubscribeAlertsReq;
import distsys.smartbridge.grpc.SubscribeAlertsRes;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class BridgeHealthAlertClient {

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        BridgeHealthServiceGrpc.BridgeHealthServiceStub asyncStub =
                BridgeHealthServiceGrpc.newStub(channel);

        SubscribeAlertsReq request = SubscribeAlertsReq.newBuilder()
                .setBridgeId("BR-001")
                .build();

        asyncStub.subscribeAlerts(request, new StreamObserver<SubscribeAlertsRes>() {
            @Override
            public void onNext(SubscribeAlertsRes alert) {
                System.out.println("Alert received:");
                System.out.println("Bridge ID: " + alert.getBridgeId());
                System.out.println("Risk level: " + alert.getRiskLevel());
                System.out.println("Alert code: " + alert.getAlertCode());
                System.out.println("Details: " + alert.getDetails());
                System.out.println("Timestamp: " + alert.getTimestamp());
                System.out.println("----------------------------");
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Alert stream completed.");
            }
        });

        Thread.sleep(5000);
        channel.shutdown();
    }
}