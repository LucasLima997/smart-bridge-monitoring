package distsys.smartbridge.common;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public class ClientIdInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> CLIENT_ID_HEADER =
            Metadata.Key.of("client-id", Metadata.ASCII_STRING_MARSHALLER);

    private final String clientId;

    public ClientIdInterceptor(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(CLIENT_ID_HEADER, clientId);
                super.start(responseListener, headers);
            }
        };
    }
}
