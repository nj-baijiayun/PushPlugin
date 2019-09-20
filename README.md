使用插件库可以简单的就将极光或者友盟的推送分享和统计集成进来,避免写入一些重复无意义的代码.加快开发速度

支付库集成请 <a href = "#pay">点击这里</a>

## 分享推送库快速集成
##### 1.1 在工程目标的`build.gradle`文件添加仓库地址
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
##### 1.2 在工程目录`builg.gradle`下的`dependencies`下添加插件依赖
```
//当前最新版本为1.1.0
 classpath 'com.nj.baijiayun:pushplugin:1.1.0'
```
##### 1.3 在app目录`build.gradle`下添加push库依赖
```
    //当前最新版本1.0.2
    api 'com.nj.baijiayun:push:1.0.2'
    //如果遇到和支付宝支付sdk冲突,加入以下代码
    //        exclude module: 'utdid'
    implementation 'com.nj.baijiayun:pushCompiler:1.0.0'
    annotationProcessor 'com.nj.baijiayun:pushCompiler:1.0.0'
```

##### 1.4 在app的`build.gradle`下添加配置推送配置信息
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
##### 1.5 Application 文件中初始化
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
##### 1.6 给Application添加注解用于自动生成WXEntryActivity
**(可选)** 如果包名和appId不一致 可以使用 @GenerateEntry(appid = "appId") 来设置appId
```
@GenerateEntry
public class BJYAPP extends Application {
    ...
}

```

##### 1.7 混淆配置

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

-dontwarn com.baijiayun.lib_compiler.**
-keep class com.baijiayun.lib_compiler.**{
    *;
}
```


# <a name = "pay">payUtils</a>

#### 项目介绍
payUtils是一个两行代码实现微信支付或支付宝支付的支付库。 应对快速开发利器，让开发变得更加简单


#### 使用说明

1. 在项目module下的gradle中添加以下依赖：

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
            url = 'http://172.20.2.114:8081/repository/maven-snapshots/'
        }
    }
}
```

2.在dependencies下添加依赖
```
 dependencies {
 
    //当前最新版本为1.0.0
    implementation 'com.nj.baijiayun:payUtils:1.0.0'
}
```

### 使用介绍 
  
Java使用 调用微信支付

```
 WxPayConfig wxPayConfig=new WxPayConfig.Builder()
 
                .with(MainActivity.this)
                .setAppId(应用ID)
                .setNoncestr(随机字符串)
                .setPackagex(扩展字段 暂填写固定值Sign=WXPay)
                .setPartnerid(商户ID)
                .setPrepayid(预支付交易会话ID)
                .setSign(签名)
                .setTimestamp(时间戳)
                .builder();
        WxPayManager.getInstance().sendPay(wxPayConfig);
```
     
微信支付回调需要在你的主App中 写一个WXPayEntryActivity并在清单文件中注册，并用广播形式或事件总线程（RXBUS、Eventbus）传送成功或失败
```
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI api;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxpay_entry);
        api = WXAPIFactory.createWXAPI(this, "AppId");
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
        Message message=new Message();
        message.what=1;
        message.obj=resp.errCode;
        handler.sendMessage(message);
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    int errcord= (int) msg.obj;
                    switch (errcord){
                        case -1:
                           //支付失败
                            break;
                        case 0:
                          //支付成功
                            break;
                        case -2:
                           //用户取消支付
                            break;
                        default:
                           //支付失败
                            break;
                    }

                    break;
            }

        }
    };
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
            return false;
        } else if(keyCode == KeyEvent.KEYCODE_MENU) {
            // rl.setVisibility(View.VISIBLE);
            return false;
        } else if(keyCode == KeyEvent.KEYCODE_HOME) {
            //由于Home键为系统键，此处不能捕获，需要重写onAttachedToWindow()
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
```

调用支付宝
在说调用调用支付宝使用时需要了解，调起支付宝参数有两种形式一种是全部由服务器生成，客户端只需要用服务器生成的参数调起支付，不需要做任务签名生成，这样做的目的是安全所有推荐这种方式，另外一种就是服务器只返回订单信息，客户端需要配置调起支付宝的参数处理，例如签名之类的，先说第一种参数交由服务器来配置的
参数由服务器生成使用方法

```
AliPayConfig aliPayConfig=new AliPayConfig.Builder()
               
                .with(上下文)
                .setSignedOrder(服务器返回配置参数)
                .setmCall(new AliPayStatusCall() {
                    @Override
                    public void getPayAliPayStatus(String msg, boolean isPaySuccess) {
                        //isPaySuccess返回true 表示支付成功
                    }
                }).builder();
 AliPayManager.getInstance().sendPay(aliPayConfig);
 ```
 
 服务器只返回订单信息，具体交由客户端来生成参数
```
  AliPayUnSignOrderConfig aliPayUnSignOrderConfig=new AliPayUnSignOrderConfig.Bulider()
                
                .with(上下文)
                .setAPPID(应用Id)
                .setBody(商品详情)
                .setCallbackUrl(异步通知地址)
                .setmCall(回调)
                .setOutTradeNo(订单号)
                .setPartner(签约合作者身份ID)
                .setPrice(价格)
                .setRSA2_PRIVATE(私钥)
                .setSubject(商品名称)
                .setTimestamp(时间戳)
                .builder();
        AliPayManager.getInstance().sendPay(aliPayUnSignOrderConfig);
```


需要注意的一点就是私钥用的是Rsa2不是Rsa,因为在最新的sdk中RSA已经慢慢淡出了！

