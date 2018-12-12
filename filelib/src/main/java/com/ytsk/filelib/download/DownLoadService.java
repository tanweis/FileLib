package com.ytsk.filelib.download;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import static com.ytsk.filelib.util.InstallUtil.installApk;


public class DownLoadService extends IntentService {

    private String TAG=getClass().getSimpleName();


    public DownLoadService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        update();
    }


    private DownLoad mDownLoad;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private String channelId = "goods";

//    private Version mVersion;
    private String downloadUrl;

    private String title;
    private String msg;
    @IdRes
    private int icon;


    void  init(){
        mDownLoad = new DownLoad(mHandler);
        mNotificationManager = ((NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel1", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true); //是否在桌面icon右上角展示小红点
            channel.setLightColor(Color.GREEN); //小红点颜色
            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
            mNotificationManager.createNotificationChannel(channel);
        }
        mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        mBuilder.setContentTitle(title)
                .setContentText(msg)
                .setSmallIcon(icon);
    }

    void update(){
        if (mDownLoad.isRunning()) {
            Toast.makeText(getApplicationContext(),"正在更新",Toast.LENGTH_SHORT).show();
            return;
        }
        mDownLoad.setUrl(downloadUrl);
        File downDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        mDownLoad.setSaveFile(downDir.getAbsolutePath() + "/" + System.currentTimeMillis() + ".apk");
        mDownLoad.start();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case DownLoad.FINISH:
                    mDownLoad = new DownLoad(mHandler);
                    String savepath = ((String) msg.obj);
                    notifyOver("更新完成");
                    installApk(new File(savepath),getApplicationContext());
                    break;
                case DownLoad.ERROR:
                    mDownLoad = new DownLoad(mHandler);
                    String mes = ((String) msg.obj);
                    notifyOver(mes);
                    Toast.makeText(getApplicationContext(),mes,Toast.LENGTH_SHORT).show();
                    break;
                case DownLoad.PROGRESS:
                    int pro = ((int) msg.obj);
                    mBuilder.setProgress(100, pro, false);
                    mNotificationManager.notify(0, mBuilder.build());
                    Log.i(TAG,"progress:" + pro);
                    break;
            }
        }
    };

    private void notifyOver(String msg) {
        mBuilder.setContentText(msg)
                .setProgress(0, 0, false);
        mNotificationManager.notify(0, mBuilder.build());
    }


}
