package cn.com.tracklibrary;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by JokerWan on 2019-06-01.
 * Function: track 数据操作类
 */
public class TrackDataManager {
    static final String SDK_VERSION = "1.0.0";
    private String mDeviceId;
    private static Map<String, Object> mDeviceInfo;
    private static final Object mLock = new Object();
    private static volatile TrackDataManager INSTANCE;

    private static final String TAG = TrackDataManager.class.getSimpleName();

    public static void init(Application application) {
        if(INSTANCE == null) {
            synchronized (mLock){
                if(INSTANCE == null) {
                    INSTANCE = new TrackDataManager(application);
                }
            }
        }
    }

    public static TrackDataManager getInstance() {
        return INSTANCE;
    }

    private TrackDataManager(Application application) {
        mDeviceId = TrackDataPrivate.getAndroidID(application);
        mDeviceInfo = TrackDataPrivate.getDeviceInfo(application);
        TrackDataPrivate.registerActivityLifecycleCallbacks(application);
        TrackDataPrivate.registerActivityStateObserver(application);
    }

    /**
     * track 事件
     *
     * @param eventName  事件名称
     * @param properties 事件属性
     */
    public void track(@NonNull String eventName, @NonNull JSONObject properties) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("event",eventName);
            jsonObject.put("time",TrackDataPrivate.formatMsToData(System.currentTimeMillis()));
            jsonObject.put("device_id",mDeviceId);
            JSONObject sendProperties = new JSONObject(mDeviceInfo);
            TrackDataPrivate.mergeJSONObject(properties,sendProperties);
            jsonObject.put("properties",sendProperties);

            Log.i(TAG, TrackDataPrivate.formatJson(jsonObject.toString()));

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void ignoreAutoTrackActivity(Class<?> activityClass) {
        TrackDataPrivate.ignoreAutoTrackActivity(activityClass);
    }

    public void removeIgnoredActivity(Class<?> activityClass) {
        TrackDataPrivate.removeIgnoreActivity(activityClass);
    }
}
