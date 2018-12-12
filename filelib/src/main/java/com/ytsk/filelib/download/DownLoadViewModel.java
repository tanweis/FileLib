package com.ytsk.filelib.download;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

public class DownLoadViewModel extends ViewModel {

    private String TAG=getClass().getSimpleName();

    public static final int FINISH=-1;
    public static final int ERROR=-2;


    public interface Action {
        void action(Object o);
    }

    private DownLoad mDownLoad;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private String channelId = "goods";


    private String downloadUrl;

    private Action callback;

    private WeakReference<Context> mContextWeakReference;

    private String title;
    private String msg;
    @IdRes
    private int icon;

    private MutableLiveData<Integer> progressLiveData=new MutableLiveData<>();

    public DownLoadViewModel() {

    }

    public void init(String downloadUrl, Action action2){
        this.downloadUrl=downloadUrl;
        this.callback=action2;
        init();
    }

    private void  init(){
        if (mContextWeakReference.get()==null){
            throw new  NullPointerException();
        }
        mDownLoad = new DownLoad(mHandler);
        mNotificationManager = ((NotificationManager) mContextWeakReference.get().getSystemService(Context.NOTIFICATION_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel1", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true); //是否在桌面icon右上角展示小红点
            channel.setLightColor(Color.GREEN); //小红点颜色
            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
            mNotificationManager.createNotificationChannel(channel);
        }
        mBuilder = new NotificationCompat.Builder(mContextWeakReference.get(), channelId);
        mBuilder.setContentTitle(title)
                .setContentText(msg)
                .setSmallIcon(icon);
    }

    public void update(){
        if (mDownLoad.isRunning()) {
            showToast("正在更新");
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
                    if (callback!=null)
                        callback.action(savepath);
                    progressLiveData.setValue(FINISH);
                    break;
                case DownLoad.ERROR:
                    mDownLoad = new DownLoad(mHandler);
                    String mes = ((String) msg.obj);
                    notifyOver(mes);
                    showToast(mes);
                    progressLiveData.setValue(ERROR);
                    break;
                case DownLoad.PROGRESS:
                    int pro = ((int) msg.obj);
                    mBuilder.setProgress(100, pro, false);
                    mNotificationManager.notify(0, mBuilder.build());
                    Log.i(TAG,"progress:"+pro);
                    progressLiveData.setValue(pro);
                    break;
            }
        }
    };

    private void notifyOver(String msg) {
        mBuilder.setContentText(msg)
                .setProgress(0, 0, false);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private void showToast(String msg){
        if (mContextWeakReference!=null&&mContextWeakReference.get()!=null){
            Toast.makeText(mContextWeakReference.get(),msg,Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        mContextWeakReference.clear();
//        mNotificationManager=null;
    }

    public MutableLiveData<Integer> getProgressLiveData() {
        return progressLiveData;
    }

    public void setContext(Context context) {
        mContextWeakReference=new WeakReference<>(context);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
