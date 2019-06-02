package cn.com.tracklibrary;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by JokerWan on 2019-06-03.
 * Function:
 */
public class TrackDataContentProvider extends ContentProvider {

    private final static int APP_START = 1;
    private final static int APP_END_STATE = 2;
    private final static int APP_PAUSED_TIME = 3;

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor mEditor;
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private ContentResolver mContentResolver;

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            String packName = getContext().getPackageName();
            uriMatcher.addURI(packName + ".TrackDataContentProvider", TrackDataTable.APP_STARTED.getName(), APP_START);
            uriMatcher.addURI(packName + ".TrackDataContentProvider", TrackDataTable.APP_END_STATE.getName(), APP_END_STATE);
            uriMatcher.addURI(packName + ".TrackDataContentProvider", TrackDataTable.APP_PAUSED_TIME.getName(), APP_PAUSED_TIME);
            sharedPreferences = getContext().getSharedPreferences("cn.com.trackibrary.TrackDataSP", Context.MODE_PRIVATE);
            mEditor = sharedPreferences.edit();
            mEditor.apply();
            mContentResolver = getContext().getContentResolver();
        }
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        int match = uriMatcher.match(uri);
        MatrixCursor matrixCursor = null;
        switch (match) {
            case APP_START:
                int appStart = sharedPreferences.getBoolean(TrackDatabaseHelper.APP_STARTED, true) ? 1 : 0;
                matrixCursor = new MatrixCursor(new String[]{TrackDatabaseHelper.APP_STARTED});
                matrixCursor.addRow(new Object[]{appStart});
                break;
            case APP_END_STATE:
                int appEnd = sharedPreferences.getBoolean(TrackDatabaseHelper.APP_END_STATE, true) ? 1 : 0;
                matrixCursor = new MatrixCursor(new String[]{TrackDatabaseHelper.APP_END_STATE});
                matrixCursor.addRow(new Object[]{appEnd});
                break;
            case APP_PAUSED_TIME:
                long pausedTime = sharedPreferences.getLong(TrackDatabaseHelper.APP_PAUSED_TIME, 0);
                matrixCursor = new MatrixCursor(new String[]{TrackDatabaseHelper.APP_PAUSED_TIME});
                matrixCursor.addRow(new Object[]{pausedTime});
                break;
        }
        return matrixCursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        if(contentValues == null) {
            return uri;
        }
        int match = uriMatcher.match(uri);
        switch (match) {
            case APP_START:
                boolean appStart = contentValues.getAsBoolean(TrackDatabaseHelper.APP_STARTED);
                mEditor.putBoolean(TrackDatabaseHelper.APP_STARTED, appStart);
                mContentResolver.notifyChange(uri, null);
                break;
            case APP_END_STATE:
                boolean appEnd = contentValues.getAsBoolean(TrackDatabaseHelper.APP_END_STATE);
                mEditor.putBoolean(TrackDatabaseHelper.APP_END_STATE, appEnd);
                break;
            case APP_PAUSED_TIME:
                long pausedTime = contentValues.getAsLong(TrackDatabaseHelper.APP_PAUSED_TIME);
                mEditor.putLong(TrackDatabaseHelper.APP_PAUSED_TIME, pausedTime);
                break;
        }
        mEditor.commit();
        return uri;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
