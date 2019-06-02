package cn.com.autotrack;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import cn.com.tracklibrary.TrackDataManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int PERMISSIONS_REQUEST_READ_CONTACTS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_click).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(this,SecondActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            //拥有权限
        } else {
            //没有权限，需要申请全新啊
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // onRequestPermissionsResult 会导致onResume重新执行，会把当前页面多统计一次，所以这里暂时将MainActivity加入忽略名单
        TrackDataManager.getInstance().ignoreAutoTrackActivity(MainActivity.class);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 用户点击允许
                } else {
                    // 用户点击禁止
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 将MainActivity移除忽略名单
        TrackDataManager.getInstance().removeIgnoredActivity(MainActivity.class);
    }
}
