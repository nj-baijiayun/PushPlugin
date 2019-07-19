package com.baijiayun.plugin

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
                        output.processManifest.doLast {
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

                project.getRootProject().project("app").android.applicationVariants.all { variant ->  //3.2
                    variant.outputs.all { output ->
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


//            修改manifest
//            project.getRootProject().project("app").android.applicationVariants.all { variant ->  //3.2
//                variant.outputs.all { output ->
//                    output.processManifest.doLast {
//                        // Stores the path to the maifest.
//                        // Stores the contents of the manifest.
//                        String manifestContent = manifestOutputDirectory.file("AndroidManifest.xml").get().getAsFile().getText()
//                        // Changes the version code in the stored text.
//                        println(manifestContent)
//
//                        def manifestMap = ['</manifest>'   : " <permission\n" +
//                                "        android:name=\"${packageName}.permission.JPUSH_MESSAGE\"\n" +
//                                "        android:protectionLevel=\"signature\" />\n" +
//                                "    <uses-permission android:name=\"${packageName}.permission.JPUSH_MESSAGE\" />" +
//                                "<uses-permission android:name=\"android.permission.RECEIVE_USER_PRESENT\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.INTERNET\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.READ_PHONE_STATE\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.READ_EXTERNAL_STORAGE\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.MOUNT_UNMOUNT_FILESYSTEMS\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.ACCESS_WIFI_STATE\" />\n" +
//                                "\n" +
//                                "    <!-- Optional. Required for location feature -->\n" +
//                                "    <uses-permission android:name=\"android.permission.ACCESS_BACKGROUND_LOCATION\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.SYSTEM_ALERT_WINDOW\" /> <!-- 用于开启 debug 版本的应用在 6.0 系统上的层叠窗口权限 -->\n" +
//                                "    <uses-permission android:name=\"android.permission.ACCESS_COARSE_LOCATION\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.CHANGE_WIFI_STATE\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.ACCESS_FINE_LOCATION\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.ACCESS_LOCATION_EXTRA_COMMANDS\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.CHANGE_NETWORK_STATE\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.GET_TASKS\" />\n" +
//                                "    <uses-permission android:name=\"android.permission.VIBRATE\" />" +
//                                "</manifest>",
//                                           '</application>': " <service\n" +
//                                                   "            android:name=\"cn.jpush.android.service.PushService\"\n" +
//                                                   "            android:enabled=\"true\"\n" +
//                                                   "            android:exported=\"false\">\n" +
//                                                   "            <intent-filter>\n" +
//                                                   "                <action android:name=\"cn.jpush.android.intent.REGISTER\" />\n" +
//                                                   "                <action android:name=\"cn.jpush.android.intent.REPORT\" />\n" +
//                                                   "                <action android:name=\"cn.jpush.android.intent.PushService\" />\n" +
//                                                   "                <action android:name=\"cn.jpush.android.intent.PUSH_TIME\" />\n" +
//                                                   "            </intent-filter>\n" +
//                                                   "        </service>\n" +
//                                                   "\n" +
//                                                   "        <!-- since 3.0.9 Required SDK 核心功能-->\n" +
//                                                   "        <provider\n" +
//                                                   "            android:name=\"cn.jpush.android.service.DataProvider\"\n" +
//                                                   "            android:authorities=\"${project.pluginExt.providerAuthorities}\"\n" +
//                                                   "            android:exported=\"true\" />\n" +
//                                                   "\n" +
//                                                   "        <!-- since 1.8.0 option 可选项。用于同一设备中不同应用的JPush服务相互拉起的功能。 -->\n" +
//                                                   "        <!-- 若不启用该功能可删除该组件，将不拉起其他应用也不能被其他应用拉起 -->\n" +
//                                                   "        <service\n" +
//                                                   "            android:name=\"cn.jpush.android.service.DaemonService\"\n" +
//                                                   "            android:enabled=\"true\"\n" +
//                                                   "            android:exported=\"true\">\n" +
//                                                   "            <intent-filter>\n" +
//                                                   "                <action android:name=\"cn.jpush.android.intent.DaemonService\" />\n" +
//                                                   "                <category android:name=\"${packageName}\" />\n" +
//                                                   "            </intent-filter>\n" +
//                                                   "        </service>\n" +
//                                                   "\n" +
//                                                   "        <!-- since 3.1.0 Required SDK 核心功能-->\n" +
//                                                   "        <provider\n" +
//                                                   "            android:name=\"cn.jpush.android.service.DownloadProvider\"\n" +
//                                                   "            android:authorities=\"${project.pluginExt.downLoadAuthorities}\"\n" +
//                                                   "            android:exported=\"true\" />\n" +
//                                                   "\n" +
//                                                   "        <!-- Required SDK核心功能-->\n" +
//                                                   "        <receiver\n" +
//                                                   "            android:name=\"cn.jpush.android.service.PushReceiver\"\n" +
//                                                   "            android:enabled=\"true\">\n" +
//                                                   "            <intent-filter android:priority=\"1000\">\n" +
//                                                   "                <action android:name=\"cn.jpush.android.intent.NOTIFICATION_RECEIVED_PROXY\" />\n" +
//                                                   "                <category android:name=\"${packageName}\" />\n" +
//                                                   "            </intent-filter>\n" +
//                                                   "            <intent-filter>\n" +
//                                                   "                <action android:name=\"android.intent.action.USER_PRESENT\" />\n" +
//                                                   "                <action android:name=\"android.net.conn.CONNECTIVITY_CHANGE\" />\n" +
//                                                   "            </intent-filter>\n" +
//                                                   "            <!-- Optional -->\n" +
//                                                   "            <intent-filter>\n" +
//                                                   "                <action android:name=\"android.intent.action.PACKAGE_ADDED\" />\n" +
//                                                   "                <action android:name=\"android.intent.action.PACKAGE_REMOVED\" />\n" +
//                                                   "\n" +
//                                                   "                <data android:scheme=\"package\" />\n" +
//                                                   "            </intent-filter>\n" +
//                                                   "        </receiver>\n" +
//                                                   "\n" +
//                                                   "        <!-- Required SDK核心功能-->\n" +
//                                                   "        <activity\n" +
//                                                   "            android:name=\"cn.jpush.android.ui.PushActivity\"\n" +
//                                                   "            android:configChanges=\"orientation|keyboardHidden\"\n" +
//                                                   "            android:exported=\"false\"\n" +
//                                                   "            android:theme=\"@android:style/Theme.NoTitleBar\">\n" +
//                                                   "            <intent-filter>\n" +
//                                                   "                <action android:name=\"cn.jpush.android.ui.PushActivity\" />\n" +
//                                                   "\n" +
//                                                   "                <category android:name=\"android.intent.category.DEFAULT\" />\n" +
//                                                   "                <category android:name=\"${packageName}\" />\n" +
//                                                   "            </intent-filter>\n" +
//                                                   "        </activity>\n" +
//                                                   "        <!-- SDK核心功能-->\n" +
//                                                   "        <activity\n" +
//                                                   "            android:name=\"cn.jpush.android.ui.PopWinActivity\"\n" +
//                                                   "            android:configChanges=\"orientation|keyboardHidden\"\n" +
//                                                   "            android:exported=\"false\"\n" +
//                                                   "            android:theme=\"@style/MyDialogStyle\">\n" +
//                                                   "            <intent-filter>\n" +
//                                                   "                <category android:name=\"android.intent.category.DEFAULT\" />\n" +
//                                                   "                <category android:name=\"${packageName}\" />\n" +
//                                                   "            </intent-filter>\n" +
//                                                   "        </activity>\n" +
//                                                   "\n" +
//                                                   "        <!-- Required SDK核心功能-->\n" +
//                                                   "        <service\n" +
//                                                   "            android:name=\"cn.jpush.android.service.DownloadService\"\n" +
//                                                   "            android:enabled=\"true\"\n" +
//                                                   "            android:exported=\"false\"></service>\n" +
//                                                   "\n" +
//                                                   "        <!-- Required SDK核心功能-->\n" +
//                                                   "        <receiver android:name=\"cn.jpush.android.service.AlarmReceiver\" />\n" +
//                                                   "\n" +
//                                                   "        <!-- Required. For publish channel feature -->\n" +
//                                                   "        <!-- JPUSH_CHANNEL 是为了方便开发者统计APK分发渠道。-->\n" +
//                                                   "        <!-- 例如: -->\n" +
//                                                   "        <!-- 发到 Google Play 的APK可以设置为 google-play; -->\n" +
//                                                   "        <!-- 发到其他市场的 APK 可以设置为 xxx-market。 -->\n" +
//                                                   "        <meta-data\n" +
//                                                   "            android:name=\"JPUSH_CHANNEL\"\n" +
//                                                   "            android:value=\"developer-default\" />\n" +
//                                                   "        <!-- Required. AppKey copied from Portal -->\n" +
//                                                   "        <meta-data\n" +
//                                                   "            android:name=\"JPUSH_APPKEY\"\n" +
//                                                   "            android:value=\"${project.pluginExt.jPushKey}\" />\n" +
//                                                   "\n" +
//                                                   "        <!-- Required SDK核心功能-->\n" +
//                                                   "        <activity\n" +
//                                                   "            android:name=\"cn.jiguang.share.android.ui.JiguangShellActivity\"\n" +
//                                                   "            android:exported=\"true\"\n" +
//                                                   "            android:launchMode=\"singleTask\"\n" +
//                                                   "            android:theme=\"@android:style/Theme.Translucent.NoTitleBar\"\n" +
//                                                   "            android:windowSoftInputMode=\"stateHidden|adjustResize\">\n" +
//                                                   "            <!-- Optional QQ分享回调-->\n" +
//                                                   "            <!-- scheme为“tencent”前缀再加上QQ开发者应用的appID；例如appID为123456，则scheme＝“tencent123456” -->\n" +
//                                                   "            <intent-filter>\n" +
//                                                   "                <data android:scheme=\"tencent${project.pluginExt.JGqqShareKey}\" />\n" +
//                                                   "                <action android:name=\"android.intent.action.VIEW\" />\n" +
//                                                   "\n" +
//                                                   "                <category android:name=\"android.intent.category.BROWSABLE\" />\n" +
//                                                   "                <category android:name=\"android.intent.category.DEFAULT\" />\n" +
//                                                   "            </intent-filter>\n" +
//                                                   "\n" +
//                                                   "            <!-- Optional 新浪微博分享回调 -->\n" +
//                                                   "            <intent-filter>\n" +
//                                                   "                <action android:name=\"com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY\" />\n" +
//                                                   "                <category android:name=\"android.intent.category.DEFAULT\" />\n" +
//                                                   "            </intent-filter>\n" +
//                                                   "\n" +
//                                                   "            <!-- Optional 新浪微博私信回调-->\n" +
//                                                   "            <intent-filter>\n" +
//                                                   "                <action android:name=\"android.intent.action.VIEW\" />\n" +
//                                                   "\n" +
//                                                   "                <category android:name=\"android.intent.category.DEFAULT\" />\n" +
//                                                   "                <category android:name=\"android.intent.category.BROWSABLE\" />\n" +
//                                                   "\n" +
//                                                   "                <data\n" +
//                                                   "                    android:host=\"sinaweibo\"\n" +
//                                                   "                    android:scheme=\"jsharesdk\" />\n" +
//                                                   "            </intent-filter>\n" +
//                                                   "        </activity>\n" +
//                                                   "\n" +
//                                                   "        <!-- Optional 微信分享回调,wxapi必须在包名路径下，否则回调不成功-->\n" +
//                                                   "        <activity\n" +
//                                                   "            android:name=\"${packageName}.wxapi.WXEntryActivity\"\n" +
//                                                   "            android:exported=\"true\"\n" +
//                                                   "            android:theme=\"@android:style/Theme.Translucent.NoTitleBar\" />\n" +
//                                                   "        <activity\n" +
//                                                   "            android:name=\"${packageName}.wxapi.WXPayEntryActivity\"\n" +
//                                                   "            android:exported=\"true\"\n" +
//                                                   "            android:theme=\"@android:style/Theme.Translucent.NoTitleBar\" />" +
//                                                   "</application>"]
////                manifest.appendNode("permission", ['android:name': "com.dsg.gradleplugindemo.permission.JPUSH_MESSAGE", 'android:protectionLevel': "signature"])
//                        String fileContent = replaceText(manifestContent, manifestMap)
//
//                        manifestOutputDirectory.file("AndroidManifest.xml").get().getAsFile().write(fileContent)
//                    }
//                }
//            }


//            增加buildconfig自定义属性
//            def manifestMap =
//                    "{\"JPUSH_PKGNAME\": \"$packageName\"," +
//                            "\"JPUSH_APPKEY\" : \"${project.pluginExt.jPushKey}\"" +
//                            "\"JPUSH_CHANNEL\": \"${project.pluginExt.JGPushChannel}\"}"
//
//
//            Map<String, String> manifestMap = new HashMap<>();
//            manifestMap.put("JPUSH_PKGNAME", packageName)
//            manifestMap.put("JPUSH_APPKEY", project.pluginExt.jPushKey)
//            manifestMap.put("JPUSH_CHANNEL", project.pluginExt.JGPushChannel)
//
//
//            println("333")
//
//            def android = project.extensions.findByName("android")
//            println("444")
//
//            if (android != null) {
//                def defaultConfig = android["defaultConfig"]
//                println("111")
//
//                defaultConfig.buildConfigField("java.util.Map<String, String>", "manifestPlaceholders", "new java.util.HashMap<String, " +
//                        "String>() { put(\"JPUSH_PKGNAME\", \"John1\"); put(\"JPUSH_APPKEY\",  \"John2\"); put(\"JPUSH_CHANNEL\", " +
//                        "\"John3\"); }")
//
//            }
//            println("222")


//            ###########################添加极光service
//          manifest中添加推送service
