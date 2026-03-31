package distsys.smartbridge.health;

import distsys.smartbridge.grpc.BridgeHealthServiceGrpc;
import distsys.smartbridge.grpc.GetHealthStatusReq;
import distsys.smartbridge.grpc.GetHealthStatusRes;
import distsys.smartbridge.grpc.RiskLevel;
import distsys.smartbridge.grpc.SubscribeAlertsReq;
import distsys.smartbridge.grpc.SubscribeAlertsRes;
import io.grpc.stub.StreamObserver;
import java.time.LocalDateTime;

public class BridgeHealthServiceImpl extends BridgeHealthServiceGrpc.BridgeHealthServiceImplBase {

    @Override
    public void getHealthStatus(GetHealthStatusReq request, StreamObserver<GetHealthStatusRes> responseObserver) {
        String bridgeId = request.getBridgeId();

        double healthScore = 72.5;
        RiskLevel riskLevel = RiskLevel.MEDIUM;
        String recommendation = "Schedule inspection within 14 days";

        GetHealthStatusRes response = GetHealthStatusRes.newBuilder()
                .setBridgeId(bridgeId)
                .setHealthScore(healthScore)
                .setRiskLevel(riskLevel)
                .setRecommendation(recommendation)
                .setComputedAt(LocalDateTime.now().toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
public void subscribeAlerts(SubscribeAlertsReq request, StreamObserver<SubscribeAlertsRes> responseObserver) {
    String bridgeId = request.getBridgeId();

    try {
        SubscribeAlertsRes alert1 = SubscribeAlertsRes.newBuilder()
                .setBridgeId(bridgeId)
                .setRiskLevel(RiskLevel.MEDIUM)
                .setAlertCode("TEMP_WARNING")
                .setDetails("Temperature is approaching the warning threshold")
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        responseObserver.onNext(alert1);

        Thread.sleep(1000);

        SubscribeAlertsRes alert2 = SubscribeAlertsRes.newBuilder()
                .setBridgeId(bridgeId)
                .setRiskLevel(RiskLevel.HIGH)
                .setAlertCode("VIB_SPIKE")
                .setDetails("Vibration exceeded safe threshold")
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        responseObserver.onNext(alert2);

        Thread.sleep(1000);

        SubscribeAlertsRes alert3 = SubscribeAlertsRes.newBuilder()
                .setBridgeId(bridgeId)
                .setRiskLevel(RiskLevel.CRITICAL)
                .setAlertCode("TILT_CRITICAL")
                .setDetails("Tilt angle reached critical level")
                .setTimestamp(LocalDateTime.now().toString())
                .build();

        responseObserver.onNext(alert3);
        responseObserver.onCompleted();

    } catch (InterruptedException e) {
        responseObserver.onError(e);
        Thread.currentThread().interrupt();
    }
}
    }

