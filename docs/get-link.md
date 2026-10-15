# 获取你的 NFC 启动链接

本教程用于读取你自己使用的标签中的网址记录。需要支持 NFC 的手机和原始标签；手里只有卡片 UID 或截图，不一定能还原完整链接。

## 方法一：NFC Tools

1. 安装开发者为 **wakdev** 的 [NFC Tools](https://www.wakdev.com/en/apps/nfc-tools-android.html)。
2. 开启手机 NFC，打开 NFC Tools 的“读 / Read”页面。
3. 把标签靠近手机感应区域，保持到读取完成。
4. 向下找到 NDEF 的网址记录，通常显示为“记录 1 - https://”“URL”或“URI”。
5. 打开这条记录的详情或右侧三点菜单，使用复制内容/复制地址功能。不同版本菜单名称可能不同；需要的是**记录里的完整网址**。
6. 打开“校园跑 NFC 助手 → 启动地址 → 修改”，粘贴并保存，再点击启动。

NFC Tools 官方说明确认“读”页可以显示 NDEF 记录。复制后请在编辑框里确认，网址包含开头的 `https://`，没有把“记录 1”或其他界面文字一起带入。

## 方法二：NXP TagInfo

1. 从 [NXP NFC 应用资源页](https://www.nxp.com/design/design-center/software/rfid-developer-resources:NFC-APP-DEVELOPER-RESOURCE-HUB)找到 **NFC TagInfo by NXP**。
2. 开启 NFC，打开 TagInfo，读取原标签。
3. 找到“NDEF 数据 / NDEF messages”中的“URI record”。
4. 复制或导出该记录展示的完整 URI，再粘贴到本 App。

有些版本会同时显示十六进制载荷。优先使用软件已经解析出来的 **URI 文本**，不要把 `0x04 0x72 …` 直接粘贴到地址框。

## 应该复制哪一部分

应复制：NDEF **URI/URL 记录**里的整条地址，保留大小写和所有参数。

不应复制：

- 卡片“序列号 / UID”，例如一串用冒号分隔的十六进制数字。
- ATQA、SAK、容量或“可写”等标签属性。
- `com.eg.android.AlipayGphone` 这条应用记录；它是支付宝包名，不是启动链接。
- 网址中间的一段 `scheme=`、`t=` 或 `p=` 参数。

如果网址以 `https://render.alipay.com/` 开头，后面还有 `%3A`、`%2F`、`%26` 等编码，**按读到的原样完整复制**。本 App 不需要先做 URL 解码，不会把 `+`、`%2B` 或大小写改掉。

## 常见问题

**只有 UID，没有网址记录。** 标签可能没有 NDEF URI，或记录未被当前工具识别。换 TagInfo 检查；没有 URI 时，本 App 无法从 UID 推导出业务链接。

**原卡可用，保存后打开失败。** 先核对是否漏了 `https://`、末尾参数或把屏幕换行带进了地址；也可能是当前业务需要真实 NFC 会话、动态数据或其他条件，保存一条静态 URI 未必足够。

**只能打开页面，不能开始。** 这仍取决于目标业务的检查。App 显示“已发送”只说明启动请求已提交。

**能不能用网页二维码代替？** 只有二维码本身编码的就是同一条启动 URI 时才可能等价。不能因为页面看起来相同，就认定二维码地址与 NFC 地址相同。

## 发布自己的版本时

你可以在自己手机里保存完整地址。向公开仓库提交截图、示例或问题时，遮盖其中可能与学校、点位或个人业务相关的参数。公开版源码没有收录任何真实启动地址。

参考：[NFC Tools 官方说明](https://www.wakdev.com/en/apps/nfc-tools-android.html)、[NXP NFC 应用资源](https://www.nxp.com/design/design-center/software/rfid-developer-resources:NFC-APP-DEVELOPER-RESOURCE-HUB)。
