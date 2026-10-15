package com.pkqa.campusrunnfc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** Validate the full URI without decoding, rewriting, or reordering parameters. */
public final class LinkValidator {
    private LinkValidator() {}
    public static String validate(String input) {
        if(input==null||input.trim().isEmpty())throw new IllegalArgumentException("请先设置完整的 NFC 地址");
        String value=input.trim();
        try {
            URI uri=new URI(value);
            String scheme=uri.getScheme()==null?"":uri.getScheme().toLowerCase(Locale.ROOT);
            if(!scheme.equals("https")&&!scheme.equals("http")&&!scheme.equals("alipay")&&!scheme.equals("alipays"))
                throw new IllegalArgumentException("仅支持 HTTP、HTTPS、alipay 或 alipays 地址");
            if(uri.getHost()==null||uri.getHost().isEmpty())throw new IllegalArgumentException("请输入带有完整域名或入口的地址");
            return value;
        } catch(URISyntaxException error) {
            throw new IllegalArgumentException("地址格式不完整，请检查空格、换行和百分号编码");
        }
    }
}
