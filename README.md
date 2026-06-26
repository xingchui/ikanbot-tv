# ikanbot-tv - 爱看播 Android TV 客户端

## 项目简介
将 ikanbot.com 视频聚合网站打包为 Android TV 应用的 WebView 客户端。

## 架构设计
- 首页使用 WebView 直接加载 ikanbot.com
- 视频播放链接通过三层拦截机制检测并自动跳转至 ExoPlayer 原生播放（绕过安全验证）
- 验证/拦截页面仍通过系统浏览器打开（回退方案）
- 注入反检测脚本，提升 WebView 兼容性
- 完全支持 Android TV 遥控器导航

## 安全验证处理策略
ikanbot.com 的视频播放页面触发了安全验证机制。
本项目采用"首页 WebView + 视频 ExoPlayer 原生播放"的混合方案：
1. 首页浏览使用 WebView 直接加载
2. 检测到视频播放链接时（通过 shouldOverrideUrlLoading + JS 注入），自动启动 ExoPlayer 原生播放
3. 对于验证码/人机验证页面，跳转到系统浏览器完成验证
4. 侦测到动态创建的 `<iframe>` 或 `<video>` 元素时，通过 JavaScript 桥接拦截并转发至 ExoPlayer

## 构建说明

### 前置条件
1. JDK 17+
2. Android SDK (API 34)
3. Android SDK Build-Tools 34.x
4. Gradle 8.7+

### 构建命令
```bash
cd ikanbot-tv
./gradlew wrapper              # 首次生成 wrapper
./gradlew assembleDebug        # 构建 Debug APK
./gradlew assembleRelease      # 构建 Release APK (需签名)
```

### 安装到 TV
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```