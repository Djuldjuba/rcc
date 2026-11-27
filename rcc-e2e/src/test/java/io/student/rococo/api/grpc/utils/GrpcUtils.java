package io.student.rococo.api.grpc.utils;

public final class GrpcUtils {
    private GrpcUtils() {
    }

    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}