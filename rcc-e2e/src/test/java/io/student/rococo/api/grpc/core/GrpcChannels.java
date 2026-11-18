package io.student.rococo.api.grpc.core;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public enum GrpcChannels {
    INSTANCE;

    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();

    public ManagedChannel channel(String hostPort) {
        return channels.computeIfAbsent(hostPort, hp ->
                ManagedChannelBuilder.forTarget(hp)
                        .usePlaintext()
                        .maxInboundMessageSize(20 * 1024 * 1024) // как в сервисе
                        .build()
        );
    }

    public void shutdownAll() {
        channels.values().forEach(ch -> {
            try {
                ch.shutdownNow().awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        channels.clear();
    }
}