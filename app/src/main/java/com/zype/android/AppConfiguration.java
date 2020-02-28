package com.zype.android;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Evgeny Cherkasov on 10.11.2018.
 */

public class AppConfiguration {
    @SerializedName("audioOnlyPlayback")
    public Boolean audioOnlyPlaybackEnabled;

    public Boolean hideFavoritesActionWhenSignedOut;

    public String marketplace;

    // Analytics
    public boolean segmentAnalytics() {
        return ZypeSettings.SEGMENT_ANALYTICS;
    }

    public String segmentAnalyticsWriteKey() {
        return ZypeSettings.SEGMENT_ANALYTICS_WRITE_KEY;
    }
}
