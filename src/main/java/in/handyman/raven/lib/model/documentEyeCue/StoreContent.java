package in.handyman.raven.lib.model.documentEyeCue;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

// ✅ Placeholder imports (replace with real JAR when available)
import com.anthem.acma.commonclient.storecontent.dto.StoreContentResponseDto;
import com.anthem.acma.commonclient.storecontent.dto.StoreContentRequestDto;
import com.anthem.acma.commonclient.storecontent.dto.Repository;
import com.anthem.acma.commonclient.storecontent.logic.Acmastorecontentclient;
import com.anthem.acma.commonclient.storecontent.logic.AcmastorecontentclientFactory;

import static co.elastic.clients.elasticsearch.snapshot.SnapshotSort.Repository;

@Slf4j
public class StoreContent {

    public <StoreContentResponseDto, StoreContentRequestDto> StoreContentResponseDto execute(String filePath,
                                                                                             String repository,
                                                                                             String applicationId,
                                                                                             String dcn,
                                                                                             String envUrl,
                                                                                             String apiKey,
                                                                                             String bearerToken) {

        in.handyman.raven.lib.model.documentEyeCue.StoreContentResponseDto responseDto = null;

        try {
            log.info("Starting StoreContent upload for file: {}", filePath);

            // ✅ Setup client properties
            Properties clientProps = new Properties();
            clientProps.setProperty("BASE_URL_STORECONTENTNONSTREAM", envUrl);
            clientProps.setProperty("BASE_URL_STORECONTENTSTREAM", envUrl);
            clientProps.put("isApigeeInvoked", "True");

            // ✅ Prepare request DTO
            StoreContentRequestDto requestDto = new StoreContentRequestDto();
            requestDto.setApplicationID(applicationId);
            requestDto.setRepository(Repository.valueOf(repository));

            // ✅ Mandatory Metadata
            HashMap<String, String> contentMetadata = new HashMap<>();
            contentMetadata.put("FileName", new File(filePath).getName());
            contentMetadata.put("MimeType", "application/pdf");
            requestDto.setContentMetaData(contentMetadata);

            // ✅ Versioning Params
            HashMap<String, String> additionalParams = new HashMap<>();
            additionalParams.put("versioning", "Y");
            additionalParams.put("contentkey", dcn);
            additionalParams.put("contentkeytype", "DCN");
            requestDto.setAddtionalParams(additionalParams);

            // ✅ Headers
            HashMap<String, String> headerMap = new HashMap<>();
            if (bearerToken != null && !bearerToken.isEmpty()) {
                headerMap.put("Authorization", "Bearer " + bearerToken);
            }
            headerMap.put("apikey", apiKey);
            headerMap.put("Accept", "application/json;charset=UTF-8");
            requestDto.setHeaderMap(headerMap);

            // ✅ Attach file stream
            File file = new File(filePath);
            requestDto.setContentData(new BufferedInputStream(new FileInputStream(file)));

            // ✅ Call StoreContent API
            Acmastorecontentclient client = AcmastorecontentclientFactory.createInstance(clientProps);
            responseDto = client.storeContent(requestDto);

            log.info("StoreContent Upload Status: {}", responseDto.getStatus());
            log.info("Content ID: {}", responseDto.getContentID());
            log.info("Message: {}", responseDto.getMessage());

        } catch (Exception e) {
            log.error("StoreContent Macro failed", e);
        }

        return responseDto;
    }
}


