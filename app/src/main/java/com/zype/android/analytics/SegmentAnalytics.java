package com.zype.android.analytics;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.zype.android.AppConfiguration;
import com.zype.android.ZypeApp;

import java.util.HashMap;
import java.util.Map;

import static com.zype.android.analytics.AnalyticsEvents.EVENT_PLAYBACK;
import static com.zype.android.analytics.AnalyticsEvents.EVENT_PLAYBACK_FINISHED;
import static com.zype.android.analytics.AnalyticsEvents.EVENT_PLAYBACK_STARTED;

public class SegmentAnalytics {
    private static final String TAG = SegmentAnalytics.class.getSimpleName();

    public static void init() {
        AppConfiguration appConfig = ZypeApp.getInstance().getAppConfiguration();
        // Create an analytics client with the given context and Segment write key.
        Analytics analytics = new Analytics.Builder(ZypeApp.getInstance().getApplicationContext(),
                appConfig.segmentAnalyticsWriteKey())
                .trackApplicationLifecycleEvents() // Enable this to record certain application events automatically!
//                .recordScreenViews() // Enable this to record screen views automatically!
                .build();

        // Set the initialized instance as a globally accessible instance.
        Analytics.setSingletonInstance(analytics);
    }

    public static void trackEvent(String event, HashMap<String, Object> attributes) {
        Properties properties = attributesToProperties(attributes);
        Context context = ZypeApp.getInstance().getApplicationContext();
        switch (event) {
            case EVENT_PLAYBACK_STARTED:
                Analytics.with(context).track("Video Content Started", properties);
                break;
            case EVENT_PLAYBACK:
                Analytics.with(context).track("Video Content Playing", properties);
                break;
            case EVENT_PLAYBACK_FINISHED:
                Analytics.with(context).track("Video Content Completed", properties);
                break;
        }
    }

    private static Properties attributesToProperties(Map<String, Object> attributes) {
        Properties properties = new Properties();

        String contentCmsCategory = null;
        properties.putValue("contentCmsCategory", contentCmsCategory);

        String adType = null;
        properties.putValue("Ad Type", adType);

        String contentShownOnPlatform = "ott";
        properties.putValue("contentShownOnPlatform", contentShownOnPlatform);

//        String streamingDevice = (String) attributes.get(AnalyticsTags.ATTRIBUTE_CONTENT_ANALYTICS_DEVICE);
        String streamingDevice = Build.MANUFACTURER + " " + Build.MODEL;
        properties.putValue("streaming_device", streamingDevice);

        String videoAccountId = "416418724";
        properties.putValue("videoAccountId", videoAccountId);

        String videoAccountName = "People";
        properties.putValue("videoAccountName", videoAccountName);

        String videoAdDuration = null;
        properties.putValue("videoAdDuration", videoAdDuration);

        String videoAdVolume = null;
        properties.putValue("videoAdVolume", videoAdVolume);

        // total_length
        long duration = (Long) attributes.get(AnalyticsTags.VIDEO_DURATION);
        properties.putValue("videoContentDuration", duration);

        // position
        long position = (Long) attributes.get(AnalyticsTags.VIDEO_CURRENT_POSITION) / 1000;
        properties.putValue("videoContentPosition", position);

        long percent = (duration != 0) ? position * 100 / duration : 0;
        properties.putValue("videoContentPercentComplete", percent);

        String videoCreatedAt = (String) attributes.get(AnalyticsTags.VIDEO_CREATED_AT);
        properties.putValue("videoCreatedAt", videoCreatedAt);

        String videoFranchise  = null;
        properties.putValue("videoFranchise ", videoFranchise );

        // asset_id
        String videoId = (String) attributes.get(AnalyticsTags.VIDEO_ID);
        properties.putValue("videoId", videoId);

        // title
        String title = (String) attributes.get(AnalyticsTags.VIDEO_TITLE);
        properties.putValue("videoName", title);

        // airdate
        String airdate = (String) attributes.get(AnalyticsTags.VIDEO_AIRDATE);
        if (TextUtils.isEmpty(airdate)) {
            airdate = null;
        }
        properties.putValue("videoPublishedAt", airdate);

        String videoSyndicate = null;
        properties.putValue("videoSyndicate", videoSyndicate);

        String videoTags = null;
        properties.putValue("videoTags", videoTags);

        String videoThumbnail = (String) attributes.get(AnalyticsTags.VIDEO_THUMBNAIL);
        properties.putValue("videoThumbnail", videoThumbnail);

        String videoUpdatedAt = (String) attributes.get(AnalyticsTags.VIDEO_UPDATED_AT);
        properties.putValue("videoUpdatedAt", videoUpdatedAt);

        Log.d(TAG, "attributesToProperties(): " + properties.toString());
        return properties;
    }
}
