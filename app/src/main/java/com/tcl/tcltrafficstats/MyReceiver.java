package com.tcl.tcltrafficstats;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }
    private final String TAG = "tag";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i(TAG, "监听到开机启动getAction");
        }else if(intent.getAction().equals(Intent.ACTION_TIME_TICK)){
            Log.i(TAG, "监听到TIME_TICK");
            boolean isServiceRunning = false;
            ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service :manager.getRunningServices(Integer.MAX_VALUE)) {
                if("com.yin.service.MyService".equals(service.service.getClassName()))
                //Service的全类名
                {
                    isServiceRunning = true;
                    Log.i(TAG, "已经启动");
                }

            }
            if (!isServiceRunning) {
                Intent i = new Intent(context, TrafficService.class);
                context.startService(i);
                Log.i(TAG, "没有启动，现在启动");
            }

        }else {
            Log.i(TAG, "监听到其他");
        }
    }
}
