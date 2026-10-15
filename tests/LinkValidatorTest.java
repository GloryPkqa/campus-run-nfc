import com.pkqa.campusrunnfc.LinkValidator;

public final class LinkValidatorTest {
    private static void accepted(String input,String expected){String actual=LinkValidator.validate(input);if(!actual.equals(expected))throw new AssertionError("URI was modified");}
    private static void rejected(String input){try{LinkValidator.validate(input);throw new AssertionError("Invalid URI accepted");}catch(IllegalArgumentException expected){}}
    public static void main(String[] args){
        String raw="https://example.com/nfc?p=a+b%2F%3D&scheme=alipay%3A%2F%2Fnfc%2Fapp";
        accepted(raw,raw);accepted("  "+raw+"  ",raw);
        accepted("alipay://nfc/app?id=123&p=a+b=","alipay://nfc/app?id=123&p=a+b=");
        accepted("alipays://platformapi/startapp?appId=123","alipays://platformapi/startapp?appId=123");
        rejected("");rejected(null);rejected("javascript:alert(1)");rejected("https:///missing-host");rejected("https://example.com/a b");rejected("https://example.com/?p=%2");rejected("https://example.com/\nsecret");
        System.out.println("PASS: URI encoding and case preserved; empty, unsupported and malformed input rejected.");
    }
}
