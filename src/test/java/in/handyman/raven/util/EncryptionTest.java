package in.handyman.raven.util;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.TransformAction;
import in.handyman.raven.lib.encryption.impl.AESEncryptionImpl;
import in.handyman.raven.lib.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.Transform;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class EncryptionTest {

    @Test
    public void InticsEncryptionMethod() {
//
//        String dataToEncrypt = "Jessica";
//        System.out.println("Actual Data: " + dataToEncrypt);
//

        InticsIntegrity encryption = new InticsIntegrity(new AESEncryptionImpl());
//        System.out.println("Encryption Method: " + encryption.getEncryptionMethod());

//        String encryptedData = encryption.encrypt(dataToEncrypt, "", "");
//        System.out.println("Encrypted Data: " + encryptedData);

        String decryptedData = encryption.decrypt("DxFaG9ICA0SNsBiNHWF3WQ==", "", "");
        System.out.println("Decrypted Data: " + decryptedData);

//        assertEquals(dataToEncrypt, decryptedData);


    }

    @Test
    public void TransformEncryptTest() {

        final ActionExecutionAudit actionExecutionAudit = ActionExecutionAudit.builder().build();

        actionExecutionAudit.setActionId(1234567L);
        actionExecutionAudit.setPipelineId(12345L);
        actionExecutionAudit.getContext().put("intics.encryption.secret.key", "2eea4bf3db275782feb6dc1f2c7e6419e5bacb7cd19d87f798c31301c8ea20bd");
        Transform writetransform = Transform.builder()
                .name("Transform block test")
                .on("intics_zio_db_conn")
                .value(List.of("INSERT INTO public.psql_encryption_test\n" +
                        "(sor_item_name, encrypted_answer)\n" +
                        "select sor_item_name,pgp_sym_encrypt(decrypted_answer,'${intics.encryption.secret.key}')::varchar as encrypted_answer \n" +
                        "from psql_encryption_input vt ;"))
                .condition(true)
                .format(true)
                .build();
        TransformAction writeTransformAction = new TransformAction(actionExecutionAudit, log, writetransform);

        writeTransformAction.execute();


    }

    @Test
    public void TransformDecryptTest() {


        final ActionExecutionAudit actionExecutionAudit = ActionExecutionAudit.builder().build();

        actionExecutionAudit.setActionId(1234567L);
        actionExecutionAudit.setPipelineId(12345L);
        actionExecutionAudit.getContext().put("intics.encryption.secret.key", "2eea4bf3db275782feb6dc1f2c7e6419e5bacb7cd19d87f798c31301c8ea20bd");
        Transform writetransform = Transform.builder()
                .name("Transform block test")
                .on("intics_zio_db_conn")
                .value(List.of("INSERT INTO psql_decryption_test\n" +
                        "(sor_item_name, decrypted_answer)\n" +
                        "select sor_item_name,pgp_sym_decrypt(encrypted_answer::bytea,'${intics.encryption.secret.key}') \n" +
                        "from psql_encryption_test;"))
                .condition(true)
                .format(true)
                .build();
        TransformAction writeTransformAction = new TransformAction(actionExecutionAudit, log, writetransform);

        writeTransformAction.execute();


    }


    @Test
    public void AssetInfoTest() {
        //Encryption Type Initialize
//        InticsIntegrity encryption = new InticsIntegrity(new AESEncryptionImpl());
//
//        ActionExecutionAudit executionAudit=new ActionExecutionAudit();
//        executionAudit.setActionId(1234L);
//        executionAudit.getContext().put("tenant_id","1");
//        AssetInfo assetInfo=new AssetInfo();
//        assetInfo.setAssetTable("macro.file_details_origin_audit");
//        assetInfo.setResourceConn("intics_zio_db_conn");
//        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(assetInfo.getResourceConn());
//        String[] fileName={"SYNTH_BH_1"};
//
//        //Encryption Method call
//        String[] encryptedValue = encryption.encrypt(fileName);
//
//        AssetInfoAction.FileInfo fileInfo= AssetInfoAction.FileInfo.builder()
//                .fileId("SYNTH_BH_1_12345")
//                .fileName(Arrays.toString(encryptedValue))
//                .build();
//        AssetInfoAction assetInfoAction=new AssetInfoAction(executionAudit,log,assetInfo);
//
//        assetInfoAction.consumerBatch(jdbi,List.of(fileInfo));

    }


}
