package distsys.smartbridge.common;

import distsys.smartbridge.grpc.BridgeSensorServiceGrpc;
import distsys.smartbridge.grpc.ValidateReadingReq;

public class ProtoCheck {
    public static void main(String[] args) {
        ValidateReadingReq request = ValidateReadingReq.newBuilder()
                .setBridgeId("BR-001")
                .setSensorId("TEMP-01")
                .setValue(22.5)
                .setUnit("C")
                .build();

        System.out.println("Proto loaded successfully!");
        System.out.println(request);
        System.out.println(BridgeSensorServiceGrpc.class.getSimpleName());
    }
}