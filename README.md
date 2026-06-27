# ikanbot-tv - 爱看播 Android TV 客户端

## 项目简介
将 ikanbot.com 视频聚合网站打包为 Android TV 应用的 WebView 客户端。

## 架构设计
- 首页使用 WebView 直接加载 ikanbot.com
- 视频播放链接自动检测并跳转系统浏览器（绕过安全验证）
- 注入反检测脚本，提升 WebView 兼容性
- 完全支持 Android TV 遥控器导航

## 安全验证处理策略
ikanbot.com 的视频播放页面触发了安全验证机制。
本项目采用"首页 WebView + 视频跳转浏览器"的混合方案：
1. 首页浏览使用 WebView 直接加载
2. 检测到视频播放链接时，自动跳转到 TV 系统浏览器
3. 用户在系统浏览器中完成验证后观看视频

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