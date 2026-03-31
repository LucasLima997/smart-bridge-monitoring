package distsys.smartbridge.maintenance;

import distsys.smartbridge.grpc.BridgeMaintenanceServiceGrpc;
import distsys.smartbridge.grpc.CreateWorkOrderReq;
import distsys.smartbridge.grpc.CreateWorkOrderRes;
import distsys.smartbridge.grpc.TriageMessage;
import distsys.smartbridge.grpc.TriageWorkOrderReq;
import distsys.smartbridge.grpc.TriageWorkOrderRes;
import distsys.smartbridge.grpc.WorkOrderEvent;
import distsys.smartbridge.grpc.WorkOrderStatus;
import io.grpc.stub.StreamObserver;
import java.time.LocalDateTime;

public class BridgeMaintenanceServiceImpl extends BridgeMaintenanceServiceGrpc.BridgeMaintenanceServiceImplBase {

    @Override
    public void createWorkOrder(CreateWorkOrderReq request, StreamObserver<CreateWorkOrderRes> responseObserver) {
        String workOrderId = "WO-" + System.currentTimeMillis();

        CreateWorkOrderRes response = CreateWorkOrderRes.newBuilder()
                .setWorkOrderId(workOrderId)
                .setBridgeId(request.getBridgeId())
                .setStatus(WorkOrderStatus.OPEN)
                .setCreatedAt(LocalDateTime.now().toString())
                .setMessage("Work order successfully created")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<TriageWorkOrderReq> triageWorkOrder(StreamObserver<TriageWorkOrderRes> responseObserver) {
        return new StreamObserver<TriageWorkOrderReq>() {
            @Override
            public void onNext(TriageWorkOrderReq request) {
                if (request.hasSubscribe()) {
                    TriageWorkOrderRes response = TriageWorkOrderRes.newBuilder()
                            .setMessage(TriageMessage.newBuilder()
                                    .setText("Subscribed to triage channel for bridge " + request.getSubscribe().getBridgeId())
                                    .setTimestamp(LocalDateTime.now().toString())
                                    .build())
                            .build();

                    responseObserver.onNext(response);

                } else if (request.hasStatusUpdate()) {
                    TriageWorkOrderRes response = TriageWorkOrderRes.newBuilder()
                            .setEvent(WorkOrderEvent.newBuilder()
                                    .setWorkOrderId(request.getStatusUpdate().getWorkOrderId())
                                    .setStatus(request.getStatusUpdate().getNewStatus())
                                    .setNote("Status updated successfully")
                                    .setTimestamp(LocalDateTime.now().toString())
                                    .build())
                            .build();

                    responseObserver.onNext(response);

                } else if (request.hasComment()) {
                    TriageWorkOrderRes response = TriageWorkOrderRes.newBuilder()
                            .setMessage(TriageMessage.newBuilder()
                                    .setText("Comment added by " + request.getComment().getAuthor())
                                    .setTimestamp(LocalDateTime.now().toString())
                                    .build())
                            .build();

                    responseObserver.onNext(response);
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Triage stream error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}