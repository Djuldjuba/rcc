package io.student.rcc.userdata.config;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

@GrpcGlobalServerInterceptor
public class JwtServerInterceptor implements ServerInterceptor {

    public static final Metadata.Key<String> AUTH_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> USERNAME_KEY =
            Context.key("username");

    private final JwtDecoder jwtDecoder;

    public JwtServerInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String header = headers.get(AUTH_KEY);
        if (header == null || !header.startsWith("Bearer ")) {
            call.close(Status.UNAUTHENTICATED
                    .withDescription("Missing Authorization header"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        try {
            Jwt jwt = jwtDecoder.decode(header.substring(7));
            String username = jwt.getClaim("sub");
            if (username == null || username.isBlank()) {
                call.close(Status.UNAUTHENTICATED
                        .withDescription("JWT has no sub claim"), new Metadata());
                return new ServerCall.Listener<>() {};
            }
            Context ctx = Context.current().withValue(USERNAME_KEY, username);
            return Contexts.interceptCall(ctx, call, headers, next);
        } catch (JwtException e) {
            call.close(Status.UNAUTHENTICATED
                    .withDescription("Invalid JWT: " + e.getMessage()), new Metadata());
            return new ServerCall.Listener<>() {};
        }
    }
}