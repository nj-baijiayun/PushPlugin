package com.baijiayun.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.Plugin
import org.gradle.api.Project

class MyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println("plugin config start")

        project.extensions.create("pluginExt", PluginExtension)
        project.pluginExt.extensions.create("jiGuangExt", JiGuangExtension)
        project.pluginExt.extensions.create("uMengExt", UMengExtension)


        project.android.defaultConfig {
            manifestPlaceholders = [
                    JPUSH_PKGNAME : "${project.pluginExt.packageName}",
                    JPUSH_APPKEY  : "${project.pluginExt.jiGuangExt.jPushKey}", //JPush 上注册的包名对应的 Appkey.
                    JPUSH_CHANNEL : "${project.pluginExt.jiGuangExt.JGPushChannel}", //暂时填写默认值即可.
                    TENCENT_APPID : "${project.pluginExt.jiGuangExt.JGQQShareKey}",
                    FACEBOOK_APPID: "${project.pluginExt.jiGuangExt.JGFaceBookShareKey}",
                    JSHARE_PKGNAME: "${project.pluginExt.packageName}",
            ]
            ndk {
                //选择要添加的对应 cpu 类型的 .so 库。
                abiFilters 'armeabi', 'armeabi-v7a', 'arm64-v8a'
                // 还可以添加 'x86', 'x86_64', 'mips', 'mips64'
            }

        }

        project.afterEvaluate {
//            project.android
            def spear = File.separator
            def packageName = project.pluginExt.packageName
            println(packageName)
            String packageNamePath = project.pluginExt.packageName.replaceAll("\\.", spear)

            def rootPath = project.projectDir.path
            def pluginPath = project.getRootProject().project("myplugin").projectDir.path
            println("pluginPath:$pluginPath")
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
                        output.getProcessManifestProvider().get().doLast {
                            String manifestContent = manifestOutputDirectory.file("AndroidManifest.xml").get().getAsFile().getText()

                            def manifestMap = ["</application>"    : " <service\n" +
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
                                               "packageNameDefault": project.pluginExt.packageName,
                                               jPushKeyDefault        : "${project.pluginExt.jiGuangExt.jPushKey}", //JPush 上注册的包名对应的 Appkey.
                                               "developer-default"       : "${project.pluginExt.jiGuangExt.JGPushChannel}", //暂时填写默认值即可.
                                               JGQQShareKeyDefault       : "${project.pluginExt.jiGuangExt.JGQQShareKey}",
                                               JGFaceBookShareKeyDefault      : "${project.pluginExt.jiGuangExt.JGFaceBookShareKey}",
                                               ]
                            String fileContent = replaceText(manifestContent, manifestMap)
                            manifestOutputDirectory.file("AndroidManifest.xml").get().getAsFile().write(fileContent)

                        }
                    }
                }


//                创建wxapi文件

                println("$pluginPath${spear}wxapi")
                println("~~~~~~~~~~~~~~~~~~~~~~~~~")
                if (!FileUtil.isFileExists(wxShareFilePath)) {
                    project.copy {
                        from("$pluginPath${spear}wxapi${spear}WXEntryActivity.java")
                        //into 是一个方法：指定拷贝的目的地>拷贝到根工程的output目录下
                        into "$wxDirPath"

                    }
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

                project.copy {
                    from("$pluginPath${spear}wxapi${spear}JGShareSDK.xml")
                    //into 是一个方法：指定拷贝的目的地>拷贝到根工程的output目录下
                    into "$assetsPath"
                }
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

                project.android. applicationVariants.all { ApplicationVariant variant ->  //3.2
                    variant.outputs.all { BaseVariantOutput output ->
                        output.getProcessManifestProvider().get().doLast {
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


//                String wxUMengShareFilePath = "$wxDirPath${spear}WXEntryActivity.java"
//                if (!FileUtil.isFileExists(wxUMengShareFilePath)) {
//                    project.copy {
//                        from("$pluginPath${spear}wxapi${spear}WXUmengEntryActivity.java")
//                        //into 是一个方法：指定拷贝的目的地>拷贝到根工程的output目录下
//                        into "$wxDirPath"
//                        rename { String fileName ->
//                            fileName.replace('Umeng', '')
//                        }
//
//                    }
//                }
//                def wxUMengEntryMap = [
//                        replacePackageName: "${project.pluginExt.packageName}.wxapi"
//                ]
//                replaceText(new File(wxUMengShareFilePath), wxUMengEntryMap)
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


}


