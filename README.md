使用插件库可以简单的就将极光或者友盟的推送分享和统计集成进来,避免写入一些重复无意义的代码.加快开发速度
## 快速集成
在工程目标的`build.gradle`文件添加仓库地址
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
在工程目录`builg.gradle`下的`dependencies`下添加插件依赖
```
//当前最新版本为13
 classpath 'com.nj.baijiayun:pushplugin:1.0.13'
```
在app目录`build.gradle`下添加push库依赖
```
    //当前最新版本1.0.1
    api 'com.nj.baijiayun:push:1.0.1'
```

在app的`build.gradle`下添加配置推送配置信息
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
