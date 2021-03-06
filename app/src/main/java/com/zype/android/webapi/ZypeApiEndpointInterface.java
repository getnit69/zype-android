package com.zype.android.webapi;

import com.zype.android.webapi.model.epg.Channel;
import com.zype.android.webapi.model.epg.ChannelResponse;
import com.zype.android.webapi.model.epg.Program;
import com.zype.android.webapi.model.epg.ProgramResponse;
import com.zype.android.webapi.model.app.App;
import com.zype.android.webapi.model.marketplaceconnect.MarketplaceConnect;
import com.zype.android.webapi.model.auth.RefreshAccessToken;
import com.zype.android.webapi.model.auth.RetrieveAccessToken;
import com.zype.android.webapi.model.auth.TokenInfo;
import com.zype.android.webapi.model.marketplaceconnect.MarketplaceConnectBody;
import com.zype.android.webapi.model.category.Categories;
import com.zype.android.webapi.model.consumers.Consumer;
import com.zype.android.webapi.model.consumers.ConsumerFavoriteVideo;
import com.zype.android.webapi.model.entitlements.VideoEntitlement;
import com.zype.android.webapi.model.entitlements.VideoEntitlements;
import com.zype.android.webapi.model.favorite.DeleteFavorite;
import com.zype.android.webapi.model.favorite.FavoriteVideo;
import com.zype.android.webapi.model.linking.DevicePin;
import com.zype.android.webapi.model.onair.OnAirAudioResponseData;
import com.zype.android.webapi.model.onair.OnAirData;
import com.zype.android.webapi.model.onair.OnAirVideoResponseData;
import com.zype.android.webapi.model.plan.Plan;
import com.zype.android.webapi.model.player.PlayerAudioData;
import com.zype.android.webapi.model.player.PlayerVideoData;
import com.zype.android.webapi.model.playlist.Playlist;
import com.zype.android.webapi.model.search.Search;
import com.zype.android.webapi.model.settings.ContentSettings;
import com.zype.android.webapi.model.settings.LiveStreamSettings;
import com.zype.android.webapi.model.settings.Settings;
import com.zype.android.webapi.model.video.Video;
import com.zype.android.webapi.model.video.VideoList;
import com.zype.android.webapi.model.zobjects.ZObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

/**
 * @author vasya
 * @version 1
 *          date 6/26/15
 */
public interface ZypeApiEndpointInterface {

    @FormUrlEncoded
    @POST("/oauth/token/")
    RefreshAccessToken.RefreshAccessTokenData authRefreshAccessToken(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/oauth/token/")
    RetrieveAccessToken.RetrieveAccessTokenData authRetrieveAccessToken(@FieldMap Map<String, String> params);

    @GET("/oauth/token/info/")
    TokenInfo getTokenInfo(@Query("access_token") String accessToken);

    // App

    @GET("/app/")
    App getApp(@QueryMap HashMap<String, String> getParams);

    // Videos

    @GET("/videos/{video_id}")
    Video getVideo(@Path("video_id") String videoId, @QueryMap HashMap<String, String> getParams);

    @GET("/videos/")
    VideoList getVideoList(@QueryMap Map<String, String> getParams);

    @GET("/videos/")
    Search getSearchVideo(@QueryMap HashMap<String, String> getParams);

    @GET("/categories/")
    Categories getCategory(@QueryMap Map<String, String> getParams);

    @GET("/zobjects/")
    ZObject getZobject(@QueryMap Map<String, String> getParams);

    @GET("/zobjects/?zobject_type=iphone_settings")
    Settings getSettings(@QueryMap Map<String, String> getParams);

    @GET("/zobjects/?zobject_type=limit_livestream")
    LiveStreamSettings getLiveStreamSettings(@QueryMap Map<String, String> getParams);

    @GET("/zobjects/?zobject_type=content")
    ContentSettings getContentSettings(@QueryMap Map<String, String> getParams);

    // Consumers

    @GET("/consumers/{consumer_id}")
    Consumer getConsumer(@Path("consumer_id") String consumerId, @QueryMap HashMap<String, String> getParams);

    @FormUrlEncoded
    @POST("/consumers/")
    Consumer createConsumer(@QueryMap HashMap<String, String> getParams, @FieldMap HashMap<String, String> postParams);

    @GET("/consumers/{consumer_id}/video_favorites/")
    ConsumerFavoriteVideo getFavoriteVideoList(@Path("consumer_id") String consumerId, @QueryMap Map<String, String> getParams);

    @FormUrlEncoded
    @POST("/consumers/{consumer_id}/video_favorites/")
    FavoriteVideo setFavoriteVideo(@Path("consumer_id") String consumerId, @FieldMap HashMap<String, String> postParams);

    @DELETE("/consumers/{consumer_id}/video_favorites/{favorite_id}/")
    DeleteFavorite setUnFavoriteVideo(@Path("consumer_id") String consumerId, @Path("favorite_id") String favoriteId, @QueryMap HashMap<String, String> postParams);

    @FormUrlEncoded
    @PUT("/consumers/forgot_password/")
    Consumer consumerForgotPassword(@QueryMap HashMap<String, String> getParams, @FieldMap HashMap<String, String> postParams);

    // Device linking

    @GET("/pin/status/")
    DevicePin getDevicePin(@QueryMap HashMap<String, String> queryParams);

    @POST("/pin/acquire/")
    DevicePin createDevicePin(@QueryMap HashMap<String, String> queryParams, @Body String emptyBody);

    // Marketplace Connect
    @POST("/v1/googleplay/")
    MarketplaceConnect verifySubscription(@Body MarketplaceConnectBody body);

    // Plans
    @GET("/plans/{plan_id}")
    Plan getPlan(@Path("plan_id") String planId, @QueryMap HashMap<String, String> getParams);

    // Video entitlements
    @GET("/videos/{video_id}/entitled/")
    VideoEntitlement checkVideoEntitlement(@Path("video_id") String videoId, @QueryMap HashMap<String, String> getParams);

    @GET("/consumer/videos/")
    VideoEntitlements getVideoEntitlements(@QueryMap HashMap<String, String> getParams);

    @GET("/embed/{video_id}")
    PlayerVideoData getVideoPlayer(@Path("video_id") String videoId, @QueryMap Map<String, String> getParams);

    @GET("/embed/{video_id}")
    PlayerAudioData getAudioPlayer(@Path("video_id") String videoId, @QueryMap Map<String, String> getParams);

    @GET("/embed/{video_id}")
    PlayerVideoData getDownloadVideo(@Path("video_id") String videoId, @QueryMap Map<String, String> getParams);

    @GET("/embed/{video_id}")
    PlayerAudioData getDownloadAudio(@Path("video_id") String videoId, @QueryMap Map<String, String> getParams);

    @GET("/embed/{video_id}")
    OnAirVideoResponseData getOnAirVideo(@Path("video_id") String videoId, @QueryMap Map<String, String> getParams);

    @GET("/embed/{video_id}")
    OnAirAudioResponseData getOnAirAudio(@Path("video_id") String videoId, @QueryMap Map<String, String> getParams);

    @GET("/videos/")
    OnAirData getOnAir(@QueryMap HashMap<String, String> getParams);

    @GET("/playlists/")
    Playlist getPlaylists(@QueryMap HashMap<String, String> getParams);

    @GET("/playlists/{playlist_id}/videos")
    VideoList getVideosFromPlaylist(@Path("playlist_id") String playlistId, @QueryMap Map<String, String> getParams);

    // EPG Channels
    @GET("/program_guides")
    ChannelResponse epgChannels(@QueryMap HashMap<String, String> params);

    // EPG events
    @GET("/program_guides/{id}/entries")
    ProgramResponse epgEvents(@Path("id") String id, @QueryMap HashMap<String, String> params);

}
