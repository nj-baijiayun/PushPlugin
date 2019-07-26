package com.baijiayun.lib_push;


/**
 * Created by desin on 2017/1/12.
 */

public interface BaseView {
    //展示错误信息
    void showToastMsg(String msg);
    //展示错误信息
    void showToastMsg(int strIds);
    //展示等待加载动画
    void showLoadV(String msg);
    void showLoadV();
    //关闭等待动画
    void closeLoadV();

    void jumpToLogin();
}
