package distsys.smartbridge.naming;

import distsys.smartbridge.grpc.DiscoverServiceReq;
import distsys.smartbridge.grpc.DiscoverServiceRes;
import distsys.smartbridge.grpc.ListServicesReq;
import distsys.smartbridge.grpc.ListServicesRes;
import distsys.smartbridge.grpc.NamingServiceGrpc;
import distsys.smartbridge.grpc.RegisterServiceReq;
import distsys.smartbridge.grpc.RegisterServiceRes;
import distsys.smartbridge.grpc.ServiceInfo;
import io.grpc.stub.StreamObserver;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NamingServiceImpl extends NamingServiceGrpc.NamingServiceImplBase {

    private final Map<String, ServiceInfo> registry = new ConcurrentHashMap<>();

    @Override
    public void registerService(RegisterServiceReq request, StreamObserver<RegisterServiceRes> responseObserver) {
        ServiceInfo serviceInfo = ServiceInfo.newBuilder()
                .setServiceName(request.getServiceName())
                .setHost(request.getHost())
                .setPort(request.getPort())
                .setDescription(request.getDescription())
                .build();

        registry.put(request.getServiceName(), serviceInfo);

        RegisterServiceRes response = RegisterServiceRes.newBuilder()
                .setSuccess(true)
                .setMessage("Service registered successfully: " + request.getServiceName())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void discoverService(DiscoverServiceReq request, StreamObserver<DiscoverServiceRes> responseObserver) {
        ServiceInfo serviceInfo = registry.get(request.getServiceName());

        DiscoverServiceRes.Builder responseBuilder = DiscoverServiceRes.newBuilder();

        if (serviceInfo != null) {
            responseBuilder
                    .setFound(true)
                    .setServiceName(serviceInfo.getServiceName())
                    .setHost(serviceInfo.getHost())
                    .setPort(serviceInfo.getPort())
                    .setDescription(serviceInfo.getDescription());
        } else {
            responseBuilder
                    .setFound(false)
                    .setServiceName(request.getServiceName());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listServices(ListServicesReq request, StreamObserver<ListServicesRes> responseObserver) {
        ListServicesRes response = ListServicesRes.newBuilder()
                .addAllServices(registry.values())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
