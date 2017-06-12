package com.wzhy.hotfixdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.wzhy.hotfixdemo.hotfix.Consts;
import com.wzhy.hotfixdemo.hotfix.FixDexUtils;
import com.wzhy.hotfixdemo.test.MyTestClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ActivityMain extends AppCompatActivity {

    private Button mBtnTest;
    private Button mBtnFix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnTest = (Button) findViewById(R.id.btn_test);
        mBtnFix = (Button) findViewById(R.id.btn_fix);

        mBtnTest.setOnClickListener(mClickListener);
        mBtnFix.setOnClickListener(mClickListener);
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_test: //测试
                    new MyTestClass().testFix();
                    break;
                case R.id.btn_fix: //修复
                    //fixBug();

                    checkAndRequestStoragePermission();

                    break;
            }
        }
    };

    private void fixBug(){
        //目录：/data/data/packagename/odex
        File fileDir = getDir(Consts.DEX_DIR, Context.MODE_PRIVATE);
        //往目录下面放置我们修复好的dex文件
        String name = "classes2.dex";
        String filePath = fileDir.getAbsolutePath() + File.separator + name;
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        //搬家：把下载好的在SD卡里面的修复了的classes2.dex搬到应用目录filePath
        FileInputStream is = null;
        FileOutputStream os = null;

        try {
            //String storagePath = FileUtils.getStoragePath(this, false);

            String storagePath = "";
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
            is = new FileInputStream(storagePath + File.separator + name);

            os = new FileOutputStream(filePath);
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1){
                os.write(buffer, 0, len);
            }

            File f = new File(filePath);
            if (f.exists()) {
                MyApplication.showToast("dex 重写成功");
            }
            //热修复
            FixDexUtils.loadFixedDex(this);

            os.flush();
            os.close();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private void checkAndRequestStoragePermission() {
        if (ContextCompat.checkSelfPermission(ActivityMain.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(ActivityMain.this, permissions, Consts.PERMISSION_CODE_STORAGE);
        } else {
            fixBug();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Consts.PERMISSION_CODE_STORAGE) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }
            }
            if (allGranted){
                fixBug();
            } else {
                MyApplication.showToast("Permission Denied");
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
