package distsys.smartbridge.sensor;

import distsys.smartbridge.grpc.BridgeSensorServiceGrpc;
import distsys.smartbridge.grpc.SensorType;
import distsys.smartbridge.grpc.UploadReadingsReq;
import distsys.smartbridge.grpc.UploadReadingsRes;
import distsys.smartbridge.grpc.ValidateReadingReq;
import distsys.smartbridge.grpc.ValidateReadingRes;
import io.grpc.stub.StreamObserver;

public class BridgeSensorServiceImpl extends BridgeSensorServiceGrpc.BridgeSensorServiceImplBase {

    @Override
    public void validateReading(ValidateReadingReq request, StreamObserver<ValidateReadingRes> responseObserver) {
        boolean valid = isReadingValid(request.getSensorType(), request.getValue());

        String message = valid ? "Reading is within safe limits." : "Reading is outside safe limits.";

        ValidateReadingRes response = ValidateReadingRes.newBuilder()
                .setIsValid(valid)
                .setMessage(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<UploadReadingsReq> uploadReadings(StreamObserver<UploadReadingsRes> responseObserver) {
        return new StreamObserver<UploadReadingsReq>() {

            private String bridgeId = "";
            private int totalReceived = 0;
            private int accepted = 0;
            private int rejected = 0;
            private double minValue = Double.MAX_VALUE;
            private double maxValue = Double.MIN_VALUE;

            @Override
            public void onNext(UploadReadingsReq request) {
                bridgeId = request.getBridgeId();
                totalReceived++;

                double value = request.getValue();
                boolean valid = isReadingValid(request.getSensorType(), value);

                if (valid) {
                    accepted++;
                    minValue = Math.min(minValue, value);
                    maxValue = Math.max(maxValue, value);
                } else {
                    rejected++;
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error receiving sensor stream: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                if (accepted == 0) {
                    minValue = 0.0;
                    maxValue = 0.0;
                }

                String status;
                if (accepted == totalReceived) {
                    status = "OK";
                } else if (accepted > 0) {
                    status = "PARTIAL_OK";
                } else {
                    status = "FAILED";
                }

                UploadReadingsRes response = UploadReadingsRes.newBuilder()
                        .setBridgeId(bridgeId)
                        .setTotalReceived(totalReceived)
                        .setAccepted(accepted)
                        .setRejected(rejected)
                        .setMinValue(minValue)
                        .setMaxValue(maxValue)
                        .setStatus(status)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    private boolean isReadingValid(SensorType sensorType, double value) {
        switch (sensorType) {
            case STRAIN:
                return value >= 0.0 && value <= 100.0;
            case VIBRATION:
                return value >= 0.0 && value <= 5.0;
            case TEMPERATURE:
                return value >= -20.0 && value <= 60.0;
            case TILT:
                return value >= 0.0 && value <= 15.0;
            default:
                return false;
        }
    }
}