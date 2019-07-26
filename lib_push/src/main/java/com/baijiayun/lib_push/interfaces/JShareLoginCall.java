package com.baijiayun.lib_push.interfaces;

import cn.jiguang.share.android.model.AccessTokenInfo;

/**
 * Created by Administrator on 2018/4/9.
 */

public interface JShareLoginCall {
    void getJShareLogin(AccessTokenInfo s, boolean isSuccessLogin, String toastMsg);
}
