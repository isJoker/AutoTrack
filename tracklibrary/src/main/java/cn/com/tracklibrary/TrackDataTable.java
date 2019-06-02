package cn.com.tracklibrary;

/**
 * Created by JokerWan on 2019-06-02.
 * Function:
 */
public enum TrackDataTable {
    APP_STARTED("app_started"),
    APP_PAUSED_TIME("app_paused_time"),
    APP_END_STATE("app_end_state");

    TrackDataTable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private String name;
}
