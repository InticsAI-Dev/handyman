package in.handyman.raven.lib.tritonservertest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        String rootPipelineId = "1";
        Integer groupId = 1;
        Integer tenantId = 1;
        String pipelineName = "encryption";
        String originId = "origin_1";




        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("publicKey", "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqoRdEwW3O51eTaupz8fks4PGhp1fmRHmsjijUarx0hlzRjSZm77IBbh93vlWhWnXV/CobpO/dIq0rrrDJ/CAncDCB7aQivMlplHy5+2hlZuENyNjevOXOrTxDlpnfXmumWCA6veqmcyQsASZmNMM+NVt91SirgEQt3EeTycegFN+nWl/2yQGEoqIg+2/WJ7dK+xr7rif7zRbbcBYnl+XdUrqR/4z46Q/prjiREbJCqYkrqsr/T530aS7ROEvQeK1gLbLqiIyxLLCcGMyA9nnk3RA8ADmkKm6Trx23t+6MgMct5Kde8IpoAgX8A4Iu9vQS+oB33BADEaKKtH9YVILTwIDAQAB-----END PUBLIC KEY-----"),
                Map.entry("apiUrl", "http://0.0.0.0:10001/copro-utils/data-security/encrypt"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "1"),
                Map.entry("decryptApiUrl","http://0.0.0.0:10001/copro-utils/data-security/decrypt"),
                Map.entry("encryption.activator","true")));


        String cipherStreamUtil = CipherStreamUtil.encryptionApi( jsonObject,  actionExecutionAudit,
                 rootPipelineId,  groupId,  tenantId,  pipelineName,  originId);
        System.out.println(cipherStreamUtil);
    }



    @Test
    public void decryptionTest() throws Exception {
        ActionExecutionAudit audit = new ActionExecutionAudit();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cipherText", "G8kxNSaTtRJT536/cHZgVUBTob9zVP+LQe6roRgkGFJh9h13rGo0mylC9fof5okaZIkzDkwsPUauFO1jzCPRJ0K5554uo6HdQ2V+bZB5iJlULr6sVaSuwgyvl9/4kUQhwoQRDezPR8e7LlRj5ihM2OlyDMtE6/9qTRJIg23XaFYXiCfg6VHWXpPj4Vdes76SN269YHM7f05UniTAmLQXcdDtNiTvQJ7/L8y9RsejELyDiuYklShrWJTnG/TMKk3vx4aUljrjA5V17+HaRxL968DkHYbn7iuLU+sReSvcp0f8sLPwFnHGv4G2v5tUdKCyf6/+ZwagRrpC1jGHRAiv0yGH2+PpwMZs5T5g05Rw37nM7hnDjcCdpYx0nNQ2Eg7mJRM4pvISkbG2zaYOhZjNnbrmBgBmIK6WPGC+/pR4pOoR5lSG919RMgJu96LHa7NeHms5oCZl+3JD/W1s3g3P3z6WJ4z9dZN/Dpq4Vah1YZvoHcPKXK/xHiq9pxvNkhFD3ALv5W8psCdTHvAXoMC33ug5KK3EXRiJFt1L8leJMr9KvetAxZzPG4aRkqPa5IxEBQ9seb2y4xAnxAMbnBfBolLIYnOxLDq19618BO9eYSINs2yPGTnw21RNadIQMrnH8YDQRkwYH2yIypJUcBs01zA+oIWJ8oWky16K65qXVQkyya/azKsAlqvPXs0s426JCFhYZMFrEK1gTsrn3Bf1dxa6CX/zLWS2fVTOTBgrQ4gFl8ULP0x6oCaDEbKRpV9Qjwf5lq50UrRnbIqlMmaGAZEleWjSIA/AoIQOREhH3swCCU3seisiKHN0UtXIKv8m9dpXuBC+kDW6AJmM9bzVODR2WMl1OdffrRbEjJy/LhfsPQ/YvQYORfvtN9UVsNsE1iaruj+Gt46kBK3DjDV3EzXg244rMGJwUgorMZ88C1/jjAXADifJ+BwCL7jCSgA/VKEM8p1x9Gik/Frdh+gOpek=");



        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("publicKey", "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqoRdEwW3O51eTaupz8fks4PGhp1fmRHmsjijUarx0hlzRjSZm77IBbh93vlWhWnXV/CobpO/dIq0rrrDJ/CAncDCB7aQivMlplHy5+2hlZuENyNjevOXOrTxDlpnfXmumWCA6veqmcyQsASZmNMM+NVt91SirgEQt3EeTycegFN+nWl/2yQGEoqIg+2/WJ7dK+xr7rif7zRbbcBYnl+XdUrqR/4z46Q/prjiREbJCqYkrqsr/T530aS7ROEvQeK1gLbLqiIyxLLCcGMyA9nnk3RA8ADmkKm6Trx23t+6MgMct5Kde8IpoAgX8A4Iu9vQS+oB33BADEaKKtH9YVILTwIDAQAB-----END PUBLIC KEY-----"),
                Map.entry("apiUrl", "http://0.0.0.0:10001/copro-utils/data-security/encrypt"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "1"),
                Map.entry("decryptApiUrl","http://0.0.0.0:10001/copro-utils/data-security/decrypt"),
                Map.entry("decryption.activator","true")));


        String cipherStreamUtil = CipherStreamUtil.decryptionApi(jsonObject, actionExecutionAudit);
        System.out.println(cipherStreamUtil);
        System.out.println(cipherStreamUtil instanceof String);

        ObjectMapper obj = new ObjectMapper();
        JsonNode json =  obj.readTree(cipherStreamUtil);
        System.out.println(json instanceof Object);
        System.out.println(json);
    }

}

