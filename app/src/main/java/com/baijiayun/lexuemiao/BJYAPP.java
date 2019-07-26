package com.baijiayun.lexuemiao;

import android.app.Application;
import android.util.Log;

import com.baijiayun.lib_push.PushHelper;
import com.umeng.message.IUmengRegisterCallback;

public class BJYAPP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PushHelper.getInstance().initUMengPush(this, new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String s) {
                Log.e("main1", "success");
            }

            @Override
            public void onFailure(String s, String s1) {

            }
        });

        PushHelper.getInstance().initUMengAnalytics(this);
        PushHelper.getInstance().initJGPush(this, true);
        PushHelper.getInstance().initJGShare(this, true);
        PushHelper.getInstance().initJGAnalytics(this,true);
    }
}
