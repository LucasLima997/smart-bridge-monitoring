package distsys.smartbridge.common;

import io.grpc.*;

public class ServerLoggingInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> CLIENT_ID_HEADER =
            Metadata.Key.of("client-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String clientId = headers.get(CLIENT_ID_HEADER);

        if (clientId == null || clientId.isBlank()) {
            clientId = "unknown-client";
        }

        System.out.println("Incoming call: " + call.getMethodDescriptor().getFullMethodName()
                + " | client-id=" + clientId);

        return next.startCall(call, headers);
    }
}