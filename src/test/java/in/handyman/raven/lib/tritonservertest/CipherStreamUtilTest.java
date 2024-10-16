package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CipherStreamUtil;
import org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;


@Slf4j
public class CipherStreamUtilTest {




    @Test
    public void encryptionTest() throws Exception {
        ActionExecutionAudit audit = new ActionExecutionAudit();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "John Doe");



        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("publicKey", "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqoRdEwW3O51eTaupz8fks4PGhp1fmRHmsjijUarx0hlzRjSZm77IBbh93vlWhWnXV/CobpO/dIq0rrrDJ/CAncDCB7aQivMlplHy5+2hlZuENyNjevOXOrTxDlpnfXmumWCA6veqmcyQsASZmNMM+NVt91SirgEQt3EeTycegFN+nWl/2yQGEoqIg+2/WJ7dK+xr7rif7zRbbcBYnl+XdUrqR/4z46Q/prjiREbJCqYkrqsr/T530aS7ROEvQeK1gLbLqiIyxLLCcGMyA9nnk3RA8ADmkKm6Trx23t+6MgMct5Kde8IpoAgX8A4Iu9vQS+oB33BADEaKKtH9YVILTwIDAQAB-----END PUBLIC KEY-----"),
                Map.entry("apiUrl", "http://0.0.0.0:10001/copro-utils/data-security/encrypt"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "1"),
                Map.entry("encryption.activator","true")));


        String cipherStreamUtil = CipherStreamUtil.encryptionApi(jsonObject, actionExecutionAudit);
        System.out.println(cipherStreamUtil);
    }

}

