package com.baijiayun.lib_push;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.baijiayun.lib_push.config.ShapeTypeConfig;
import com.baijiayun.lib_push.interfaces.JShareLoginCall;
import com.dsg.lib_push.BuildConfig;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.jiguang.analytics.android.api.JAnalyticsInterface;
import cn.jiguang.share.android.api.AuthListener;
import cn.jiguang.share.android.api.JShareInterface;
import cn.jiguang.share.android.api.PlatActionListener;
import cn.jiguang.share.android.api.Platform;
import cn.jiguang.share.android.api.ShareParams;
import cn.jiguang.share.android.model.AccessTokenInfo;
import cn.jiguang.share.android.model.BaseResponseInfo;
import cn.jiguang.share.android.utils.Logger;
import cn.jiguang.share.qqmodel.QQ;
import cn.jiguang.share.qqmodel.QZone;
import cn.jiguang.share.wechat.Wechat;
import cn.jiguang.share.wechat.WechatMoments;
import cn.jiguang.share.weibo.SinaWeibo;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class PushHelper {
    private String TAG = this.getClass().getName();
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
        UMConfigure.init(context, getMetaData(context, "UMENG_APPKEY"), "Umeng", UMConfigure.DEVICE_TYPE_PHONE, getMetaData(context, "UMENG_SECRET"));
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        PushAgent.getInstance(context).onAppStart();
    }

    //    返回token
    public void initUMengPush(Context context, IUmengRegisterCallback iUmengRegisterCallback) {
        UMConfigure.setLogEnabled(BuildConfig.DEBUG);

        UMConfigure.init(context, getMetaData(context, "UMENG_APPKEY"), "Umeng", UMConfigure.DEVICE_TYPE_PHONE, getMetaData(context, "UMENG_SECRET"));
        PushAgent mPushAgent = PushAgent.getInstance(context);
//注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(iUmengRegisterCallback);
    }

    //    设置umeng推送点击事件
    public void setUmengNotificationClickHandler(Context context, UmengNotificationClickHandler umengNotificationClickHandler) {
        PushAgent.getInstance(context).setNotificationClickHandler(umengNotificationClickHandler);
    }

    private String getMetaData(Context context, String key) {
        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Bundle bundle = ai.metaData;
        return bundle.getString(key);
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


    /**
     * 吊起分享  自己定义一个Dialog,传进来,点击事件调用下面的share方法
     */
    public void openShape(Dialog dialog) {
        dialog.show();
    }

    private void share(ShapeTypeConfig type, ShareParams shareParams, PlatActionListener platActionListener) {
        if (type == ShapeTypeConfig.WX) {
            JShareInterface.share(Wechat.Name, shareParams, platActionListener);
        } else if (type == ShapeTypeConfig.WXP) {
            JShareInterface.share(WechatMoments.Name, shareParams, platActionListener);
        } else if (type == ShapeTypeConfig.QQ) {
            JShareInterface.share(QQ.Name, shareParams, platActionListener);
        } else if (type == ShapeTypeConfig.QQZONE) {
            JShareInterface.share(QZone.Name, shareParams, platActionListener);
        } else if (type == ShapeTypeConfig.SINA) {
            JShareInterface.share(SinaWeibo.Name, shareParams, platActionListener);
        }
    }

    /**
     * 第三方登录
     *
     * @param shapeTypeConfig 平台名称
     * @param call            回调
     */
    public void UserLoginByJShareLogin(ShapeTypeConfig shapeTypeConfig, final JShareLoginCall call) {
        JShareInterface.authorize(getPtName(shapeTypeConfig), new AuthListener() {
            @Override
            public void onComplete(Platform platform, int action, BaseResponseInfo data) {
                Logger.e(TAG, "----->>>action" + action + "*****");
                String toastMsg = null;
                switch (action) {
                    case Platform.ACTION_AUTHORIZING:
                        if (data instanceof AccessTokenInfo) {        //授权信息
                            String token = ((AccessTokenInfo) data).getToken();// token
                            long expiration = ((AccessTokenInfo) data).getExpiresIn();// token有效时间，时间戳
                            String refresh_token = ((AccessTokenInfo) data).getRefeshToken();//refresh_token
                            String openid = ((AccessTokenInfo) data).getOpenid();//openid
                            //授权原始数据，开发者可自行处理
                            String originData = data.getOriginData();
                            toastMsg = "授权成功:" + data.toString();
                            Logger.e(TAG, "openid:" + openid + ",token:" + token + ",expiration:" + expiration + ",refresh_token:" + refresh_token);
                            Logger.e(TAG, "originData:" + originData);
                            call.getJShareLogin((AccessTokenInfo) data, true, toastMsg);
                        }
                        break;
                }
            }

            @Override
            public void onError(Platform platform, int action, int i1, Throwable throwable) {
                String toastMsg = null;
                switch (action) {
                    case Platform.ACTION_AUTHORIZING:
                        toastMsg = "授权失败";
                        call.getJShareLogin(null, false, toastMsg);
                        break;
                }
            }

            @Override
            public void onCancel(Platform platform, int action) {
                Logger.e(TAG, "onCancel:" + platform + ",action:" + action);
                String toastMsg = null;
                switch (action) {
                    case Platform.ACTION_AUTHORIZING:
                        toastMsg = "取消授权";
                        call.getJShareLogin(null, false, toastMsg);
                        break;
                }
            }
        });
    }

    /**
     * 判断是否已经授权
     *
     * @param shapeTypeConfig
     * @return
     */
    public boolean isJuspLoginAuthorize(ShapeTypeConfig shapeTypeConfig) {
        return JShareInterface.isAuthorize(getPtName(shapeTypeConfig));
    }


    public void cancleTagAndAlias(Context context, TagAliasCallback tagAliasCallback) {
        //TODO 上下文、别名、标签、回调  退出后空数组与空字符串取消之前的设置
        Set<String> tags = new HashSet<String>();
        JPushInterface.setAliasAndTags(context, "", tags, tagAliasCallback);
    }

    /**
     * 删除标签
     *
     * @param context
     * @param userId
     */
    public void deletedJPushAlias(Context context, int userId) {
        JPushInterface.deleteAlias(context, userId);

    }


    /**
     * 设置标签  针对定向推送
     */
    public void setJPushAliasCallback(Context context, String useId, TagAliasCallback tagAliasCallback) {
//        JPushInterface.setAlias(context,tag,tags,tagAliasCallback);
        Set<String> tags = new HashSet<String>();
        String JPushTag = "push" + useId;
        tags.add(JPushTag);
        JPushInterface.setAliasAndTags(context, useId, tags, tagAliasCallback);

    }

    /**
     * 获取平台信息
     *
     * @return
     */
    public List<String> getPlats() {
        return JShareInterface.getPlatformList();
    }

    /**
     * 判断是否有效
     *
     * @param name
     * @return
     */
    public boolean isClient(String name) {
        return JShareInterface.isClientValid(name);
    }

    /**
     * 删除授权
     *
     * @param shapeTypeConfig
     * @param authListener
     */
    public void JuspLoginDeleteAuthorize(ShapeTypeConfig shapeTypeConfig, AuthListener authListener) {
        JShareInterface.removeAuthorize(getPtName(shapeTypeConfig), authListener);
    }

    private String getPtName(ShapeTypeConfig shapeTypeConfig) {
        String LoginName = "";
        if (shapeTypeConfig == ShapeTypeConfig.QQ) {
            LoginName = "QQ";
        } else if (shapeTypeConfig == ShapeTypeConfig.WX) {
            LoginName = "Wechat";
        } else {
            LoginName = "QQ";
        }
        return LoginName;
    }


}
