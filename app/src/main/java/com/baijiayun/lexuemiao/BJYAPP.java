package com.baijiayun.lexuemiao;

import android.app.Application;
import android.util.Log;

import com.baijiayun.lib_compiler.GenerateEntry;
import com.baijiayun.lib_push.PushHelper;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

@GenerateEntry
public class BJYAPP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PushHelper.getInstance().initUMengAnalytics(this,BuildConfig.DEBUG);
        PushHelper.getInstance().initUMengPush(this, new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String s) {
                Log.e("main1", "success:"+s);
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.e("main1", "s:"+s+"s1:"+s1);

            }
        },BuildConfig.DEBUG);
        PushAgent.getInstance(this).addAlias("111", "test", new UTrack.ICallBack() {
            @Override
            public void onMessage(boolean b, String s) {
                Log.d("main1",b+s);
            }
        });
//        PushHelper.getInstance().initUMengAnalytics(this,BuildConfig.DEBUG);
//        PushHelper.getInstance().initJGPush(this, true);
        PushHelper.getInstance().initJGShare(this, true);
//        PushHelper.getInstance().initJGAnalytics(this, true);
    }
}
