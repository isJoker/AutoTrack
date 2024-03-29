package cn.com.tracklibrary;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by JokerWan on 2019-06-02.
 * Function:
 */
class TrackDataPrivate {

    private static List<String> mIgnoredActivities;
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"
            + ".SSS", Locale.CHINA);
    private static TrackDatabaseHelper mDatabaseHelper;
    private static CountDownTimer mCountDownTimer;
    private static WeakReference<Activity> mCurrentActivity;
    /**
     * session间隔时间为30s，即30s之内没有新的页面打开，则认为app处于后台（触发app end事件），
     * 当一个页面显示出来了，与上一个页面的退出时间间隔超过了30s，就认为app重新处于前台了（触发app start）
     */
    private final static int SESSION_INTERVAL_TIME = 30 * 1000;

    static {
        mIgnoredActivities = new ArrayList<>();
    }

    /**
     * merge 源JSONObject 到 目标JSONObject
     *
     * @param source
     * @param dest
     * @throws JSONException
     */
    static void mergeJSONObject(JSONObject source, JSONObject dest) throws JSONException {
        Iterator<String> keys = source.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = source.get(key);
            if (value instanceof Date) {
                synchronized (mDateFormat) {
                    dest.put(key, mDateFormat.format(value));
                }
            } else {
                dest.put(key, value);
            }
        }
    }

    static String formatJson(String jsonStr) {
        try {
            if (null == jsonStr || "".equals(jsonStr)) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(" \n");
            char last;
            char current = '\0';
            int indent = 0;
            boolean isInQuotationMarks = false;
            for (int i = 0; i < jsonStr.length(); i++) {
                last = current;
                current = jsonStr.charAt(i);
                switch (current) {
                    case '"':
                        if (last != '\\') {
                            isInQuotationMarks = !isInQuotationMarks;
                        }
                        sb.append(current);
                        break;
                    case '{':
                    case '[':
                        sb.append(current);
                        if (!isInQuotationMarks) {
                            sb.append('\n');
                            indent++;
                            addIndentBlank(sb, indent);
                        }
                        break;
                    case '}':
                    case ']':
                        if (!isInQuotationMarks) {
                            sb.append('\n');
                            indent--;
                            addIndentBlank(sb, indent);
                        }
                        sb.append(current);
                        break;
                    case ',':
                        sb.append(current);
                        if (last != '\\' && !isInQuotationMarks) {
                            sb.append('\n');
                            addIndentBlank(sb, indent);
                        }
                        break;
                    default:
                        sb.append(current);
                }
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void addIndentBlank(StringBuilder sb, int indent) {
        try {
            for (int i = 0; i < indent; i++) {
                sb.append('\t');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 忽略采集某个activity的页面浏览事件
     *
     * @param activity 忽略的activity
     */
    static void ignoreAutoTrackActivity(Class<?> activity) {
        if (activity == null) {
            return;
        }
        mIgnoredActivities.add(activity.getCanonicalName());
    }

    /**
     * 恢复采集某个activity的页面浏览事件
     *
     * @param activity 恢复的activity
     */
    static void removeIgnoreActivity(Class<?> activity) {
        if (activity == null) {
            return;
        }
        String canonicalName = activity.getCanonicalName();
        if (mIgnoredActivities.contains(canonicalName)) {
            mIgnoredActivities.remove(canonicalName);
        }
    }

    /**
     * 获取 Android ID
     *
     * @param mContext Context
     * @return String
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context mContext) {
        String androidID = "";
        try {
            androidID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return androidID;
    }

    public static Map<String, Object> getDeviceInfo(Context context) {
        final Map<String, Object> deviceInfo = new HashMap<>();
        {
            deviceInfo.put("lib", "Android");
            deviceInfo.put("lib_version", TrackDataManager.SDK_VERSION);
            deviceInfo.put("os", "Android");
            deviceInfo.put("os_version",
                    Build.VERSION.RELEASE == null ? "UNKNOWN" : Build.VERSION.RELEASE);
            deviceInfo
                    .put("manufacturer", Build.MANUFACTURER == null ? "UNKNOWN" : Build.MANUFACTURER);
            if (TextUtils.isEmpty(Build.MODEL)) {
                deviceInfo.put("model", "UNKNOWN");
            } else {
                deviceInfo.put("model", Build.MODEL.trim());
            }

            try {
                final PackageManager manager = context.getPackageManager();
                final PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
                deviceInfo.put("app_version", packageInfo.versionName);

                int labelRes = packageInfo.applicationInfo.labelRes;
                deviceInfo.put("app_name", context.getResources().getString(labelRes));
            } catch (final Exception e) {
                e.printStackTrace();
            }

            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            deviceInfo.put("screen_height", displayMetrics.heightPixels);
            deviceInfo.put("screen_width", displayMetrics.widthPixels);

            // 返回只读的map
            return Collections.unmodifiableMap(deviceInfo);
        }
    }

    /**
     * 注册全局Activity生命周期回调
     *
     * @param application
     */
    public static void registerActivityLifecycleCallbacks(Application application) {

        mDatabaseHelper = new TrackDatabaseHelper(application,application.getPackageName());
        mCountDownTimer = new CountDownTimer(SESSION_INTERVAL_TIME,10 * 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                trackAppEnd(mCurrentActivity.get());
            }
        };

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mDatabaseHelper.commitAppStart(true);
                long timeDiff = System.currentTimeMillis() - mDatabaseHelper.getAppPausedTime();
                if(timeDiff > SESSION_INTERVAL_TIME) {
                    // 若APP被杀死或者异常退出，导致没有收到app end时间，则重新发送app end事件
                    if(!mDatabaseHelper.getAppEndEventState()) {
                        trackAppEnd(activity);
                    }
                }

                if(mDatabaseHelper.getAppEndEventState()) {
                    mDatabaseHelper.commitAppEndEventState(false);
                    trackAppStart(activity);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                trackAppViewScreen(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                mCurrentActivity = new WeakReference<>(activity);
                mCountDownTimer.start();
                mDatabaseHelper.commitAppPausedTime(System.currentTimeMillis());
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    /**
     * track 页面浏览事件
     *
     * @param activity 浏览的activity
     */
    private static void trackAppViewScreen(Activity activity) {
        if (activity == null) {
            return;
        }
        String canonicalName = activity.getClass().getCanonicalName();
        if (mIgnoredActivities.contains(canonicalName)) {
            return;
        }

        try {
            JSONObject properties = new JSONObject();
            properties.put("activity", canonicalName);
            properties.put("title", getActivityTitle(activity));
            TrackDataManager.getInstance().track("AppViewScreen", properties);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /**
     * Track AppStart 事件
     */
    private static void trackAppStart(Activity activity) {
        try {
            if (activity == null) {
                return;
            }
            JSONObject properties = new JSONObject();
            properties.put("activity", activity.getClass().getCanonicalName());
            properties.put("title", getActivityTitle(activity));
            TrackDataManager.getInstance().track("AppStart", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Track AppEnd 事件
     */
    private static void trackAppEnd(Activity activity) {
        try {
            if (activity == null) {
                return;
            }
            JSONObject properties = new JSONObject();
            properties.put("activity", activity.getClass().getCanonicalName());
            properties.put("title", getActivityTitle(activity));
            TrackDataManager.getInstance().track("AppEnd", properties);
            mDatabaseHelper.commitAppEndEventState(true);
            mCurrentActivity = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定activity title
     *
     * @param activity 指定的activity
     * @return
     */
    private static String getActivityTitle(Activity activity) {
        String activityTitle = null;

        if (activity == null) {
            return null;
        }

        try {
            activityTitle = activity.getTitle().toString();

            if (Build.VERSION.SDK_INT >= 11) {
                String toolbarTitle = getToolbarTitle(activity);
                if (!TextUtils.isEmpty(toolbarTitle)) {
                    activityTitle = toolbarTitle;
                }
            }

            if (TextUtils.isEmpty(activityTitle)) {
                PackageManager packageManager = activity.getPackageManager();
                if (packageManager != null) {
                    ActivityInfo activityInfo = packageManager.getActivityInfo(activity.getComponentName(), 0);
                    if (activityInfo != null) {
                        activityTitle = activityInfo.loadLabel(packageManager).toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activityTitle;
    }

    @TargetApi(11)
    private static String getToolbarTitle(Activity activity) {
        try {
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                if (!TextUtils.isEmpty(actionBar.getTitle())) {
                    return actionBar.getTitle().toString();
                }
            } else {
                if (activity instanceof AppCompatActivity) {
                    AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
                    android.support.v7.app.ActionBar supportActionBar = appCompatActivity.getSupportActionBar();
                    if (supportActionBar != null) {
                        if (!TextUtils.isEmpty(supportActionBar.getTitle())) {
                            return supportActionBar.getTitle().toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static String formatMsToData(long ms){
        return mDateFormat.format(ms);
    }

    /**
     * 注册 AppStart 的监听
     */
    static void registerActivityStateObserver(Application application) {
        final Uri appStartUri = mDatabaseHelper.getAppStartUri();
        application.getContentResolver().registerContentObserver(appStartUri
                , false, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if(appStartUri.equals(uri)) {
                    mCountDownTimer.cancel();
                }
            }
        });
    }
}
