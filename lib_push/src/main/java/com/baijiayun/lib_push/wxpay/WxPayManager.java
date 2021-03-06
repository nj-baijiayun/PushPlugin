package com.baijiayun.lib_push.wxpay;

import android.widget.Toast;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * 微信支付管理
 */
public class WxPayManager {
    private static WxPayManager wxPayManager;
    public static WxPayManager getInstance(){
        synchronized (WxPayManager.class){
            if(wxPayManager==null){
                wxPayManager=new WxPayManager();
            }
        }
        return wxPayManager;
    }

    /**
     * 调起支付
     * @param wxPayConfig
     */
    public void sendPay(WxPayConfig wxPayConfig){
        IWXAPI msgApi = WXAPIFactory.createWXAPI(wxPayConfig.getActivity(), wxPayConfig.getAppId(),false);
        if (!msgApi.isWXAppInstalled()){
            Toast.makeText(wxPayConfig.getActivity(), "请先下载微信APP", Toast.LENGTH_SHORT).show();
            return;
        }
        msgApi.registerApp(wxPayConfig.getAppId());
        PayReq req=new PayReq();
        req.appId = wxPayConfig.getAppId();
        req.partnerId = wxPayConfig.getPartnerid();
        req.prepayId = wxPayConfig.getPrepayid();
        req.packageValue =wxPayConfig.getPackagex();
        req.nonceStr = wxPayConfig.getNoncestr();
        req.timeStamp = wxPayConfig.getTimestamp();
        req.sign = wxPayConfig.getSign();
        msgApi.sendReq(req);
    }
}
