package com.ytsk.filelib.download;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ytsk.filelib.util.ApiException;
import com.ytsk.filelib.util.ErrorHandler;

import java.io.File;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class DownLoad extends Thread {

    private String TAG=getClass().getSimpleName();

    public static final int FINISH = 1;
    public static final int PROGRESS = 2;
    public static final int ERROR = 3;

    public static final int DOWNLOAD_CHUNK_SIZE = 2048; //Same as Okio Segment.SIZE

    private OkHttpClient mOkHttpClient;
    private Handler mHandler;

    private String url;
    private String saveFile;

    private boolean isRunning = false;


    public DownLoad(Handler handler) {
        mHandler = handler;
        mOkHttpClient = new OkHttpClient.Builder().build();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSaveFile(String saveFile) {
        this.saveFile = saveFile;
    }

    @Override
    public void run() {
        super.run();
        try {
            isRunning = true;
            Request request = new Request.Builder().url(url).build();
            Response response = mOkHttpClient.newCall(request).execute();
            ResponseBody body;
            body = response.body();
            long contentLength = body.contentLength();
            BufferedSource source = body.source();

            File file = new File(saveFile);
            BufferedSink sink = Okio.buffer(Okio.sink(file));

            long totalRead = 0;
            long read = 0;
            while ((read = (source.read(sink.buffer(), DOWNLOAD_CHUNK_SIZE))) != -1) {
                totalRead += read;
                int progress = (int) ((totalRead * 100) / contentLength);
                sendMsg(PROGRESS, progress, null);

            }
            sink.writeAll(source);
            sink.flush();
            sink.close();
            sendMsg(FINISH, -1, null);
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
            ApiException ext= ErrorHandler.handleException(e);
            sendMsg(ERROR,-1,ext.msg);
        } finally {
            isRunning = false;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    private int curPro = -1;

    private void sendMsg(int what, int progress, String msg) {
        Message message = mHandler.obtainMessage(what);
        switch (what) {
            case FINISH:
                message.obj = saveFile;
                mHandler.sendMessage(message);
                break;
            case PROGRESS:
                message.obj = progress;
                if (progress != curPro&&progress%10==0)
                    mHandler.sendMessage(message);
                curPro = progress;
                break;
            case ERROR:
                message.obj = msg;
                mHandler.sendMessage(message);
                break;
        }

    }

}
