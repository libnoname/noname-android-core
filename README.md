noname安卓版本核心库

# 项目说明
该项目仅供学习参考，请勿用于非法用途。

本项目中使用[WebViewUpgrade](https://github.com/JonaNorman/WebViewUpgrade)项目的代码升级Webview内核

将noname安卓版本的公共Api和升级Webview内核操作封装到此模块中，使所有noname App可以共用相同功能

为了防止倒卖，加入了签名验证功能

# 使用说明
创建Cordova项目后，点击Android Studio菜单栏的 File -> New -> Import Module

选择项目根目录，模块名称可以自己起，但一般用:NonameCore即可

然后在自己的Cordova安卓项目的MainActivity继承com.noname.core.activity.MainActivity

# Api说明
1. 显示一个持续几秒的文字提示
```ts
window.NonameAndroidBridge.showToast(message: string)
```

2.分享apk目录下的一个文件
```ts
var bool: boolean = window.NonameAndroidBridge.shareFile(documentFile: string)
```

函数返回值为一个布尔值，代表是否能正常触发分享功能 
 
例如，我觉得周公瑾的原画好看，我可以使用window.NonameAndroidBridge.shareFile('image/character/zhouyu.jpg')来把图片分享给QQ好友。 

3.异步压缩并分享本地已有的一个扩展 
```ts
window.NonameAndroidBridge.shareExtensionAsync
(extName: string)
```

4.异步压缩并分享本地已有的一个扩展(设置密码)
```ts
window.NonameAndroidBridge.shareExtensionWithPassWordAsync
(extName: string, pwd: string)
```

5.旧版App升级到http协议时，需要调用此方法
```ts
var url: string = window.NonameAndroidBridge.sendUpdate()
```

6.获取当前App的包名
```ts
var packageName: string = window.NonameAndroidBridge.getPackageName()
```

7.截屏并保存到DCIM/应用包名文件夹内
```ts
var bool: boolean = window.NonameAndroidBridge.captureScreen(fileName: string)
```

函数返回值为一个布尔值，代表是否能正常触发截屏功能 

8.切换App内使用的Webview实现
```ts
window.NonameAndroidBridge.changeWebviewProvider()
```