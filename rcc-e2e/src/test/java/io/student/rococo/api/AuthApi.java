package io.student.rococo.api;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface AuthApi {

    @GET("oauth2/authorize")
    Call<Void> authorize(
            @Query("response_type") String responseType,
            @Query("client_id") String clientId,
            @Query("scope") String scope,
            @Query("redirect_uri") String redirectUri,
            @Query("code_challenge") String codeChallenge,
            @Query("code_challenge_method") String codeChallengeMethod
    );

    @GET("login")
    Call<ResponseBody> getLoginPage();

    @POST("login")
    @FormUrlEncoded
    Call<Void> login(
            @Field("username") String username,
            @Field("password") String password,
            @Field("_csrf") String csrf
    );

    @GET
    Call<Void> followRedirect(@Url String url);

    @GET
    Call<ResponseBody> followRedirectWithBody(@Url String url);

    @POST("oauth2/token")
    @FormUrlEncoded
    Call<JsonNode> token(
            @Field("client_id") String clientId,
            @Field("redirect_uri") String redirectUri,
            @Field("grant_type") String grantType,
            @Field("code") String code,
            @Field("code_verifier") String codeVerifier
    );
}