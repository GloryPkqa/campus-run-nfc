# 制作自己学校的版本

可以为自己的学校修改名称、校名、图标、文案、配色和默认地址，重新构建 APK 发布。请保留原作者 **@Pkqa** 及点击前往本项目的链接；也可以添加自己的维护者名字。

## 1. 获取源码

Fork 本项目，或下载源码压缩包。先按照[构建教程](build.md)运行一次，确认环境能够生成 APK。

## 2. 修改软件名和校名

打开 `src/com/pkqa/campusrunnfc/MainActivity.java`：

- 顶部的 `校园跑 NFC 助手`：改为你想显示的 App 标题。
- 主卡片顶部的 `自定义 NFC 链接`：可以改为学校全称。
- `阳光校园跑`、`NFC启动工具`、`少些等待，多些运动`：可以按学校使用场景调整。

打开 `AndroidManifest.xml`，把 `android:label="校园跑 NFC 助手"` 改为新名称。这决定桌面上的 App 名称，仅改 Java 文案不会改变桌面名称。

## 3. 设置默认链接（可选）

找到 `DEFAULT_URL`。公开通用版的值是空字符串，因此首次使用由用户自己填写。

如果你已经取得学校明确提供给你使用、适合在该版本中分发的启动 URI，可以将它设为默认值。请将整条 URI 原样放进字符串，不要删掉编码或参数。公开分享时不要把个人账号、临时票据或不适合公开的链接写入源码。

Java 字符串中的双引号和反斜杠需要转义。App 仍会优先使用用户在设置中保存的地址；修改默认值不会覆盖已经保存的配置。

## 4. 修改图标和配色

- 图标：`res/drawable/app_icon.xml`，目前是蓝绿跑步矢量图标。
- 颜色：`MainActivity.java` 顶部的 `INK`、`MUTED`、`TEAL` 及 `RunningArt` 绘制代码。
- 使用学校标志前，先确认你有使用该素材的权限。也可以继续用本项目自带的跑步图案。

## 5. 使用自己的包名

当前包名是 `com.pkqa.campusrunnfc`。为便于与原版共存，学校版本建议使用自己的唯一包名，例如 `com.example.schoolrun`（请替换为自己的值）：

1. 修改 `AndroidManifest.xml` 的 `package`。
2. 修改 `MainActivity.java` 和 `LinkValidator.java` 第一行的 `package`，将文件放到对应目录。
3. 修改 `tests/LinkValidatorTest.java` 中的 import。
4. 构建脚本会自动查找源码，不需要写死新的 Java 文件路径。

不要修改代码中的 `com.eg.android.AlipayGphone`，它是目标支付宝 App 的包名，不是你的 App 包名。

## 6. 保留作者署名

`MainActivity.java` 中的 `credit` 文本是 `@Pkqa`，点击目标是：

```text
https://github.com/GloryPkqa/campus-run-nfc
```

请保留这条可见、可点击的署名，可以另加学校维护者的署名。发布源码时同时保留 `LICENSE` 和 `NOTICE`。

## 7. 构建并发布

1. 按[构建教程](build.md)生成 APK，使用你自己长期保存的签名密钥。
2. 修改 Manifest 中的 `versionCode` / `versionName`。后续更新时 `versionCode` 必须增大，同包名更新需使用同一签名密钥。
3. 新建 GitHub Release，上传构建好的 APK 和 SHA-256 校验值。
4. 发布时简要说明适用学校和维护者，并注明原作者 `@Pkqa` 及[原项目链接](https://github.com/GloryPkqa/campus-run-nfc)即可。
