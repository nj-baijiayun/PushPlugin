package com.baijiayun.plugin

import com.android.build.gradle.internal.dsl.BuildType
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.regex.Matcher

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
                        //这里的JPUSH_PKGNAME主要是用来替换manidest中对象
                        JPUSH_PKGNAME : "${project.pluginExt.applicationId}",
                        JPUSH_APPKEY  : "${project.pluginExt.jiGuangExt.jPushKey}", //JPush 上注册的包名对应的 Appkey.
                        JPUSH_CHANNEL : "${project.pluginExt.jiGuangExt.JGPushChannel}", //暂时填写默认值即可.
                        TENCENT_APPID : "${project.pluginExt.jiGuangExt.JGQQShareKey}",
                        FACEBOOK_APPID: "${project.pluginExt.jiGuangExt.JGFaceBookShareKey}",
                        //这里的分享包名,极光让我们使用appid,然后微信回调也用了这个id,所以当我们appid和包名不一样的时候,就会出问题
                        //所以下面做了单独处理,替换包名
                        JSHARE_PKGNAME: "${project.pluginExt.applicationId}",
                ]
            }
        }

        /*
        *
        * 如果在apply里面直接设置manifestplaceholder,那么app的buildconfig还没有初始化完成,我们这边设置的值,都会覆盖掉我们
        * 设置的值,所以我们得在设置完成之后设置
        *
        * */

        project.afterEvaluate {
//            project.android
            def spear = Matcher.quoteReplacement(File.separator)
            def packageName = project.pluginExt.packageName
            println(packageName)

            //这里遇到一个问题,就是当我们包名和appID不一样时,
//            我们的wxapi文件夹放在包名路径下,会收不到回调.
//            只有放在appId路径下才能成功,所以这边会在不同目录创建
            String applicationIdPath = project.pluginExt.applicationId.replaceAll("\\.", spear)

            def rootPath = project.projectDir.path
            def wxDirPath = "${rootPath}${spear}src${spear}main${spear}java${spear}${applicationIdPath}${spear}wxapi"
//            创建wxapi文件夹
//            FileUtil.createOrExistsDir(wxDirPath)

            String wxPayFilePath = "$wxDirPath${spear}WXPayEntryActivity.java"
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
                            def facebookShareKey = project.pluginExt.jiGuangExt.JGFaceBookShareKey;
                            if (facebookShareKey.equals("JGFaceBookShareKeyDefault")) {
                                facebookShareKey = project.pluginExt.applicationId;
                            }
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
                                    "</application>".replaceAll("\n", System.lineSeparator()),
                                               "applicationIdDefault"   : project.pluginExt.applicationId,
                                               jPushKeyDefault          : "${project.pluginExt.jiGuangExt.jPushKey}",
                                               "developer-default"      : "${project.pluginExt.jiGuangExt.JGPushChannel}",
                                               JGQQShareKeyDefault      : "${project.pluginExt.jiGuangExt.JGQQShareKey}",
                                               JGFaceBookShareKeyDefault: "${facebookShareKey}",
//                                               "wxapi.WXEntryActivity"  : "WXEntryActivity"
                            ]

                            String fileContent = replaceText(manifestContent, manifestMap)
                            manifestOutputDirectory.file("AndroidManifest.xml").get().getAsFile().write(fileContent, "UTF-8")

                        }
                    }
                }


//            生成assest并且移动分享配置文件
                def assetsPath = "${rootPath}${spear}src${spear}main${spear}assets"
                FileUtil.createOrExistsDir(assetsPath)
//
                println("jdsharepath:${assetsPath}${spear}JGShareSDK.xml")
                FileUtil.createAndWriteFile(jgShareSdk, "${assetsPath}${spear}JGShareSDK.xml")
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
                replaceText(new File("${assetsPath}${spear}JGShareSDK.xml"), shareEntryMap)
            }

            //        ********************************以下是友盟相关***********************************

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

    String jgShareSdk = '''<?xml version="1.0" encoding="utf-8"?>
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


