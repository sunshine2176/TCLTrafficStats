package com.tcl.tcltrafficstats;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Map;

import utils.TrafficInfo;

public class MainActivity extends AppCompatActivity {

    private TrafficService trafficService;
    private String trafficPath;
    private String pkg;
    private String timeStr;
    public static final int EXTERNAL_STORAGE_REQ_CODE = 10 ;
    private String fileDir;
    private File confFile;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            trafficService = ((TrafficService.MyBinder)service).getService();
            Log.i("tag", "service not null");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    trafficService.getTrafficInfo(trafficPath,Long.valueOf(timeStr),pkg);
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            trafficService = null;
            Log.i("tag","service is null");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File root = Environment.getExternalStorageDirectory();
        fileDir = root.getAbsolutePath() + File.separator + "trafficStats";
        String confPath = fileDir + File.separator + "conf.txt";
        confFile = new File(confPath);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},EXTERNAL_STORAGE_REQ_CODE);
            }else {
                getWritePermission(confFile);
            }
        }else getWritePermission(confFile);



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case EXTERNAL_STORAGE_REQ_CODE:{
                if (grantResults.length > 0){
                    getWritePermission(confFile);
                }else {
                    Toast.makeText(this, "please give me the permission", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void getWritePermission(File confFile){
        if (confFile.exists()){
            TrafficInfo trafficInfo = new TrafficInfo();
            Map<String,String> confMap = trafficInfo.readFileToMap(confFile);
            pkg = confMap.get("packageName");
            timeStr = confMap.get("time");

            trafficPath = fileDir + File.separator + "trafficInfo.txt";
            File trafficFile = new File(trafficPath);
            if (trafficFile.exists()){
                trafficFile.delete();
            }
            Intent intent = new Intent(MainActivity.this,TrafficService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        }
    }

    @Override
    protected void onDestroy() {
//        unbindService(mConnection);
        super.onDestroy();
    }

}
