package com.baijiayun.lib_push;

import android.content.Context;

import com.dsg.lib_push.BuildConfig;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;

import cn.jiguang.analytics.android.api.JAnalyticsInterface;
import cn.jiguang.share.android.api.AuthListener;
import cn.jiguang.share.android.api.JShareInterface;
import cn.jiguang.share.android.api.PlatActionListener;
import cn.jiguang.share.android.api.Platform;
import cn.jiguang.share.android.api.ShareParams;
import cn.jpush.android.api.JPushInterface;

public class PushHelper {
    private static volatile PushHelper sInsatance;

    private PushHelper() {

    }

    public static PushHelper getInstance() {
        if (sInsatance == null) {
            synchronized (PushHelper.class) {
                if (sInsatance == null) {
                    sInsatance = new PushHelper();
                }
            }
        }
        return sInsatance;
    }

    public void initJGAnalytics(Context context, boolean debugMode) {
        JAnalyticsInterface.setDebugMode(debugMode);
        JAnalyticsInterface.init(context);
    }

    public void initJGPush(Context context, boolean debugMode) {
        JPushInterface.setDebugMode(debugMode);
        JPushInterface.init(context);
    }

    public void initJGShare(Context context, boolean debugMode) {
        JShareInterface.setDebugMode(debugMode);
        JShareInterface.init(context);
    }

    public void initUMengAnalytics(Context context) {
        UMConfigure.setLogEnabled(BuildConfig.DEBUG);
        UMConfigure.init(context, BuildConfig.UMengKey, "Umeng", UMConfigure.DEVICE_TYPE_PHONE, BuildConfig.UMengSecret);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        PushAgent.getInstance(context).onAppStart();
    }

    //    返回token
    public void initUMengPush(Context context, IUmengRegisterCallback iUmengRegisterCallback) {
        UMConfigure.setLogEnabled(BuildConfig.DEBUG);

        UMConfigure.init(context, BuildConfig.UMengKey, "Umeng", UMConfigure.DEVICE_TYPE_PHONE, BuildConfig.UMengSecret);
        PushAgent mPushAgent = PushAgent.getInstance(context);
//注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(iUmengRegisterCallback);
    }

    public void setUmengNotificationClickHandler(Context context, UmengNotificationClickHandler umengNotificationClickHandler) {
        PushAgent.getInstance(context).setNotificationClickHandler(umengNotificationClickHandler);
    }


    /*
    *     * name 平台名称，值可选 Wechat.Name、SinaWeibo.Name、QQ.Name、Facebook.Name、Twitter.Name、JChatPro.Name。
            shareParams 各平台可分享各自的配置
     * */
    public boolean shareWithPlatform(String plantName, ShareParams shareParams, PlatActionListener platActionListener) {
        if (!JShareInterface.isClientValid(plantName)) {
            return false;
        }
        if (shareParams == null) {
            shareParams = new ShareParams();
            shareParams.setShareType(Platform.SHARE_TEXT);
            shareParams.setText("标题内容");
        }
        JShareInterface.share(plantName, shareParams, platActionListener);
        return true;
    }

    /*
    * name 平台名称，值可选 Wechat.Name、SinaWeibo.Name、QQ.Name、Facebook.Name、Twitter.Name、JChatPro.Name。
        authListener 回调接口，可为 null，为 null 时则没有回调。
*/
    public void thirdPlatformLogin(String platformName, AuthListener authListener) {
        JShareInterface.authorize(platformName, authListener);
    }
}
