package io.student.rcc.userdata.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.student.rcc.userdata.config.JwtServerInterceptor;
import io.student.rcc.userdata.domain.UpdateUserRequest;
import io.student.rcc.userdata.domain.UserResponse;
import io.student.rcc.userdata.exception.ResourceNotFoundException;
import io.student.rcc.userdata.grpc.*;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class UserdataGrpcService extends UserdataServiceGrpc.UserdataServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(UserdataGrpcService.class);

    private final UserService userService;

    public UserdataGrpcService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void getCurrentUser(EmptyRequest request,
                               StreamObserver<UserResponseProto> observer) {
        try {
            String username = JwtServerInterceptor.USERNAME_KEY.get();
            log.debug("gRPC getCurrentUser username={}", username);
            UserResponse response = userService.createNewUserIfNotPresent(username);
            observer.onNext(toProto(response));
            observer.onCompleted();
        } catch (ResourceNotFoundException e) {
            log.warn("gRPC getCurrentUser: user not found yet: {}", e.getMessage());
            observer.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            failInternal(observer, "getCurrentUser", e);
        }
    }

    @Override
    public void updateUser(UpdateUserRequestProto request,
                           StreamObserver<UserResponseProto> observer) {
        try {
            String username = JwtServerInterceptor.USERNAME_KEY.get();
            log.debug("gRPC updateUser username={}", username);

            UpdateUserRequest updateRequest = new UpdateUserRequest(
                    request.getFirstname().isEmpty() ? null : request.getFirstname(),
                    request.getLastname().isEmpty() ? null : request.getLastname(),
                    request.getAvatar().isEmpty() ? null : request.getAvatar()
            );
            UserResponse response = userService.updateUser(username, updateRequest);
            observer.onNext(toProto(response));
            observer.onCompleted();
        } catch (ResourceNotFoundException e) {
            observer.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (IllegalArgumentException e) {
            observer.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            failInternal(observer, "updateUser", e);
        }
    }

    private UserResponseProto toProto(UserResponse response) {
        return UserResponseProto.newBuilder()
                .setId(response.id() == null ? "" : response.id())
                .setUsername(response.username() == null ? "" : response.username())
                .setFirstname(response.firstname() == null ? "" : response.firstname())
                .setLastname(response.lastname() == null ? "" : response.lastname())
                .setAvatar(response.avatar() == null ? "" : response.avatar())
                .build();
    }

    private void failInternal(StreamObserver<?> observer, String operation, Exception e) {
        log.error("Error in {}", operation, e);
        observer.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asRuntimeException());
    }
}