package com.baijiayun.lib_push;


import androidx.appcompat.app.AppCompatActivity;

import com.umeng.analytics.MobclickAgent;

/**
 * @project wx_weilin
 * @class name：com.baijiayun.basic.activity
 * @describe
 * @anthor houyi QQ:1007362137
 * @time 2019/03/25 18:01
 * @change
 * @time
 * @describe
 */
public class MobHookActivity extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
