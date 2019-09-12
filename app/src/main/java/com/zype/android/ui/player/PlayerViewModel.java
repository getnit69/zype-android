package com.zype.android.ui.player;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.otto.Subscribe;
import com.zype.android.Auth.AuthHelper;
import com.zype.android.DataRepository;
import com.zype.android.Db.DbHelper;
import com.zype.android.Db.Entity.AdSchedule;
import com.zype.android.Db.Entity.AnalyticBeacon;
import com.zype.android.Db.Entity.Video;
import com.zype.android.R;
import com.zype.android.ZypeApp;
import com.zype.android.ZypeConfiguration;
import com.zype.android.core.provider.helpers.PlaylistHelper;
import com.zype.android.utils.Logger;
import com.zype.android.webapi.WebApiManager;
import com.zype.android.webapi.builder.ParamsBuilder;
import com.zype.android.webapi.builder.PlayerParamsBuilder;
import com.zype.android.webapi.events.player.PlayerAudioEvent;
import com.zype.android.webapi.model.player.File;
import com.zype.android.zypeapi.IZypeApiListener;
import com.zype.android.zypeapi.ZypeApi;
import com.zype.android.zypeapi.ZypeApiResponse;
import com.zype.android.zypeapi.model.Advertising;
import com.zype.android.zypeapi.model.Analytics;
import com.zype.android.zypeapi.model.PlayerResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Evgeny Cherkasov on 23.07.2018
 */
public class PlayerViewModel extends AndroidViewModel implements CustomPlayer.InfoListener {

    private MutableLiveData<List<PlayerMode>> availablePlayerModes;
    private MutableLiveData<PlayerMode> playerMode;
    private MutableLiveData<String> playerUrl;
    private MutableLiveData<Integer> playbackState = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private String videoId;
    private String playlistId;

    private List<AdSchedule> adSchedule;
    private AnalyticBeacon analyticBeacon;
    private boolean onAir;

    private long playbackPosition = 0;
    private boolean isPlaybackPositionRestored;
    private boolean isUrlLoaded = false;
    private boolean inBackground = false;

    public enum PlayerMode {
        AUDIO,
        VIDEO
    }

    DataRepository repo;
    ZypeApi api;
    WebApiManager oldApi;

    public PlayerViewModel(Application application) {
        super(application);
        repo = DataRepository.getInstance(application);
        api = ZypeApi.getInstance();
        oldApi = WebApiManager.getInstance();
        oldApi.subscribe(this);

        availablePlayerModes = new MutableLiveData<>();
        availablePlayerModes.setValue(new ArrayList<PlayerMode>());
        playerMode = new MutableLiveData<>();
    }

    @Override
    protected void onCleared() {
        oldApi.unsubscribe(this);
        super.onCleared();
    }

    public void init(String videoId, String playlistId, PlayerMode mediaType) {
        this.videoId = videoId;
        this.playlistId = playlistId;

        Video video = repo.getVideoSync(videoId);
        playbackPosition = video.playTime;
        isPlaybackPositionRestored = false;

        updateAvailablePlayerModes();
        if (mediaType != null || isMediaTypeAvailable(mediaType)) {
            playerMode.setValue(mediaType);
        }
        else {
            List<PlayerMode> mediaTypes = availablePlayerModes.getValue();
            if (mediaTypes != null && !mediaTypes.isEmpty()) {
                playerMode.setValue(mediaTypes.get(0));
            }
            else {
                playerMode.setValue(null);
            }
        }
    }

    // Ad schedule

    private void updateAdSchedule() {
        adSchedule = repo.getAdScheduleSync(videoId);
    }

    public List<AdSchedule> getAdSchedule() {
        return adSchedule;
    }

    // Analytics beacon

    private void updateAnalyticsBeacon() {
        analyticBeacon = repo.getAnalyticsBeaconSync(videoId);
    }

    public AnalyticBeacon getAnalyticBeacon() {
        return analyticBeacon;
    }

    // Playback position

    public long getPlaybackPosition() {
        return playbackPosition;
    }

    public void savePlaybackPosition(long position) {
        this.playbackPosition = position;

        if(!TextUtils.isEmpty(videoId)) {
            Video video = repo.getVideoSync(videoId);

            if(video != null) {
                video.playTime = position;
                repo.updateVideo(video);
            }
        }

        isPlaybackPositionRestored = false;
    }

    public boolean playbackPositionRestored() {
        return isPlaybackPositionRestored;
    }

    public void onPlaybackPositionRestored() {
        isPlaybackPositionRestored = true;
    }

    public void onPlaybackStarted() {
        if(!TextUtils.isEmpty(videoId)) {
            Video video = repo.getVideoSync(videoId);

            if (video != null) {
                video.isPlayStarted = 1;
                repo.updateVideo(video);
            }
        }
    }

    public void onPlaybackFinished() {
        if(!TextUtils.isEmpty(videoId)) {
            Video video = repo.getVideoSync(videoId);

            if (video != null) {
                video.isPlayStarted = 1;
                video.isPlayFinished = 1;
                repo.updateVideo(video);
            }
        }
    }

    public boolean isThereNextVideo() {
        return !(PlaylistHelper.getNextVideoId(videoId, repo.getPlaylistVideosSync(playlistId)) == null);
    }

    public boolean isTherePreviousVideo() {
        return !(PlaylistHelper.getPreviousVideoId(videoId, repo.getPlaylistVideosSync(playlistId)) == null);
    }

    // Playback state

    public MutableLiveData<Integer> getPlaybackState() {
        return playbackState;
    }

    public void setPlaybackState(int state) {
        playbackState.setValue(state);
    }

    // On air

    public void setOnAir(boolean onAir) {
        this.onAir = onAir;
    }

    public boolean isOnAir() {
        return this.onAir;
    }

    // Player url

    public MutableLiveData<String> getPlayerUrl() {
        if (playerUrl == null) {
            playerUrl = new MutableLiveData<>();
        }
        Video video = repo.getVideoSync(videoId);
        video.playerAudioUrl = null;
        video.playerVideoUrl = null;
        updatePlayerUrl(video);
        loadPlayer();
        return playerUrl;
    }

    private void updatePlayerUrl(Video video) {
        String newPlayerUrl = null;
        if (playerMode.getValue() != null) {
            switch (playerMode.getValue()) {
                case AUDIO:
                    if (video.isDownloadedAudio == 1) {
                        newPlayerUrl = video.downloadAudioPath;
                    }
                    else {
                        newPlayerUrl = video.playerAudioUrl;
                    }
                    break;
                case VIDEO:
                    if (video.isDownloadedVideo == 1) {
                        newPlayerUrl = video.downloadVideoPath;
                    }
                    else {
                        newPlayerUrl = video.playerVideoUrl;
                    }
                    break;
            }
        }
        updateAdSchedule();
        updateAnalyticsBeacon();
        if (playerUrl.getValue() == null) {
            if (newPlayerUrl != null) {
                playerUrl.setValue(newPlayerUrl);
            }
        }
        else {
            if (!playerUrl.getValue().equals(newPlayerUrl)) {
                playerUrl.setValue(newPlayerUrl);
            }
        }
    }

    // Player mode

    public MutableLiveData<PlayerMode> getPlayerMode() {
        return playerMode;
    }

    public void setPlayerMode(PlayerMode mode) {
        Logger.d("setPlayerMode(): mode=" + mode.name());
        Video video = repo.getVideoSync(videoId);
        if (video != null) {
            if (playerMode.getValue() != mode) {
                playerMode.setValue(mode);
                updatePlayerUrl(video);
            }
        }
    }

    public MutableLiveData<List<PlayerMode>> getAvailablePlayerModes() {
        return availablePlayerModes;
    }

    private void updateAvailablePlayerModes() {
        List<PlayerMode> result = new ArrayList<>();

        Video video = repo.getVideoSync(videoId);
        if (video != null) {
            if (ZypeApp.get(getApplication()).getAppConfiguration().audioOnlyPlaybackEnabled) {
                if (!TextUtils.isEmpty(video.playerAudioUrl)
                        || video.isDownloadedAudio == 1) {
                    result.add(PlayerMode.AUDIO);
                }
            }
            if (!TextUtils.isEmpty(video.playerVideoUrl)
                    || video.isDownloadedVideo == 1) {
                result.add(PlayerMode.VIDEO);
            }
        }

        availablePlayerModes.setValue(result);
    }

    public boolean isMediaTypeAvailable(PlayerMode mediaType) {
        return availablePlayerModes.getValue() != null
                && availablePlayerModes.getValue().contains(mediaType);
    }

    public void setMediaTypeAvailable(PlayerMode mediaType, boolean available) {
        Logger.d("setMediaTypeAvailable(): mediaType=" + mediaType + ", available=" + available);
        if (isMediaTypeAvailable(mediaType)) {
            if (!available) {
                availablePlayerModes.getValue().remove(mediaType);
                availablePlayerModes.setValue(availablePlayerModes.getValue());
            }
        }
        else {
            if (available) {
                availablePlayerModes.getValue().add(mediaType);
                availablePlayerModes.setValue(availablePlayerModes.getValue());
            }
        }
    }

    // Downloads

    public boolean isAudioDownloaded() {
        Video video = repo.getVideoSync(videoId);
        if (video != null) {
            return video.isDownloadedAudio == 1;
        }
        else
            return false;
    }

    public boolean isVideoDownloaded() {
        Video video = repo.getVideoSync(videoId);
        if (video != null) {
            return video.isDownloadedVideo == 1;
        }
        else
            return false;
    }

    // Background

    public boolean isBackgroundPlaybackEnabled() {
        if (playerMode.getValue() != null) {
            switch (playerMode.getValue()) {
                case AUDIO:
                    return ZypeConfiguration.isBackgroundAudioPlaybackEnabled(getApplication());
                case VIDEO:
                    return ZypeConfiguration.isBackgroundPlaybackEnabled(getApplication());
                default:
                    return false;
            }
        }
        else {
            return false;
        }
    }

    public void setToBackground(boolean value) {
        inBackground = value;
    }

    public boolean isInBackground() {
        return inBackground;
    }

    // Error

    public LiveData<String> onPlayerError() {
        if (errorMessage == null) {
            errorMessage = new MutableLiveData<>();
        }
        return errorMessage;
    }

    //

    public void loadPlayer() {
        Logger.d("loadPlayer(): videoId=" + videoId);
        String uuid = null;
        if (AuthHelper.isLoggedIn()) {
            AuthHelper.onLoggedIn(isLoggedIn ->
                    loadVideoPlayer(AuthHelper.getAccessToken(), uuid));
        }
        else {
            loadVideoPlayer(null, uuid);
        }
    }

    IZypeApiListener createVideoPlayerListener(String accessToken, String uuid) {
        return response -> {
            PlayerResponse playerResponse = (PlayerResponse) response.data;
            if (response.isSuccessful) {
                List<com.zype.android.zypeapi.model.File> files = playerResponse.playerData.body.files;
                if (files == null || files.isEmpty()) {
                    Logger.w("loadVideoPlayer()::onCompleted(): Video source not found");
                    return;
                }
                // Take first source in the list
                String url = files.get(0).url;
                Logger.d("loadVideoPlayer()::onCompleted(): url=" + url);

                // Ad tags
                repo.deleteAdSchedule(videoId);
                Advertising advertising = playerResponse.playerData.body.advertising;
                if (advertising != null) {
                    List<AdSchedule> schedule = DbHelper.adScheduleApiToEntity(advertising.schedule, videoId);
                    repo.insertAdSchedule(schedule);
                }
                updateAdSchedule();

                // Analytics
                repo.deleteAnalyticsBeacon(videoId);
                Analytics analytics = playerResponse.playerData.body.analytics;
                if (analytics != null &&
                        analytics.beacon != null && analytics.dimensions != null) {
                    repo.insertAnalyticsBeacon(DbHelper.analyticsApiToEntity(analytics));
                }
                updateAnalyticsBeacon();

                // Save video url in the local database if it was changed
                Video video = repo.getVideoSync(videoId);
                if (video.playerVideoUrl == null || !video.playerVideoUrl.equals(url)) {
                    video.playerVideoUrl = url;
                    repo.updateVideo(video);
                }
                updateAvailablePlayerModes();
                if (playerMode.getValue() == null) {
                    setPlayerMode(PlayerMode.VIDEO);
                }
                else if (playerMode.getValue() == PlayerMode.VIDEO) {
                    updatePlayerUrl(video);
                }

                // Load audio
                if (ZypeApp.get(getApplication()).getAppConfiguration().audioOnlyPlaybackEnabled) {
                    loadAudioPlayer(accessToken, uuid);
                }
            }
            else {
                if (playerResponse != null) {
//                        errorMessage.setValue(playerResponse.message);
                }
                else {
                    if (WebApiManager.isHaveActiveNetworkConnection(getApplication())) {
                        errorMessage.setValue(getApplication().getString(R.string.video_error_bad_request));
                    }
                    else {
                        errorMessage.setValue(getApplication().getString(R.string.error_internet_connection));
                    }
                }
            }
        };
    }

    private void loadVideoPlayer(String accessToken, String uuid) {
        api.getPlayer(videoId, false, accessToken, uuid,
                createVideoPlayerListener(accessToken, uuid));
    }

    IZypeApiListener createAudioPlayerListener(String accessToken, String uuid) {
        return response -> {
            PlayerResponse playerResponse = (PlayerResponse) response.data;
            if (response.isSuccessful) {
                List<com.zype.android.zypeapi.model.File> files = playerResponse.playerData.body.files;
                if (files == null || files.isEmpty()) {
                    Logger.w("loadAudioPlayer()::onCompleted(): Audio source not found");
                    return;
                }
                // Take first source in the list
                String url = files.get(0).url;
                Logger.d("loadAudioPlayer()::onCompleted(): url=" + url);

                // Save audio url in the local database if it was changed
                Video video = repo.getVideoSync(videoId);
                if (video.playerAudioUrl == null || !video.playerAudioUrl.equals(url)) {
                    video.playerAudioUrl = url;
                    repo.updateVideo(video);
                }
                updateAvailablePlayerModes();
                if (playerMode.getValue() == null) {
                    setPlayerMode(PlayerMode.AUDIO);
                }
                else if (playerMode.getValue() == PlayerMode.AUDIO) {
                    updatePlayerUrl(video);
                }
            }
            else {
            }
        };
    }

    private void loadAudioPlayer(String accessToken, String uuid) {
        api.getPlayer(videoId, true, accessToken, uuid,
                createAudioPlayerListener(accessToken, uuid));
    }

    // 'CustomPlayer.InfoListener' implementation

    @Override
    public void onVideoFormatEnabled(Format format, int trigger, long mediaTimeMs) {
        Logger.i("onVideoFormatEnabled()");
        if (getPlayerMode().getValue() == PlayerMode.VIDEO
                && format.codecs != null && format.codecs.equals("mp4a.40.2")) {
            Video video = repo.getVideoSync(videoId);
            video.playerVideoUrl = null;
            repo.updateVideo(video);

            updateAvailablePlayerModes();
            setPlayerMode(PlayerMode.AUDIO);
        }
    }

    @Override
    public void onAudioFormatEnabled(Format format, int trigger, long mediaTimeMs) {
        Logger.i("onAudioFormatEnabled()");
    }

    @Override
    public void onDroppedFrames(int count, long elapsed) {
    }

    @Override
    public void onBandwidthSample(int elapsedMs, long bytes, long bitrateEstimate) {
    }

    @Override
    public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs) {
    }

    @Override
    public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {
    }

    @Override
    public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {
    }

    @Override
    public void onAvailableRangeChanged(TimeRange availableRange) {
    }

    // ExoPlayer 2 related staff
    // TODO: Move this to a helper class

    public MediaSource getMediaSource(Context context, String contentUri) {
        MediaSource result = null;
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, WebApiManager.CUSTOM_HEADER_VALUE));
        if (contentUri.contains("http:") || contentUri.contains("https:")) {
            if (contentUri.contains(".mp4")
                    || contentUri.contains(".m4a")
                    || contentUri.contains(".mp3")) {
                result = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(contentUri));
            }
            else {
                result = new HlsMediaSource.Factory(dataSourceFactory)
                        .setAllowChunklessPreparation(true)
                        .createMediaSource(Uri.parse(contentUri));
            }
        }
        else {
            result = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(contentUri));
        }
        return result;
    }

}
