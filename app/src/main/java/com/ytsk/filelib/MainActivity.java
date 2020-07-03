package com.ytsk.filelib;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "没有外部存储", Toast.LENGTH_SHORT).show();
            return;
        }
        String extDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        String dir = extDir + "/com/ytsk/gcbandNew/";
        File dirf = new File(dir);
        if (!dirf.exists()) {
            boolean dr=dirf.mkdirs();
            Log.i("main","dir res:"+dr);
        }
        String fn = dir  +"aaa.xlsx";
        File sf = new File(fn);
        if (sf.exists()) {
            sf.delete();
        } else {
            try {
                sf.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
