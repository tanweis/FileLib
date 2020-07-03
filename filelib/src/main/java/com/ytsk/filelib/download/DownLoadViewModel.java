package com.ytsk.filelib.download;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.ytsk.filelib.BuildConfig;
import com.ytsk.filelib.util.DownloadUrlException;
import com.ytsk.filelib.util.ResSaveFileException;

import java.lang.ref.WeakReference;

import okhttp3.Request;

public class DownLoadViewModel extends ViewModel {

    private String TAG = getClass().getSimpleName();

    public static final int FINISH = -1;
    public static final int ERROR = -2;


    public interface Action {
        void action(Object o);
    }

    private DownLoad mDownLoad;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private String channelId = BuildConfig.APPLICATION_ID;


    private String downloadUrl;
    private String saveFilePath;

    private Action callback;

    private WeakReference<Context> mContextWeakReference;

    private String title;
    private String msg;
    @IdRes
    private int icon;
    private boolean showNotify = true;

    private MutableLiveData<Integer> progressLiveData = new MutableLiveData<>();

    public DownLoadViewModel() {

    }

    public void init(Action action2) {
//        this.downloadUrl = downloadUrl;
        this.callback = action2;
        init();
    }

    private void init() {
        Context context = mContextWeakReference.get();
        if (context == null) {
            throw new NullPointerException();
        }
        mDownLoad = new DownLoad(mHandler);

        mNotificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "download", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true); //是否在桌面icon右上角展示小红点
            channel.setLightColor(Color.GREEN); //小红点颜色
            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
            mNotificationManager.createNotificationChannel(channel);
        }
        mBuilder = new NotificationCompat.Builder(context, channelId);
        mBuilder.setContentTitle(title)
                .setContentText(msg)
                .setSmallIcon(icon);
    }

    public void addHeader(String key, String value) {
        if (mDownLoad == null) {
            throw new NullPointerException("需要先调用init方法");
        }
        mDownLoad.addHeader(key, value);
    }

    public void startDownload() throws DownloadUrlException, ResSaveFileException {
        if (mDownLoad.isRunning()) {
            showToast("正在下载");
            return;
        }
        try {
            Request.Builder builder = new Request.Builder().url(downloadUrl);
        } catch (Exception e) {
            throw new DownloadUrlException();
        }
        try {
            Uri.parse(saveFilePath);
        } catch (Exception e) {
            throw new ResSaveFileException();
        }

        mDownLoad.setUrl(downloadUrl);
//        File downDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        String fn = downDir.getAbsolutePath() + "/" + System.currentTimeMillis() + ".apk";
        mDownLoad.setSaveFile(saveFilePath);
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
                    notifyOver("下载完成");
                    if (callback != null)
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
                    DownLoadViewModel.this.notify(pro, mBuilder, 100);
                    progressLiveData.setValue(pro);
                    break;
            }
        }
    };

    private void notify(int pro, NotificationCompat.Builder builder, int i) {
        if (showNotify) {
            builder.setProgress(i, pro, false);
            mNotificationManager.notify(0, mBuilder.build());
        }
    }

    private void notifyOver(String msg) {
        notify(0, mBuilder.setContentText(msg), 0);
    }

    private void showToast(String msg) {
        if (mContextWeakReference != null && mContextWeakReference.get() != null) {
            Toast.makeText(mContextWeakReference.get(), msg, Toast.LENGTH_SHORT).show();
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
        mContextWeakReference = new WeakReference<>(context);
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

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setSaveFilePath(String saveFilePath) {
        this.saveFilePath = saveFilePath;
    }

    public void setShowNotify(boolean showNotify) {
        this.showNotify = showNotify;
    }
}
