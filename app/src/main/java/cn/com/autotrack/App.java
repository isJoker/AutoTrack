package cn.com.autotrack;

import android.app.Application;

import cn.com.tracklibrary.TrackDataManager;

/**
 * Created by JokerWan on 2019-06-02.
 * Function:
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TrackDataManager.init(this);
    }
}
