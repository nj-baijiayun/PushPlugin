package com.baijiayun.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.dsl.ProductFlavor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class MyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("plugin config start")

        project.extensions.create("pluginExt", PluginExtension)
        project.pluginExt.extensions.create("jiGuangExt", JiGuangExtension)

        project.pluginExt.extensions.create("uMengExt", UMengExtension)
        project.tasks.findByName("preBuild").doFirst {
            project.android.buildTypes.each { BuildType buildType ->
                buildType.manifestPlaceholders = [
                        JPUSH_PKGNAME : "${project.pluginExt.packageName}",
                        JPUSH_APPKEY  : "${project.pluginExt.jiGuangExt.jPushKey}", //JPush 上注册的包名对应的 Appkey.
                        JPUSH_CHANNEL : "${project.pluginExt.jiGuangExt.JGPushChannel}", //暂时填写默认值即可.
                        TENCENT_APPID : "${project.pluginExt.jiGuangExt.JGQQShareKey}",
                        FACEBOOK_APPID: "${project.pluginExt.jiGuangExt.JGFaceBookShareKey}",
                        JSHARE_PKGNAME: "${project.pluginExt.packageName}",
                ]
            }
        }

//        project.tasks.findByName("preBuild").doLast {
//            project.tasks.each { Task task ->
//                if (task.name.startsWith("check") && task.name.endsWith("Manifest")) {
//                    println("taskName:${task.name}")
//                    task.doLast {
//                        println(task.project.android.defaultConfig.manifestPlaceholders)
//                        Map manifestHolders = task.project.android.defaultConfig.manifestPlaceholders
//                        manifestHolders.each { key, value ->
//                            println("before  key: ${key} value:${value}")
//                        }
//                        manifestHolders.put("JPUSH_PKGNAME", "${project.pluginExt.packageName}")
//                        manifestHolders.put('JPUSH_APPKEY', "${project.pluginExt.jiGuangExt.jPushKey}")
//                        manifestHolders.put('JPUSH_CHANNEL', "${project.pluginExt.jiGuangExt.JGPushChannel}")
//                        manifestHolders.put('TENCENT_APPID', "${project.pluginExt.jiGuangExt.JGQQShareKey}")
//                        manifestHolders.put('FACEBOOK_APPID', "${project.pluginExt.jiGuangExt.JGFaceBookShareKey}")
//                        manifestHolders.put('JSHARE_PKGNAME', "${project.pluginExt.packageName}")
//                        manifestHolders = task.project.android.defaultConfig.manifestPlaceholders
//                        manifestHolders.each { key, value ->
//                            println("after  key: ${key} value:${value}")
//                        }
//                    }
//                }
//                if (task.name.startsWith("process") && task.name.endsWith("Manifest")) {
//                    task.doFirst {
//                        println(task.project.android.defaultConfig.manifestPlaceholders)
//                        println(task.project.android.defaultConfig.manifestPlaceholders.getClass().name)
//                    }
//                }
//
//            }
//        }


        /*
        *
        * 如果在apply里面直接设置manifestplaceholder,那么app的buildconfig还没有初始化完成,我们这边设置的值,都会覆盖掉我们
        * 设置的值,所以我们得在设置完成之后设置
        *
        * */


//
//        project.android.defaultConfig {
//            manifestPlaceholders = [
//                    JPUSH_PKGNAME : "${project.pluginExt.packageName}",
//                    JPUSH_APPKEY  : "${project.pluginExt.jiGuangExt.jPushKey}", //JPush 上注册的包名对应的 Appkey.
//                    JPUSH_CHANNEL : "${project.pluginExt.jiGuangExt.JGPushChannel}", //暂时填写默认值即可.
//                    TENCENT_APPID : "${project.pluginExt.jiGuangExt.JGQQShareKey}",
//                    FACEBOOK_APPID: "${project.pluginExt.jiGuangExt.JGFaceBookShareKey}",
//                    JSHARE_PKGNAME: "${project.pluginExt.packageName}",
//            ]
//            ndk {
//                //选择要添加的对应 cpu 类型的 .so 库。
//                abiFilters 'armeabi', 'armeabi-v7a', 'arm64-v8a'
//                // 还可以添加 'x86', 'x86_64', 'mips', 'mips64'
//            }
//
//        }
        project.afterEvaluate {
//            project.android
            def spear = File.separator
            def packageName = project.pluginExt.packageName
            println(packageName)
            String packageNamePath = project.pluginExt.packageName.replaceAll("\\.", spear)

            def rootPath = project.projectDir.path
            def wxDirPath = "${rootPath}${spear}src${spear}main${spear}java${spear}${packageNamePath}${spear}wxapi"
            println("wxDirPath:$wxDirPath")
//            创建wxapi文件夹
            FileUtil.createOrExistsDir(wxDirPath)

            String wxShareFilePath = "$wxDirPath${spear}WXEntryActivity.java"
            String wxPayFilePath = "$wxDirPath${spear}WXPayEntryActivity.java"
            println(wxShareFilePath)
            println(project.pluginExt.jiGuangExt.jPushKey.class)

            //        ********************************以下是是否创建wxPayEntryactivity***********************************
            if (project.pluginExt.isNeedPayEntryActivityDefault) {
                println("~~~~~~~~~~~~~~~~~~~~~~~~~3")
                FileUtil.createAndWriteFile(wxPayEntryActivity, wxPayFilePath)
                def wxPayMap = [
                        replacePackageName   : "${project.pluginExt.packageName}.wxapi",
                        replaceWxPayIdDefault: project.pluginExt.wxPayId,
                        activityDefault      : project.pluginExt.handlePayMessageActivity

                ]
                println("~~~~~~~~~~~~~~~~~~~~~~~~~2")
                replaceText(new File(wxPayFilePath), wxPayMap)

            }

            //        ********************************以下是极光相关***********************************
            if (!project.pluginExt.jiGuangExt.jPushKey.equals("jPushKey")) {
                //            manifest中添加极光推送service
                project.getRootProject().project("app").android.applicationVariants.all { variant ->  //3.2
                    variant.outputs.all { output ->
//                        processManifest  gradle3.2不支持getProcessManifestProvider
                        output.processManifest.doLast {
                            String manifestContent = manifestOutputDirectory.file("AndroidManifest.xml").get().getAsFile().getText()

                            def manifestMap = ["</application>"         : " <service\n" +
                                    "                        android:name=\"com.baijiayun.lib_push.LibPushService\"\n" +
                                    "                        android:enabled=\"true\"\n" +
                                    "                        android:exported=\"false\"\n" +
                                    "                        android:process=\":pushcore\">\n" +
                                    "                                <intent-filter>\n" +
                                    "                                <action android:name=\"cn.jiguang.user.service.action\" />\n" +
                                    "                                </intent-filter>\n" +
                                    "        </service>" +
                                    "<receiver\n" +
                                    "       android:name=\"com.baijiayun.lib_push.LibPushReceiver\"\n" +
                                    "       android:enabled=\"true\" \n" +
                                    "       android:exported=\"false\" >\n" +
                                    "       <intent-filter>\n" +
                                    "            <action android:name=\"cn.jpush.android.intent.RECEIVE_MESSAGE\" />\n" +
                                    "            <category android:name=\"${project.pluginExt.packageName}\" />\n" +
                                    "       </intent-filter>\n" +
                                    " </receiver>" +
                                    "</application>",
                                               "packageNameDefault"     : project.pluginExt.packageName,
                                               jPushKeyDefault          : "${project.pluginExt.jiGuangExt.jPushKey}", //JPush 上注册的包名对应的 Appkey.
                                               "developer-default"      : "${project.pluginExt.jiGuangExt.JGPushChannel}", //暂时填写默认值即可.
                                               JGQQShareKeyDefault      : "${project.pluginExt.jiGuangExt.JGQQShareKey}",
                                               JGFaceBookShareKeyDefault: "${project.pluginExt.jiGuangExt.JGFaceBookShareKey}",
                            ]
                            String fileContent = replaceText(manifestContent, manifestMap)
                            manifestOutputDirectory.file("AndroidManifest.xml").get().getAsFile().write(fileContent)

                        }
                    }
                }


//                创建wxapi文件

                println("~~~~~~~~~~~~~~~~~~~~~~~~~")
                if (!FileUtil.isFileExists(wxShareFilePath)) {
                    FileUtil.createAndWriteFile(wxEntryActivity, wxShareFilePath)

                }
                println("~~~~~~~~~~~~~~~~~~~~~~~~~1")
                def wxEntryMap = [
                        replacePackageName: "${project.pluginExt.packageName}.wxapi"
                ]

                replaceText(new File(wxShareFilePath), wxEntryMap)


//            生成assest并且移动分享配置文件
                def assetsPath = "${rootPath}${spear}src${spear}main${spear}assets"
                FileUtil.createOrExistsDir(assetsPath)
//
                println("222")
                println("jdsharepath:${assetsPath}${spear}JGShareSDK.xml")
                FileUtil.createAndWriteFile(jgSkareSdk, "${assetsPath}${spear}JGShareSDK.xml")
                println("111")
                def shareEntryMap = [
                        JGWeiboShareKeyDefault      : "${project.pluginExt.jiGuangExt.JGSinaShareKey}",
                        JGWeiboShareSecretDefault   : "${project.pluginExt.jiGuangExt.JGSinaShareSecret}",
                        JGQQShareKeyDefault         : "${project.pluginExt.jiGuangExt.JGQQShareKey}",
                        JGQQShareSecretDefault      : "${project.pluginExt.jiGuangExt.JGQQShareSecret}",
                        JGWxShareKeyDefault         : "${project.pluginExt.jiGuangExt.JGWxShareKey}",
                        JGWxShareSecretDefault      : "${project.pluginExt.jiGuangExt.JGWxShareSecret}",
                        JGFaceBookShareKeyDefault   : "${project.pluginExt.jiGuangExt.JGFaceBookShareKey}",
                        JGFaceBookShareSecretDefault: "${project.pluginExt.jiGuangExt.JGFaceBookShareSecret}",

                ]
                println("333")

                replaceText(new File("${assetsPath}${spear}JGShareSDK.xml"), shareEntryMap)

            }

            //        ********************************以下是友盟相关***********************************

            if (!project.pluginExt.uMengExt.UMengKey.equals("UMengKeyDefault")) {

                project.android.applicationVariants.all { ApplicationVariant variant ->  //3.2
                    variant.outputs.all { BaseVariantOutput output ->
                        output.processManifest.doLast {
                            String manifestContent = manifestOutputDirectory.file("AndroidManifest.xml").get().getAsFile().getText()

                            def manifestMap = ["</application>": " <activity\n" +
                                    "            android:name=\".wxapi.WXEntryActivity\"\n" +
                                    "            android:configChanges=\"keyboardHidden|orientation|screenSize\"\n" +
                                    "            android:exported=\"true\"\n" +
                                    "            android:theme=\"@android:style/Theme.Translucent.NoTitleBar\" /></application>"]
                            String fileContent = replaceText(manifestContent, manifestMap)
                            manifestOutputDirectory.file("AndroidManifest.xml").get().getAsFile().write(fileContent)

                        }
                    }
                }


            }

        }


    }

    def static replaceText(String fileContent, Map<String, String> map) {
        map.each {
            def regex = "$it.key"
            fileContent = (fileContent =~ /${regex}/).replaceAll(it.value)
        }
        return fileContent

    }

    def static replaceText(File file, Map<String, String> map) {
        def fileText = file.text
        map.each {
            def regex = "$it.key"
            fileText = (fileText =~ /${regex}/).replaceAll(it.value)
            file.write(fileText)
        }

    }

    String jgSkareSdk = '''<?xml version="1.0" encoding="utf-8"?>
<DevInfor>

    <!-- 如果不需要支持某平台，可缺省该平台的配置-->
    <!-- 各个平台的KEY仅供DEMO演示，开发者要集成发布需要改成自己的KEY-->
    <!--请不要在这里改动,每次编译会被覆盖,请在app gradle文件进行配置-->
    <SinaWeibo
        AppKey="JGWeiboShareKeyDefault"
        AppSecret="JGWeiboShareSecretDefault"
        RedirectUrl="https://www.jiguang.cn"/>

    <QQ
        AppId="JGQQShareKeyDefault"
        AppKey="JGQQShareSecretDefault"/>

    <Wechat
        AppId="JGWxShareKeyDefault"
        AppSecret="JGWxShareSecretDefault"/>

    <Facebook
        AppId="JGFaceBookShareKeyDefault"
        AppName="JGFaceBookShareSecretDefault"
    />

</DevInfor>
'''
    String wxEntryActivity = '''
/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */

package replacePackageName;


import android.content.Intent;
import android.os.Bundle;

import cn.jiguang.share.wechat.WeChatHandleActivity;


/** 微信客户端回调activity示例 */
public class WXEntryActivity extends WeChatHandleActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
'''
    String wxPayEntryActivity = '''
package replacePackageName;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import com.alibaba.android.arouter.launcher.ARouter;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, "replaceWxPayIdDefault");
        api.handleIntent(getIntent(), this);
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        Message message = new Message();
        message.what = 1;
        message.obj = resp.errCode;
        ARouter.getInstance().build("activityDefault").withObject("message",message).navigation();
        finish();
    }

}
'''

}


