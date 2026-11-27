package io.student.rococo.api.grpc.core;

import io.student.rococo.api.grpc.*;
import io.student.rococo.config.Config;

public final class GrpcStubs {
    private static final Config CFG = Config.getInstance();

    private GrpcStubs() {
    }

    public static MuseumGrpcApi museum() {
        return new MuseumGrpcApi(GrpcChannels.INSTANCE.channel(CFG.rococoGrpcMuseumUrl()));
    }

    public static GeoGrpcApi geo() {
        return new GeoGrpcApi(GrpcChannels.INSTANCE.channel(CFG.rococoGrpcGeoUrl()));
    }

    public static ArtistGrpcApi artist() {
        return new ArtistGrpcApi(GrpcChannels.INSTANCE.channel(CFG.rococoGrpcArtistUrl()));
    }

    public static PaintingGrpcApi painting() {
        return new PaintingGrpcApi(GrpcChannels.INSTANCE.channel(CFG.rococoGrpcPaintingUrl()));
    }

    public static UserdataGrpcApi userdata() {
        return new UserdataGrpcApi(GrpcChannels.INSTANCE.channel(CFG.rococoGrpcUserdataUrl()));
    }
}