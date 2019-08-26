使用插件库可以简单的就将极光或者友盟的推送分享和统计集成进来,避免写入一些重复无意义的代码.加快开发速度
## 快速集成
#### 在工程目标的`build.gradle`文件添加仓库地址
```
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            //release仓库地址
            url = 'http://172.20.2.114:8081/repository/maven-releases/'
        }
        maven {
            //snapshot仓库地址
            url = 'http://172.20.2.114:8081/repository/maven-releases/'
        }
    }
}

```
#### 在工程目录`builg.gradle`下的`dependencies`下添加插件依赖
```
//当前最新版本为1.1.0
 classpath 'com.nj.baijiayun:pushplugin:1.1.0'
```
#### 在app目录`build.gradle`下添加push库依赖
```
    //当前最新版本1.0.2
    api 'com.nj.baijiayun:push:1.0.2'
    //如果遇到和支付宝支付sdk冲突,加入以下代码
    //        exclude module: 'utdid'
    implementation 'com.nj.baijiayun:pushCompiler:1.0.0'
    annotationProcessor 'com.nj.baijiayun:pushCompiler:1.0.0'
```

#### 在app的`build.gradle`下添加配置推送配置信息
```
apply plugin: 'pushplugin'
/*
极光示例
     JPushKey = "JPushKey"    //必填
     JGQQShareKey = "JGQQShareKeyDefault"
     JGWxShareKey = "JGWxShareKeyDefault"
     JGSinaShareKey = "JGSinaShareKey"
     JGFaceBookShareKey = "JGFaceBookShareKeyDefault"
     JGQQShareSecret = "JGQQShareSecretDefault"
     JGWxShareSecret = "JGWxShareSecretDefault"
     JGSinaShareSecret = "JGSinaShareSecret"
     JGFaceBookShareSecret = "JGFaceBookShareSecretDefault"
     JGPushChannel = "developer-default"
*
* 友盟示例
        UMengKey = "UMengKeyDefault" //必填
        UMengSecret = "UMengSecret"  //必填
*
* */
pluginExt {
    packageName = "包名"
    applicationId = "applicationId"
    jiGuangExt {
        jPushKey = "极光推送key"    //必填
        JGQQShareKey = "qq第三方key"
        JGWxShareKey = "微信第三方key"
        JGQQShareSecret = "qq第三方秘钥"
        JGWxShareSecret = "微信第三方秘钥"
    }
    uMengExt {
        UMengKey = "友盟key" //必填
        UMengSecret = "友盟秘钥"  //必填
    }
}
```
### Application 文件中初始化
```
 PushHelper.getInstance().initUMengPush(this, new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String s) {
                Log.e("main1", "success");
            }

            @Override
            public void onFailure(String s, String s1) {

            }
        });

        PushHelper.getInstance().initUMengAnalytics(this);
        PushHelper.getInstance().initJGPush(this, true);
        PushHelper.getInstance().initJGShare(this, true);
        PushHelper.getInstance().initJGAnalytics(this,true);
```
#### 给Application添加注解用于自动生成WXEntryActivity
```
@GenerateEntry
public class BJYAPP extends Application {
    ...
}

```

### 混淆配置

```
#极光
-keep class cn.jiguang.** { *; }
-keep class android.support.** { *; }
-keep class androidx.** { *; }
-keep class com.google.android.** { *; }

-dontoptimize
-dontpreverify

-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.helpers.JPushMessageReceiver { *; }

-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

-dontoptimize
-dontpreverify

-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.helpers.JPushMessageReceiver { *; }

-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

#友盟
-keep class com.umeng.** {*;}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-dontwarn com.umeng.**
-dontwarn com.taobao.**
-dontwarn anet.channel.**
-dontwarn anetwork.channel.**
-dontwarn org.android.**
-dontwarn org.apache.thrift.**
-dontwarn com.xiaomi.**
-dontwarn com.huawei.**
-dontwarn com.meizu.**

-keepattributes *Annotation*

-keep class com.taobao.** {*;}
-keep class org.android.** {*;}
-keep class anet.channel.** {*;}
-keep class com.umeng.** {*;}
-keep class com.xiaomi.** {*;}
-keep class com.huawei.** {*;}
-keep class com.meizu.** {*;}
-keep class org.apache.thrift.** {*;}

-keep class com.alibaba.sdk.android.**{*;}
-keep class com.ut.**{*;}
-keep class com.ta.**{*;}

-keep public class **.R$*{
   public static final int *;
}
```
