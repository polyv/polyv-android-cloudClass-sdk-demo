polyv-android-cloudClass-sdk-demo
===

[![build passing](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)
[![GitHub release](https://img.shields.io/badge/release-v0.6.2-blue.svg)](https://github.com/polyv/polyv-android-cloudClass-sdk-demo/releases/tag/v0.6.2)
#### polyvSDKCloudClass （以下称SDK）是什么？

SDK是Polyv为开发者用户提供的云课堂观看端SDK ，是jar文件和so文件。易于集成，内部包含`登录` `视频直播`  `视频回放`   `聊天`  `连麦` `ppt播放`等功能。首先需要在[链接到官网](http://www.polyv.net)注册账户并开通点播功能，然后集成SDK到你的项目中。
#### polyv-android-cloudClass-sdk-demo（以下称**SDKdemo**）是什么？
SDKdemo是SDK的demo示例Android studio项目工程，其中包含了最新SDK并且演示了如何在项目中集成SDK。
***
#### 运行环境
* JDK 1.7 或以上
* Android SDK 16或以上
* Android Studio 3.0.0 或以上
***
#### 支持功能

### 登录

- #### 直播登录

- #### 回放登录

### 直播

- #### 普通功能
  - 播放，刷新
  - 视频弹幕
  - 视频码率切换
  - 横竖屏切换
  - 副窗口可随意拖动

- #### 暖场播放（无直播时播放）

- #### 手势滑动
   - 屏幕播放区域右方是音量调节区域
   - 播放区域左方是亮度调节区域

- #### ppt，教师同时在线播放
  - 教师端，ppt显示切换
  - ppt的动态画笔功能
  - ppt、教师端的同步播放

### 连麦
- #### 基本功能
  - 举手连麦
  - 取消举手
  - 断开连麦
  - 横竖屏切换
  - 本地前后摄像头切换

- #### 多人连麦
  - 同时6人在线连麦
  - 连麦者可以被教师关闭摄像头或者是麦克风

- #### 连麦的主副屏切换

### 回放
- #### 基本功能
  - 播放直播的缓存视频
  - 暂停播放
  - 拖动滑动条，可以seek进度播放
  - 视频码率切换
  - 播放倍速切换
  - 横竖屏切换

- #### 手势滑动
  - 屏幕播放区域右方是音量调节区域
  - 播放区域左方是亮度调节区域
  - 水平左右滑动seek进度播放

- #### ppt播放
  - 教师端，ppt显示切换
  - ppt、教师端的同步回放
  - ppt与视频播放的快进快退的同步
  - ppt与视频倍速的同步播放

### 聊天
- #### 基本功能
  - 设置用户昵称
  - 发言
  - 送花
  - 支持文字和Emoji静态表情的键盘，和文本混排
  - 接收并显示其他端发送的图片(支持gif动图)
  - 支持只看讲师聊天信息
  - 跑马灯公告
  - 欢迎语
  - 回看历史聊天记录
  - 清空聊天信息
  - 删除某条聊天信息
  - 禁言
  - 踢人
  - 私聊
  - 未读消息提醒

### 答题
- #### 基本功能
  - 竖屏时，在屏幕中间显示教师客户端发送的题目信息（单选题，多选题）
  - 可作答，并提交答案
  - 显示作答结果
  



***
#### 更多关于SDKdemo和SDK的详细介绍请看[Wiki](https://github.com/polyv/polyv-android-cloudClass-sdk-demo/wiki)。

0.2.0版API文档请看[v0.2.0 API](http://repo.polyv.net/android/cloudclass/javadoc/0.2.0/index.html)

0.3.0版API文档请看[v0.3.0 API](http://repo.polyv.net/android/cloudclass/javadoc/0.3.0/index.html)

0.4.0版API文档请看[v0.4.0 API](http://repo.polyv.net/android/cloudclass/javadoc/0.4.0/index.html)

0.5.0版API文档请看[v0.5.0 API](http://repo.polyv.net/android/cloudclass/javadoc/0.5.0/index.html)

0.6.0版API文档请看[v0.6.0 API](http://repo.polyv.net/android/cloudclass/javadoc/0.6.0/index.html)

0.6.2版API文档请看[v0.6.2 API](http://repo.polyv.net/android/cloudclass/javadoc/0.6.2/index.html)