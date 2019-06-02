package cn.com.autotrack;

import android.app.Application;

import cn.com.tracklibrary.TrackDataApi;

/**
 * Created by JokerWan on 2019-06-02.
 * Function:
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TrackDataApi.init(this);
    }
}
