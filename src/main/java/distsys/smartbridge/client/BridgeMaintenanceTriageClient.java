package distsys.smartbridge.client;

import distsys.smartbridge.grpc.BridgeMaintenanceServiceGrpc;
import distsys.smartbridge.grpc.AddComment;
import distsys.smartbridge.grpc.StatusUpdate;
import distsys.smartbridge.grpc.SubscribeTriage;
import distsys.smartbridge.grpc.TriageWorkOrderReq;
import distsys.smartbridge.grpc.TriageWorkOrderRes;
import distsys.smartbridge.grpc.WorkOrderStatus;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class BridgeMaintenanceTriageClient {

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50053)
                .usePlaintext()
                .build();

        BridgeMaintenanceServiceGrpc.BridgeMaintenanceServiceStub asyncStub =
                BridgeMaintenanceServiceGrpc.newStub(channel);

        StreamObserver<TriageWorkOrderRes> responseObserver = new StreamObserver<TriageWorkOrderRes>() {
            @Override
            public void onNext(TriageWorkOrderRes response) {
                if (response.hasMessage()) {
                    System.out.println("Message: " + response.getMessage().getText());
                    System.out.println("Timestamp: " + response.getMessage().getTimestamp());
                }

                if (response.hasEvent()) {
                    System.out.println("Event received:");
                    System.out.println("Work order ID: " + response.getEvent().getWorkOrderId());
                    System.out.println("Status: " + response.getEvent().getStatus());
                    System.out.println("Note: " + response.getEvent().getNote());
                    System.out.println("Timestamp: " + response.getEvent().getTimestamp());
                }

                System.out.println("----------------------------");
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Triage stream completed.");
            }
        };

        StreamObserver<TriageWorkOrderReq> requestObserver = asyncStub.triageWorkOrder(responseObserver);

        requestObserver.onNext(TriageWorkOrderReq.newBuilder()
                .setSubscribe(SubscribeTriage.newBuilder()
                        .setBridgeId("BR-001")
                        .build())
                .build());

        requestObserver.onNext(TriageWorkOrderReq.newBuilder()
                .setStatusUpdate(StatusUpdate.newBuilder()
                        .setWorkOrderId("WO-123456")
                        .setNewStatus(WorkOrderStatus.IN_PROGRESS)
                        .build())
                .build());

        requestObserver.onNext(TriageWorkOrderReq.newBuilder()
                .setComment(AddComment.newBuilder()
                        .setWorkOrderId("WO-123456")
                        .setAuthor("Engineer A")
                        .setText("Inspection team assigned")
                        .setTimestamp("2026-03-31T17:10:00")
                        .build())
                .build());

        requestObserver.onCompleted();

        Thread.sleep(4000);
        channel.shutdown();
    }
}