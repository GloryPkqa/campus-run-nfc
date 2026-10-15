# 从源码构建

项目是原生 Java Android App，没有第三方运行时依赖，也不需要 Android Studio 或 Gradle。构建脚本负责 Java 编译、DEX 转换、资源打包、APK 对齐和签名验证。

## 环境

- JDK 17 或更新版本。
- Android SDK：`platforms;android-35`、`build-tools;35.0.0`。
- Windows 使用 PowerShell；Linux 使用 Bash。

可以通过 Android Studio 的 SDK Manager 安装 SDK 组件，或通过 Android command-line tools 的 `sdkmanager` 安装：

```sh
sdkmanager 'platforms;android-35' 'build-tools;35.0.0'
```

## Windows

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-17'
./build.ps1 -AndroidSdk 'C:\path\to\Android\Sdk'
```

若已设置 `ANDROID_SDK_ROOT` 或 `ANDROID_HOME`，可以省略 `-AndroidSdk`。

## Linux

```sh
export JAVA_HOME=/path/to/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_SDK_ROOT=/path/to/android-sdk
bash build.sh
```

输出文件为 `build/campus-run-nfc.apk`。脚本会运行链接校验测试，并检查 APK 签名。

## 签名与正式发布

默认构建会在本机 `build/` 下生成一份本地签名密钥，密码默认为 `android`，适合本地验证。发布自己的学校版本时，应使用自己保管的正式密钥与密码，后续更新保持相同密钥。

Windows 示例：

```powershell
$env:NFC_STORE_PASSWORD = '你的密钥库密码'
$env:NFC_KEY_PASSWORD = '你的私钥密码'
./build.ps1 -AndroidSdk 'C:\path\to\Android\Sdk' -KeyStorePath 'C:\private\school-run.jks' -KeyAlias app
```

Linux 示例：

```sh
export NFC_KEYSTORE=/private/school-run.jks
export NFC_KEY_ALIAS=app
export NFC_STORE_PASSWORD='你的密钥库密码'
export NFC_KEY_PASSWORD='你的私钥密码'
bash build.sh
```

不要把密钥或密码提交到仓库。`.gitignore` 已排除 `build/`、APK 和常见签名密钥文件。

## GitHub Actions

仓库的构建工作流会在 push、pull request 或手动触发时编译、运行链接测试，并上传 APK 到 Actions Artifacts。

Actions 每次使用临时生成的本地签名，不是 Releases 正式版的签名；它主要用于验证源码构建。不要把 Actions 产物当作正式版的可覆盖更新。正式下载入口为 [Releases](https://github.com/GloryPkqa/campus-run-nfc/releases/latest)。

## 技术信息

- 最低 Android API：23。
- 目标 Android API：32；编译平台：35。
- 包名：`com.pkqa.campusrunnfc`。
- 唯一声明权限：`android.permission.NFC`。
- 发送 `ACTION_NDEF_DISCOVERED`，包含 URI 和支付宝应用记录。
- 不连接真实 Tag，不调用 Root，不申请网络权限。
