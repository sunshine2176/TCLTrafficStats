package com.tcl.tcltrafficstats;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Build;
import android.os.Debug;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import utils.ProcessManager;
import utils.TrafficInfo;
import utils.models.AndroidAppProcess;

public class TrafficService extends Service {

    private MyBinder binder = new MyBinder();
    Timer mTimer;
    private Intent serviceIntent;
    private TrafficInfo trafficInfo = new TrafficInfo();
    private File file;
    private String packageName;
    private String trafficPath;
    private String time;
    private ActivityManager mActivityManager;

    public class MyBinder extends Binder{
        TrafficService getService(){return TrafficService.this;}
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.i("tag", "binder");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Notification notification = new Notification();
//        startForeground(-1, notification);
        Log.i("tag", "here");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        flags = START_STICKY_COMPATIBILITY;
        Log.i("tag", "start service");

        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent service = new Intent(this,TrafficService.class);
        this.startService(service);
    }

    public void getTrafficInfo(String trafficPath,long time,String packageName){
        try {
            file = new File(trafficPath);
            startGet(packageName);
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < time){
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mTimer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startGet(final String packageName) throws Exception {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                int count = 0;
                StringBuffer sb = new StringBuffer();
                PackageManager pm = getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
                int uid = ai.uid;
                @Override
                public void run() {
                    int pid = -1;
                    mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        List<AndroidAppProcess> processInfos = ProcessManager.getRunningAppProcesses();
                        for(AndroidAppProcess processInfo : processInfos){
                            if (packageName.equals(processInfo.name)){
                                pid = processInfo.pid;
                                break;
                            }
                        }
                    }else{
                        List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager.getRunningAppProcesses();
                        for (ActivityManager.RunningAppProcessInfo appProcessInfo:appProcessList){
                            if (appProcessInfo.processName.equals(packageName)){
                                pid = appProcessInfo.pid;
                                break;
                            }
                        }
                    }
                    int[] myMempid = new int[]{pid};
                    Debug.MemoryInfo[] memoryInfo = mActivityManager.getProcessMemoryInfo(myMempid);
                    int memSize = memoryInfo[0].dalvikPrivateDirty;
                    sb.append(String.valueOf(trafficInfo.getCurrentTime()) + "    ");
                    sb.append(String.valueOf(TrafficStats.getUidRxBytes(uid) + "    "));
                    sb.append(String.valueOf(TrafficStats.getUidTxBytes(uid) + "    "));
                    sb.append(String.valueOf(memSize));
                    sb.append("\n");
                    count++;
                    if(count >= 10){
                        trafficInfo.writeFiles(file,sb.toString());
                        sb = new StringBuffer();
                        count = 0;
                    }
                }
            }, 1000, 1000);
        }
    }
}
