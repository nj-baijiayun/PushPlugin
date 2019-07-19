package com.baijiayun.lexuemiao;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.baijiayun.lib_push.MobHookActivity;
import com.baijiayun.lib_push.PushHelper;

import java.util.HashMap;

import cn.jiguang.share.android.api.AuthListener;
import cn.jiguang.share.android.api.PlatActionListener;
import cn.jiguang.share.android.api.Platform;
import cn.jiguang.share.android.api.ShareParams;
import cn.jiguang.share.android.model.AccessTokenInfo;
import cn.jiguang.share.android.model.BaseResponseInfo;
import cn.jiguang.share.android.utils.Logger;
import cn.jiguang.share.qqmodel.QQ;
import cn.jiguang.share.wechat.Wechat;


public class MainActivity extends MobHookActivity implements View.OnClickListener {

    private Button mBtnQq;
    private Button mBtnWx;
    private Button mBtnShare;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnQq = (Button) findViewById(R.id.btn_qq);
        mBtnWx = (Button) findViewById(R.id.btn_wx);
        mBtnShare = (Button) findViewById(R.id.btn_share);
        mBtnQq.setOnClickListener(this);
        mBtnWx.setOnClickListener(this);
        mBtnShare.setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_qq:
                PushHelper.getInstance().thirdPlatformLogin(QQ.Name, new AuthListener() {
                    @Override
                    public void onComplete(Platform platform, int i, BaseResponseInfo data) {
                        if (data instanceof AccessTokenInfo) {        //授权信息
                            String token = ((AccessTokenInfo) data).getToken();//token
                            long expiration = ((AccessTokenInfo) data).getExpiresIn();//token有效时间，时间戳
                            String refresh_token = ((AccessTokenInfo) data).getRefeshToken();//refresh_token
                            String openid = ((AccessTokenInfo) data).getOpenid();//openid
                            //授权原始数据，开发者可自行处理
                            String originData = data.getOriginData();
                            Logger.dd("main1", "openid:" + openid + ",token:" + token + ",expiration:" + expiration + ",refresh_token:" + refresh_token);
                            Logger.dd("main1", "originData:" + originData);
                        }
                    }

                    @Override
                    public void onError(Platform platform, int i, int i1, Throwable throwable) {
                        Log.e("main1","授权失败");
                    }

                    @Override
                    public void onCancel(Platform platform, int i) {
                        Log.e("main1","授权取消");

                    }
                });
                break;
            case R.id.btn_wx:
                PushHelper.getInstance().thirdPlatformLogin(Wechat.Name, new AuthListener() {
                    @Override
                    public void onComplete(Platform platform, int i, BaseResponseInfo data) {
                        if (data instanceof AccessTokenInfo) {        //授权信息
                            String token = ((AccessTokenInfo) data).getToken();//token
                            long expiration = ((AccessTokenInfo) data).getExpiresIn();//token有效时间，时间戳
                            String refresh_token = ((AccessTokenInfo) data).getRefeshToken();//refresh_token
                            String openid = ((AccessTokenInfo) data).getOpenid();//openid
                            //授权原始数据，开发者可自行处理
                            String originData = data.getOriginData();
                            Logger.dd("main1", "openid:" + openid + ",token:" + token + ",expiration:" + expiration + ",refresh_token:" + refresh_token);
                            Logger.dd("main1", "originData:" + originData);
                        }
                    }

                    @Override
                    public void onError(Platform platform, int i, int i1, Throwable throwable) {
                        Log.e("main1","授权失败");
                    }

                    @Override
                    public void onCancel(Platform platform, int i) {
                        Log.e("main1","授权取消");

                    }
                });
                break;

            case R.id.btn_share:
                ShareParams shareParams = new ShareParams();
                shareParams.setShareType(Platform.SHARE_TEXT);
                shareParams.setText("分享");
                PushHelper.getInstance().shareWithPlatform(Wechat.Name, shareParams, new PlatActionListener() {
                    @Override
                    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                            Log.e("main1","onComplete");
                    }

                    @Override
                    public void onError(Platform platform, int i, int i1, Throwable throwable) {
                        Log.e("main1","onError"+throwable.getMessage());

                    }

                    @Override
                    public void onCancel(Platform platform, int i) {
                        Log.e("main1","onCancel");

                    }
                });
                break;
        }
    }





}
