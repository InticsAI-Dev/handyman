package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CipherStreamUtil;
import org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


@Slf4j
public class CipherStreamUtilTest {

    @Test
    public void tritonTest() throws Exception {
        ActionExecutionAudit audit = new ActionExecutionAudit();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "John Doe");
        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxXA5Y0WmQL4hA+8oCl308ASFZGBh6moDv0b6q8RIzoqvyMGRWGPE4A5XgfRznwfMGnfGJCAdil0NFAR8bLd2mVQr6xhp1HXX4/a8t+hrMF2qCpjAe2RqeIcCwpe9tzk+ZTsIN9NaInXx9wyt46YsgC4fD0Z9+Bu7DIQONL5+zmqfaUeoBfZPn5avosqWIOwGx9uEYvuufd9r8KhyH0O/d++SzO/2XeO3MDW8pcbjiGHMRE7xna7gLHZyj8eooRpVsXZbP/anhafZYPCvfzpU8vbui01zdusmKolfEDF5ATX7cdH2naS+1E6DOcsrjxW/Ld8vDEsJuJWLaP7KlBcFiQIDAQAB-----END PUBLIC KEY-----";
        String apiUrl = "http://0.0.0.0:10001/copro-utils/aegis-cryptor/encrypt";
        String cipherStreamUtil = CipherStreamUtil.encryptionApi("jsonObject", publicKey,apiUrl);
        System.out.println(cipherStreamUtil);
    }

}
