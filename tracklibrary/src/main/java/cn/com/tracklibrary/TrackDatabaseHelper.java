package cn.com.tracklibrary;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by JokerWan on 2019-06-02.
 * Function: ContentProvider + SharedPreferences 来实现前后台标记位跨进程数据共享
 */
public class TrackDatabaseHelper {

    private static final String TrackDataContentProvider = ".TrackDataContentProvider/";
    private ContentResolver mContentResolver;
    private Uri mAppStart;
    private Uri mAppEndState;
    private Uri mAppPausedTime;

    public static final String APP_STARTED = "app_started";
    public static final String APP_END_STATE = "app_end_state";
    public static final String APP_PAUSED_TIME = "app_paused_time";

    public TrackDatabaseHelper(Context context, String packageName) {
        mContentResolver = context.getContentResolver();
        mAppStart = Uri.parse("content://" + packageName + TrackDataContentProvider + TrackDataTable.APP_STARTED.getName());
        mAppEndState = Uri.parse("content://" + packageName + TrackDataContentProvider + TrackDataTable.APP_END_STATE.getName());
        mAppPausedTime = Uri.parse("content://" + packageName + TrackDataContentProvider + TrackDataTable.APP_PAUSED_TIME.getName());
    }

    /**
     * 保存app start 的状态
     *
     * @param appStart 是否是start
     */
    public void commitAppStart(boolean appStart) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(APP_STARTED, appStart);
        mContentResolver.insert(mAppStart, contentValues);
    }

    /**
     * 保存app paused 的时长
     *
     * @param pausedTime Activity paused 时长
     */
    public void commitAppPausedTime(long pausedTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(APP_PAUSED_TIME, pausedTime);
        mContentResolver.insert(mAppPausedTime, contentValues);
    }

    /**
     * 获取app paused 的时长
     *
     * @return Activity paused 时长
     */
    public long getAppPausedTime() {
        long pausedTime = 0;
        Cursor query = mContentResolver.query(mAppPausedTime, new String[]{APP_PAUSED_TIME}, null, null, null);
        if(query != null && query.getCount() > 0) {
            while (query.moveToNext()){
                pausedTime = query.getLong(0);
            }
        }
        if(query != null) {
            query.close();
        }
        return pausedTime;
    }

    /**
     * 保存app end 的状态
     *
     * @param appEndState Activity end 状态
     */
    public void commitAppEndEventState(boolean appEndState) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(APP_END_STATE, appEndState);
        mContentResolver.insert(mAppEndState, contentValues);
    }

    /**
     * 返回app end 的状态
     *
     * @return Activity End 状态
     */
    public boolean getAppEndEventState() {
        boolean state = true;
        Cursor cursor = mContentResolver.query(mAppEndState, new String[]{APP_END_STATE}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                state = cursor.getInt(0) > 0;
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return state;
    }

    public Uri getAppStartUri() {
        return mAppStart;
    }
}
