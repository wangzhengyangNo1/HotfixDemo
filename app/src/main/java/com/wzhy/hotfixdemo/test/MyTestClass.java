package com.wzhy.hotfixdemo.test;

import com.wzhy.hotfixdemo.MyApplication;

/**
 * Created by wangzhengyang on 2017/6/8.
 */

public class MyTestClass {

    public void testFix(){
        int a = 10;
        int b = 0;
        MyApplication.showToast("shit: " + a/b);
    }
}
