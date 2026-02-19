package in.handyman.raven.lib.custom.outbound.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lib.custom.outbound.dao.MetadataContext;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.custom.outbound.model.*;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class MedicalPayloadGeneration {

    private final Logger log;
    public MedicalPayloadGeneration(Logger log) {
        this.log = log;
    }
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String DEFAULT_VALUE = "";
    private static final double DEFAULT_DOUBLE_VALUE = 0.0;

    // SOR Item Name Constants
    private static final String MEMBER_ID_SOR_ITEM_NAME = "member_id";
    private static final String MEDICAID_ID_SOR_ITEM_NAME = "medicaid_id";
    private static final String MEMBER_LAST_NAME_SOR_ITEM_NAME = "member_last_name";
    private static final String MEMBER_FIRST_NAME_SOR_ITEM_NAME = "member_first_name";
    private static final String MEMBER_DATE_OF_BIRTH_SOR_ITEM_NAME = "member_date_of_birth";
    private static final String MEMBER_GROUP_ID_SOR_ITEM_NAME = "member_group_id";
    private static final String MEMBER_GENDER_SOR_ITEM_NAME = "member_gender";
    private static final String MEMBER_ADDRESS_LINE_1_SOR_ITEM_NAME = "member_address_line1";
    private static final String MEMBER_CITY_SOR_ITEM_NAME = "member_city";
    private static final String MEMBER_ZIPCODE_SOR_ITEM_NAME = "member_zipcode";
    private static final String MEMBER_STATE_SOR_ITEM_NAME = "member_state";
    private static final String LEVEL_OF_SERVICE_SOR_ITEM_NAME = "level_of_service";
    private static final String SERVICE_FROM_DATE_SOR_ITEM_NAME = "service_from_date";
    private static final String SERVICE_TO_DATE_SOR_ITEM_NAME = "service_to_date";
    private static final String NOTIFICATION_TYPE_SOR_ITEM_NAME = "notification_type";
    private static final String AUTH_ADMIT_DATE_SOR_ITEM_NAME = "auth_admit_date";
    private static final String AUTH_DISCHARGE_DATE_SOR_ITEM_NAME = "auth_discharge_date";
    private static final String AUTH_ID_SOR_ITEM_NAME = "auth_id";
    private static final String DIAGNOSIS_CODE_SOR_ITEM_NAME = "diagnosis_code";
    private static final String DIAGNOSIS_DESCRIPTION_SOR_ITEM_NAME = "diagnosis_description";
    private static final String CODE_POINTER_SOR_ITEM_NAME = "code_pointer";
    private static final String SERVICE_CODE_SOR_ITEM_NAME = "service_code";
    private static final String SERVICE_MODIFIER_SOR_ITEM_NAME = "service_code_modifier";
    private static final String SERVICE_DESCRIPTION_SOR_ITEM_NAME = "service_description";
    private static final String LINE_NUMBER_SOR_ITEM_NAME = "line_number";
    private static final String FAX_RECEIVED_DATE_SOR_ITEM_NAME = "fax_received_date";
    private static final String TOTAL_SERVICE_DAYS_SOR_ITEM_NAME = "total_service_days";
    private static final String SERVICE_QUANTITY_VISITS_SOR_ITEM_NAME = "service_quantity_visits";
    private static final String SERVICE_QUANTITY_UNIT_SOR_ITEM_NAME = "service_quantity_units";
    private static final String MULTIPLE_MEMBER_SOR_ITEM_NAME = "multiple_member_indicator";
    private static final String NEWBORN_REQUEST_SOR_ITEM_NAME = "newborn_request";
    private static final String NEWBORN_LAST_NAME_SOR_ITEM_NAME = "newborn_last_name";
    private static final String NEWBORN_FIRST_NAME_SOR_ITEM_NAME = "newborn_first_name";
    private static final String NEWBORN_DATE_OF_BIRTH_SOR_ITEM_NAME = "newborn_date_of_birth";
    private static final String NEWBORN_GENDER_SOR_ITEM_NAME = "newborn_gender";
    private static final String FAX_REPORT_SOR_ITEM_NAME = "fax_report";
    private static final String CLINICAL_PRESENT_PROP_VALUE = "CLINICAL_PRESENT";

    private static final String FIRST_NAME_SUFFIX = "_first_name";
    private static final String LAST_NAME_SUFFIX = "_last_name";
    private static final String ADDRESS_LINE1_SUFFIX = "_address_line1";
    private static final String ADDRESS_LINE2_SUFFIX = "_address_line2";
    private static final String CITY_SUFFIX = "_city";
    private static final String STATE_SUFFIX = "_state";
    private static final String ZIPCODE_SUFFIX = "_zipcode";
    private static final String SPECIALTY_SUFFIX = "_specialty";

    public MedicalOutboundResponse buildMedicalOutboundResponse(
            List<PredictionDTO> predictions,
            Map<String, String> configMap,
            String metadataContextString
            ) throws JsonProcessingException {

        MetadataContext metadataContext = mapper.readValue(metadataContextString, MetadataContext.class);

        log.info("Building complete medical outbound response");

        OutboundJsonMetaData metadata = buildMetadata(metadataContext, configMap);

        MedicalPayload payload = buildMedicalPayload(predictions, configMap);

        String uploadStatus = metadataContext.getUploadStatus();
        String mappedStatus = "SUCCESS";
        if (uploadStatus != null) {
            String statusUpper = uploadStatus.toUpperCase();
            if (statusUpper.contains("FAIL") || statusUpper.contains("ERROR") || 
                statusUpper.contains("REJECT") || statusUpper.contains("INVALID")) {
                mappedStatus = "FAILURE";
            } else if (statusUpper.equals("SUCCESS") || statusUpper.equals("COMPLETED") || 
                       statusUpper.equals("COMPLETE") || statusUpper.equals("SUCCEEDED")) {
                mappedStatus = "SUCCESS";
            }
        }

        MedicalOutboundResponse.MedicalOutboundResponseBuilder responseBuilder = MedicalOutboundResponse.builder()
                .requestTxnId(metadataContext.getRequestTxnId())
                .status(mappedStatus)
                .documentId(metadataContext.getDocumentId())
                .inboundTransactionId(metadataContext.getInboundTransactionId())
                .metadata(metadata)
                .aumipayload(payload);

        if (metadataContext.getErrorCode() != 200 && metadataContext.getErrorCode() != null) {
            if (metadataContext.getErrorMessage() != null && !metadataContext.getErrorMessage().trim().isEmpty()) {
                responseBuilder.errorMessage(metadataContext.getErrorMessage());
            }
            if (metadataContext.getErrorMessageDetail() != null && !metadataContext.getErrorMessageDetail().trim().isEmpty()) {
                responseBuilder.errorMessageDetail(metadataContext.getErrorMessageDetail());
            }
            if (metadataContext.getErrorCode() != null) {
                responseBuilder.errorCd(metadataContext.getErrorCode());
            }
        } else {
            responseBuilder.errorMessage(null)
                    .errorMessageDetail(null)
                    .errorCd(null);
        }
        
        return responseBuilder.build();
    }


    private OutboundJsonMetaData buildMetadata(MetadataContext context, Map<String, String> configMap) {
        log.info("Building metadata section");

        Integer overallConfidence = 0;
        Long processingTimeMs = 0L;
        if (context.getProcessStartTime() != null && context.getProcessEndTime() != null) {
            try {
                java.time.LocalDateTime startTime = java.time.LocalDateTime.parse(context.getProcessStartTime());
                java.time.LocalDateTime endTime = java.time.LocalDateTime.parse(context.getProcessEndTime());
                java.time.Duration duration = java.time.Duration.between(startTime, endTime);
                processingTimeMs = duration.toMillis();
            } catch (Exception e) {
                log.warn("Failed to calculate processingTimeMs from timestamps: {} - {}", 
                        context.getProcessStartTime(), context.getProcessEndTime(), e);
            }
        }

        return OutboundJsonMetaData.builder()
                .documentExtension(context.getDocumentExtension())
                .transactionId(context.getTransactionId())
                .inboundDocumentName(context.getInboundDocumentName())
                .documentType(context.getDocumentType())
                .processStartTime(context.getProcessStartTime())
                .processEndTime(context.getProcessEndTime())
                .processingTimeMs(processingTimeMs)
                .processedAt(context.getProcessedAt())
                .pageCount(context.getCandidatePapers() != null ? context.getCandidatePapers().size() : 0)
                .candidatePaper(context.getCandidatePapers() != null ? context.getCandidatePapers() : Collections.emptyList())
                .overallConfidence(overallConfidence)
                .build();
    }


    private MedicalPayload buildMedicalPayload(List<PredictionDTO> predictions, Map<String, String> configMap) {
        log.info("Building medical payload from {} predictions", predictions.size());

        List<PredictionDTO> singleEntityPredictions =  predictions.stream().filter(predictionDTO -> !predictionDTO.getIsMultiEntityEnabled()).collect(Collectors.toList());

        List<PredictionDTO> multiEntityPredictions =  predictions.stream().filter(PredictionDTO::getIsMultiEntityEnabled).collect(Collectors.toList());
        MedicalPayload.MedicalPayloadBuilder builder = MedicalPayload.builder();

        getSingleEntityMedicalPayload(singleEntityPredictions, configMap, builder);
        getMultiEntityMedicalPayload(multiEntityPredictions, configMap, builder);


        return builder.build();
    }


    private void getSingleEntityMedicalPayload(List<PredictionDTO> predictions, Map<String, String> configMap, MedicalPayload.MedicalPayloadBuilder builder) {

        Map<String, List<PredictionDTO>> predictionsByType = predictions.stream()
                .collect(Collectors.groupingBy(
                        pred -> pred.getLineItemType() != null ? pred.getLineItemType()
                                : "single_value",
                        Collectors.toList()));

        List<PredictionDTO> singleValuePredictions = predictionsByType.getOrDefault("single_value",
                Collections.emptyList());
        List<PredictionDTO> multiValuePredictions = predictionsByType.getOrDefault("multi_value",
                Collections.emptyList());

        Map<String, ExtractedField> singleValueFieldMap = buildSingleValueFieldMap(singleValuePredictions,
                configMap);

        boolean cleanStatus = Boolean
                .parseBoolean(configMap.getOrDefault("CUSTOM_MEDICAL_OUTBOUND_CLEANER", "true"));

        buildMemberSection(builder, singleValueFieldMap, cleanStatus);

        buildAuthorizationSection(builder, singleValueFieldMap, cleanStatus);

        List<ServiceModifier> serviceModifiers = buildServiceModifiers(multiValuePredictions, configMap);
        if (!serviceModifiers.isEmpty()) {
            builder.service(serviceModifiers);
        }

        List<Diagonsis> diagnosisList = buildDiagnosisList(multiValuePredictions, singleValueFieldMap,
                configMap,
                cleanStatus);
        if (!diagnosisList.isEmpty()) {
            builder.diagnosis(diagnosisList);
        }

        List<Provider> providers = buildProvidersFromSingleValue(singleValueFieldMap, cleanStatus);
        if (!providers.isEmpty()) {
            builder.provider(providers);
        }

        List<AdditionalProperties> additionalProperties = buildAdditionalProperties(singleValueFieldMap,
                configMap);
        if (!additionalProperties.isEmpty()) {
            builder.additionalProperties(additionalProperties);
        }

        List<AdditionalProperties> memberAdditionalProperties = buildMemberAdditionalProperties(
                singleValueFieldMap);
        if (!memberAdditionalProperties.isEmpty()) {
            builder.memberAdditionalProperties(memberAdditionalProperties);
        }

    }


    private void getMultiEntityMedicalPayload(List<PredictionDTO> predictions, Map<String, String> configMap, MedicalPayload.MedicalPayloadBuilder builder) {

        Map<String, List<PredictionDTO>> predictionsByType = predictions.stream()
                .collect(Collectors.groupingBy(
                        pred -> pred.getLineItemType() != null ? pred.getLineItemType()
                                : "single_value",
                        Collectors.toList()));

        List<PredictionDTO> singleValuePredictions = predictionsByType.getOrDefault("single_value",
                Collections.emptyList());

        // Build field map from single_value predictions
        Map<String, ExtractedField> singleValueFieldMap = buildSingleValueFieldMap(singleValuePredictions,
                configMap);

        boolean cleanStatus = Boolean
                .parseBoolean(configMap.getOrDefault("CUSTOM_MEDICAL_OUTBOUND_CLEANER", "false"));

        List<Provider> providers = buildProvidersFromSingleValue(singleValueFieldMap, cleanStatus);
        if (!providers.isEmpty()) {
            builder.provider(providers);
        }

    }

    private Map<String, ExtractedField> buildSingleValueFieldMap(List<PredictionDTO> singleValuePredictions,
                                                                 Map<String, String> configMap) {
        if (singleValuePredictions == null || singleValuePredictions.isEmpty()) {
            log.info("No single_value predictions to process");
            return new HashMap<>();
        }

        log.info("Building field map from {} single_value predictions", singleValuePredictions.size());

        Map<String, ExtractedField> fieldMap = new HashMap<>();

        int confidenceMultiplier = Integer
                .parseInt(configMap.getOrDefault("CONFIDENCE_SCORE_MULTIPLY_VARIABLE", "100"));
        int scaledWidth = Integer.parseInt(configMap.getOrDefault("AUMI_BBOX_SCALAR_WIDTH", "1000"));
        int scaledHeight = Integer.parseInt(configMap.getOrDefault("AUMI_BBOX_SCALAR_HEIGHT", "1000"));
        int roundingPrecision = Integer.parseInt(configMap.getOrDefault("FLOAT_VALUE_ROUNDING_PRECISION", "2"));
        boolean reorderPaperNumber = Boolean
                .parseBoolean(configMap.getOrDefault("aumi.reorder.paper.number", "false"));

        for (PredictionDTO prediction : singleValuePredictions) {
            String itemName = prediction.getSorItemName();
            String value = prediction.getPredictedValue();

            if (itemName == null || value == null || value.trim().isEmpty()) {
                continue;
            }

            ExtractedField field = createExtractedFieldFromPrediction(
                    prediction, confidenceMultiplier, scaledWidth, scaledHeight,
                    roundingPrecision, reorderPaperNumber);

            fieldMap.put(itemName, field);
            log.debug("Added single_value field: {} = {}", itemName, value);
        }

        log.info("Built field map with {} single_value entries", fieldMap.size());
        return fieldMap;
    }

    private List<ServiceModifier> buildServiceModifiers(List<PredictionDTO> multiValuePredictions,
                                                        Map<String, String> configMap) {
        log.info("Building service modifiers from multi-value fields");

        Map<String, Map<String, ExtractedField>> groupedByInstance = groupAndExtractFields(
                multiValuePredictions,
                configMap);
        List<ServiceModifier> serviceModifiers = new ArrayList<>();

        for (Map<String, ExtractedField> fieldMap : groupedByInstance.values()) {
            ExtractedField serviceCode = fieldMap.get(SERVICE_CODE_SOR_ITEM_NAME);

            if (serviceCode == null || serviceCode.getValue() == null || serviceCode.getValue().isEmpty()) {
                continue;
            }

            ExtractedField modifier = fieldMap.get(SERVICE_MODIFIER_SOR_ITEM_NAME);
            List<ServiceModifierWrapper> modifierList = new ArrayList<>();
            if (modifier != null && modifier.getValue() != null && !modifier.getValue().isEmpty()) {
                modifierList.add(ServiceModifierWrapper.builder().cd(modifier).build());
            }

            List<ServiceQuantity> quantities = buildServiceQuantities(fieldMap);

            ServiceModifier serviceModifier = ServiceModifier.builder()
                    .cd(serviceCode)
                    .modifier(modifierList.isEmpty() ? null : modifierList)
                    .serviceQuantity(quantities.isEmpty() ? null : quantities)
                    .build();

            serviceModifiers.add(serviceModifier);
        }

        log.info("Built {} service modifiers", serviceModifiers.size());
        return serviceModifiers;
    }

    private List<ServiceQuantity> buildServiceQuantities(Map<String, ExtractedField> fieldMap) {
        List<ServiceQuantity> quantities = new ArrayList<>();

        ExtractedField unitField = fieldMap.get(SERVICE_QUANTITY_UNIT_SOR_ITEM_NAME);
        if (unitField != null && unitField.getValue() != null && !unitField.getValue().isEmpty()) {
            quantities.add(ServiceQuantity.builder()
                    .quantityType(SimpleValueField.builder().value("Units").build())
                    .quantityUnits(unitField)
                    .build());
        }

        ExtractedField visitField = fieldMap.get(SERVICE_QUANTITY_VISITS_SOR_ITEM_NAME);
        if (visitField != null && visitField.getValue() != null && !visitField.getValue().isEmpty()) {
            quantities.add(ServiceQuantity.builder()
                    .quantityType(SimpleValueField.builder().value("Visits").build())
                    .quantityUnits(visitField)
                    .build());
        }

        return quantities;
    }

    private Map<String, Map<String, ExtractedField>> groupAndExtractFields(List<PredictionDTO> predictions,
                                                                           Map<String, String> configMap) {
        int confidenceMultiplier = Integer
                .parseInt(configMap.getOrDefault("CONFIDENCE_SCORE_MULTIPLY_VARIABLE", "100"));
        int scaledWidth = Integer.parseInt(configMap.getOrDefault("AUMI_BBOX_SCALAR_WIDTH", "1000"));
        int scaledHeight = Integer.parseInt(configMap.getOrDefault("AUMI_BBOX_SCALAR_HEIGHT", "1000"));
        int roundingPrecision = Integer.parseInt(configMap.getOrDefault("FLOAT_VALUE_ROUNDING_PRECISION", "2"));
        boolean reorderPaperNumber = Boolean
                .parseBoolean(configMap.getOrDefault("aumi.reorder.paper.number", "false"));

        // Group by sorContainerInstance
        Map<String, List<PredictionDTO>> byInstance = predictions.stream()
                .collect(Collectors
                        .groupingBy(p -> p.getSorContainerInstance() != null
                                ? p.getSorContainerInstance()
                                : "1"));

        Map<String, Map<String, ExtractedField>> result = new TreeMap<>(); // Sorted by instance ID

        for (Map.Entry<String, List<PredictionDTO>> entry : byInstance.entrySet()) {
            Map<String, ExtractedField> fieldMap = new HashMap<>();
            for (PredictionDTO p : entry.getValue()) {
                ExtractedField field = createExtractedFieldFromPrediction(
                        p, confidenceMultiplier, scaledWidth, scaledHeight,
                        roundingPrecision, reorderPaperNumber);
                fieldMap.put(p.getSorItemName(), field);
            }
            result.put(entry.getKey(), fieldMap);
        }
        return result;
    }

    private ExtractedField createExtractedFieldFromPrediction(PredictionDTO prediction,
                                                              int confidenceMultiplier,
                                                              int scaledWidth, int scaledHeight,
                                                              int roundingPrecision,
                                                              boolean reorderPaperNumber) {
        int confidence = (int) (Math.round((prediction.getPrecision() * confidenceMultiplier) / 10.0) * 10);

        int paperNumber = prediction.getPaperNo();
        if (reorderPaperNumber && paperNumber >= 1) {
            paperNumber = paperNumber - 1;
        }

        double[] boundingBox = new double[] {
                prediction.getLeftPos(),
                prediction.getUpperPos(),
                prediction.getRightPos(),
                prediction.getLowerPos()
        };

        double[] rescaledBox = rescaleBoundingBox(
                boundingBox,
                prediction.getImageWidth(),
                prediction.getImageHeight(),
                scaledWidth,
                scaledHeight,
                roundingPrecision);

        Map<String, Double> bboxMap = new HashMap<>();
        bboxMap.put("x", rescaledBox[0]);
        bboxMap.put("y", rescaledBox[1]);
        bboxMap.put(WIDTH, rescaledBox[2]);
        bboxMap.put(HEIGHT, rescaledBox[3]);

        JsonNode boundingBoxNode = mapper.valueToTree(bboxMap);

        return ExtractedField.builder()
                .value(prediction.getPredictedValue())
                .confidence(confidence)
                .page(paperNumber)
                .boundingBox(boundingBoxNode)
                .build();
    }

    private double[] rescaleBoundingBox(double[] boundingBox, int originalWidth, int originalHeight,
                                        int scaledWidth, int scaledHeight, int precision) {
        double xScale = (double) originalWidth / scaledWidth;
        double yScale = (double) originalHeight / scaledHeight;

        return new double[] {
                round(boundingBox[0] * xScale, precision),
                round(boundingBox[1] * yScale, precision),
                round(boundingBox[2] * xScale, precision),
                round(boundingBox[3] * yScale, precision)
        };
    }

    private double round(double value, int places) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void buildMemberSection(MedicalPayload.MedicalPayloadBuilder builder,
                                    Map<String, ExtractedField> fieldMap,
                                    boolean cleanStatus) {
        log.info("Building member section");

        if (cleanStatus) {
            putIfPresent(builder::hcid, fieldMap, MEMBER_ID_SOR_ITEM_NAME);
            putIfPresent(builder::medicaidId, fieldMap, MEDICAID_ID_SOR_ITEM_NAME);
            putIfPresent(builder::groupId, fieldMap, MEMBER_GROUP_ID_SOR_ITEM_NAME);
            putIfPresent(builder::memberFirstName, fieldMap, MEMBER_FIRST_NAME_SOR_ITEM_NAME);
            putIfPresent(builder::memberLastName, fieldMap, MEMBER_LAST_NAME_SOR_ITEM_NAME);
            putIfPresent(builder::memberDOB, fieldMap, MEMBER_DATE_OF_BIRTH_SOR_ITEM_NAME);
            putIfPresent(builder::memberGender, fieldMap, MEMBER_GENDER_SOR_ITEM_NAME);
            putIfPresent(builder::memberAddressLine1, fieldMap, MEMBER_ADDRESS_LINE_1_SOR_ITEM_NAME);
            putIfPresent(builder::memberCity, fieldMap, MEMBER_CITY_SOR_ITEM_NAME);
            putIfPresent(builder::memberState, fieldMap, MEMBER_STATE_SOR_ITEM_NAME);
            putIfPresent(builder::memberZipCode, fieldMap, MEMBER_ZIPCODE_SOR_ITEM_NAME);
        }
        else{
            builder.hcid(fieldMap.getOrDefault(MEMBER_ID_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .medicaidId(fieldMap.getOrDefault(MEDICAID_ID_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .groupId(fieldMap.getOrDefault(MEMBER_GROUP_ID_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .memberFirstName(fieldMap.getOrDefault(MEMBER_FIRST_NAME_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .memberLastName(fieldMap.getOrDefault(MEMBER_LAST_NAME_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .memberDOB(fieldMap.getOrDefault(MEMBER_DATE_OF_BIRTH_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .memberGender(fieldMap.getOrDefault(MEMBER_GENDER_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .memberAddressLine1(fieldMap.getOrDefault(MEMBER_ADDRESS_LINE_1_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .memberCity(fieldMap.getOrDefault(MEMBER_CITY_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .memberState(fieldMap.getOrDefault(MEMBER_STATE_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .memberZipCode(fieldMap.getOrDefault(MEMBER_ZIPCODE_SOR_ITEM_NAME, getDefaultExtractedField()));
        }
    }


    private void buildAuthorizationSection(MedicalPayload.MedicalPayloadBuilder builder,
                                           Map<String, ExtractedField> fieldMap,
                                           boolean cleanStatus) {
        log.info("Building authorization section");

        if (cleanStatus) {
            putIfPresent(builder::authId, fieldMap, AUTH_ID_SOR_ITEM_NAME);
            putIfPresent(builder::levelOfService, fieldMap, LEVEL_OF_SERVICE_SOR_ITEM_NAME);
            putIfPresent(builder::serviceFromDate, fieldMap, SERVICE_FROM_DATE_SOR_ITEM_NAME);
            putIfPresent(builder::serviceToDate, fieldMap, SERVICE_TO_DATE_SOR_ITEM_NAME);
            putIfPresent(builder::authAdmitDate, fieldMap, AUTH_ADMIT_DATE_SOR_ITEM_NAME);
            putIfPresent(builder::authDischargeDate, fieldMap, AUTH_DISCHARGE_DATE_SOR_ITEM_NAME);
            putIfPresent(builder::faxReceivedDate, fieldMap, FAX_RECEIVED_DATE_SOR_ITEM_NAME);
            putIfPresent(builder::totalServiceDays, fieldMap, TOTAL_SERVICE_DAYS_SOR_ITEM_NAME);
            putIfPresent(builder::notificationType, fieldMap, NOTIFICATION_TYPE_SOR_ITEM_NAME);

        } else {
            builder.authId(fieldMap.getOrDefault(AUTH_ID_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .levelOfService(fieldMap.getOrDefault(LEVEL_OF_SERVICE_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .serviceFromDate(fieldMap.getOrDefault(SERVICE_FROM_DATE_SOR_ITEM_NAME,getDefaultExtractedField()))
                    .serviceToDate(fieldMap.getOrDefault(SERVICE_TO_DATE_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .authAdmitDate(fieldMap.getOrDefault(AUTH_ADMIT_DATE_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .authDischargeDate(fieldMap.getOrDefault(AUTH_DISCHARGE_DATE_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .faxReceivedDate(fieldMap.getOrDefault(FAX_RECEIVED_DATE_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .totalServiceDays(fieldMap.getOrDefault(TOTAL_SERVICE_DAYS_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .notificationType(fieldMap.getOrDefault(NOTIFICATION_TYPE_SOR_ITEM_NAME, getDefaultExtractedField()));

        }
    }

    private List<Diagonsis> buildDiagnosisList(List<PredictionDTO> multiValuePredictions,
                                               Map<String, ExtractedField> singleValueMap,
                                               Map<String, String> configMap,
                                               boolean cleanStatus) {
        log.info("Building diagnosis list from multi-value fields");

        Map<String, ExtractedField> fieldMap = new HashMap<>(singleValueMap);

        Map<String, Map<String, ExtractedField>> groupedByInstance = groupAndExtractFields(
                multiValuePredictions,
                configMap);

        for (Map.Entry<String, Map<String, ExtractedField>> instanceEntry : groupedByInstance.entrySet()) {
            String instanceId = instanceEntry.getKey();
            Map<String, ExtractedField> instanceFields = instanceEntry.getValue();
            
            for (Map.Entry<String, ExtractedField> fieldEntry : instanceFields.entrySet()) {
                String fieldName = fieldEntry.getKey();
                ExtractedField field = fieldEntry.getValue();
                
                if ("1".equals(instanceId)) {
                    if (!fieldMap.containsKey(fieldName)) {
                        fieldMap.put(fieldName, field);
                    }
                } else {
                    fieldMap.put(fieldName + "_" + instanceId, field);
                }
            }
        }

        List<Diagonsis> diagnosisList = new ArrayList<>();

        // Find all diagnosis code entries (e.g., "diagnosis_code", "diagnosis_code_1", "diagnosis_code_2", etc.)
        List<Map.Entry<String, ExtractedField>> diagnosisCodeEntries = fieldMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(DIAGNOSIS_CODE_SOR_ITEM_NAME))
                .collect(Collectors.toList());

        if (diagnosisCodeEntries.isEmpty()) {
            if (!cleanStatus) {
                log.warn("No diagnosis codes found; adding default Diagnosis");
                Diagonsis.DiagonsisBuilder builder = Diagonsis.builder()
                        .cd(getDefaultExtractedField())
                        .desc(fieldMap.getOrDefault(DIAGNOSIS_DESCRIPTION_SOR_ITEM_NAME, getDefaultExtractedField()))
                        .codePointer(fieldMap.getOrDefault(CODE_POINTER_SOR_ITEM_NAME, getDefaultExtractedField()));
                diagnosisList.add(builder.build());
            } else {
                log.info("No diagnosis codes found; skipped default Diagnosis due to customMedicalOutboundCleaner");
            }
        } else {
            for (Map.Entry<String, ExtractedField> entry : diagnosisCodeEntries) {
                String key = entry.getKey();
                ExtractedField icd10CodeField = entry.getValue();
                
                if (cleanStatus && (icd10CodeField.getValue() == null || icd10CodeField.getValue().isEmpty())) {
                    log.info("Skipped Diagnosis for key {} due to empty code value", key);
                    continue;
                }

                String descKey = key.replace(DIAGNOSIS_CODE_SOR_ITEM_NAME, DIAGNOSIS_DESCRIPTION_SOR_ITEM_NAME);
                String codePointerKey = key.replace(DIAGNOSIS_CODE_SOR_ITEM_NAME, CODE_POINTER_SOR_ITEM_NAME);

                ExtractedField description = fieldMap.getOrDefault(descKey, 
                        fieldMap.getOrDefault(DIAGNOSIS_DESCRIPTION_SOR_ITEM_NAME, getDefaultExtractedField()));

                ExtractedField codePointer = fieldMap.getOrDefault(codePointerKey, 
                        fieldMap.getOrDefault(CODE_POINTER_SOR_ITEM_NAME, getDefaultExtractedField()));

                Diagonsis.DiagonsisBuilder builder = Diagonsis.builder();
                builder.cd(icd10CodeField)
                        .desc(description)
                        .codePointer(codePointer);


                diagnosisList.add(builder.build());
                log.info("Generated Diagnosis for key: {}", key);
            }
        }

        log.info("Built {} diagnosis entries", diagnosisList.size());
        return diagnosisList;
    }

    private List<Provider> buildProvidersFromSingleValue(Map<String, ExtractedField> fieldMap,
                                                         boolean cleanStatus) {
        log.info("Building providers from single-value fields");

        List<Provider> providers = new ArrayList<>();

        Provider servicingProvider = buildProviderIfPresent("Servicing Provider", "servicing_provider",
                fieldMap,
                cleanStatus);
        if (servicingProvider != null) {
            providers.add(servicingProvider);
        }

        Provider servicingFacility = buildProviderIfPresent("Servicing Facility", "servicing_facility",
                fieldMap,
                cleanStatus);
        if (servicingFacility != null) {
            providers.add(servicingFacility);
        }

        Provider referringProvider = buildProviderIfPresent("Requesting Provider", "referring_provider",
                fieldMap,
                cleanStatus);
        if (referringProvider != null) {
            providers.add(referringProvider);
        }

        Provider orderingProvider = buildProviderIfPresent("Ordering Provider", "ordering_provider", fieldMap,
                cleanStatus);
        if (orderingProvider != null) {
            providers.add(orderingProvider);
        }

        log.info("Built {} providers", providers.size());
        return providers;
    }

    private Provider buildProviderIfPresent(String category, String prefix,
                                            Map<String, ExtractedField> fieldMap,
                                            boolean cleanStatus) {
        Provider.ProviderBuilder builder = Provider.builder();

        boolean hasData = false;

        if (cleanStatus) {
            hasData |= putIfPresentProvider(builder::providerNPI, fieldMap, prefix + "_npi");
            hasData |= putIfPresentProvider(builder::providerTIN, fieldMap, prefix + "_tin");
            hasData |= putIfPresentProvider(builder::providerFirstName, fieldMap,
                    prefix + FIRST_NAME_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerLastName, fieldMap, prefix + LAST_NAME_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerAddressLine1, fieldMap,
                    prefix + ADDRESS_LINE1_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerAddressLine2, fieldMap,
                    prefix + ADDRESS_LINE2_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerCity, fieldMap, prefix + CITY_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerState, fieldMap, prefix + STATE_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerZipCode, fieldMap, prefix + ZIPCODE_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerSpeciality, fieldMap,
                    prefix + SPECIALTY_SUFFIX);

            if (!hasData) {
                log.debug("Skipping {} - no data present", category);
                return null;
            }
        } else {
            // Only set fields that have values, and check if provider has any dat
            hasData |= putIfPresentProvider(builder::providerNPI, fieldMap, prefix + "_npi");
            hasData |= putIfPresentProvider(builder::providerTIN, fieldMap, prefix + "_tin");
            hasData |= putIfPresentProvider(builder::providerFirstName, fieldMap,
                    prefix + FIRST_NAME_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerLastName, fieldMap, prefix + LAST_NAME_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerAddressLine1, fieldMap,
                    prefix + ADDRESS_LINE1_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerAddressLine2, fieldMap,
                    prefix + ADDRESS_LINE2_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerCity, fieldMap, prefix + CITY_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerState, fieldMap, prefix + STATE_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerZipCode, fieldMap, prefix + ZIPCODE_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerSpeciality, fieldMap,
                    prefix + SPECIALTY_SUFFIX);

            if (!hasData) {
                log.debug("Skipping {} - no data present", category);
                return null;
            }
        }

        builder.providerCategory(CategoryField.builder().value(category).build());
        return builder.build();
    }

    private boolean putIfPresentProvider(Consumer<ExtractedField> setter,
                                         Map<String, ExtractedField> fieldMap,
                                         String key) {
        ExtractedField value = fieldMap.get(key);
        if (value != null && value.getValue() != null && !value.getValue().isEmpty()) {
            setter.accept(value);
            return true;
        }
        return false;
    }

    private List<AdditionalProperties> buildAdditionalProperties(Map<String, ExtractedField> fieldMap,
                                                                 Map<String, String> configMap) {
        log.info("Building additional properties");

        List<AdditionalProperties> properties = new ArrayList<>();

        handleClinicalPresent(fieldMap, properties, configMap);
        handleAuthAdditionalKeyword(fieldMap, properties);
        handleResponsibleArea(fieldMap, properties);
        handleLevelOfCare(fieldMap, properties);
        handleFaxReport(fieldMap, properties);

        log.info("Built {} additional properties", properties.size());
        return properties;
    }

    private List<AdditionalProperties> buildMemberAdditionalProperties(Map<String, ExtractedField> fieldMap) {
        log.info("Building member additional properties");

        List<AdditionalProperties> properties = new ArrayList<>();

        addMemberAdditionalPropertiesIfPresent(properties, "MULTIPLE_MEMBER",
                    fieldMap.get(MULTIPLE_MEMBER_SOR_ITEM_NAME));
        addMemberAdditionalPropertiesIfPresent(properties, "NEWBORN_REQUEST",
                    fieldMap.get(NEWBORN_REQUEST_SOR_ITEM_NAME));
        addMemberAdditionalPropertiesIfPresent(properties, "NEWBORN_FIRSTNAME",
                    fieldMap.get(NEWBORN_FIRST_NAME_SOR_ITEM_NAME));
        addMemberAdditionalPropertiesIfPresent(properties, "NEWBORN_LASTNAME",
                    fieldMap.get(NEWBORN_LAST_NAME_SOR_ITEM_NAME));
        addMemberAdditionalPropertiesIfPresent(properties, "NEWBORN_GENDER",
                    fieldMap.get(NEWBORN_GENDER_SOR_ITEM_NAME));
        addMemberAdditionalPropertiesIfPresent(properties, "NEWBORN_DOB",
                    fieldMap.get(NEWBORN_DATE_OF_BIRTH_SOR_ITEM_NAME));

        log.info("Built {} member additional properties", properties.size());
        return properties;
    }

    private void addMemberAdditionalPropertiesIfPresent(List<AdditionalProperties> properties,
                                             String propName,
                                             ExtractedField field) {
        if (field != null && field.getValue() != null && !field.getValue().isEmpty()) {
            properties.add(AdditionalProperties.builder()
                    .propName(propName)
                    .propValue(field.getValue())
                    .page(field.getPage())
                    .confidence(Double.valueOf(field.getConfidence()))
                    .boundingBox(field.getBoundingBox())
                    .build());
        }
    }

    private void putIfPresent(Consumer<ExtractedField> setter,
                              Map<String, ExtractedField> fieldMap,
                              String key) {
        ExtractedField value = fieldMap.get(key);
        if (value != null && value.getValue() != null && !value.getValue().isEmpty()) {
            setter.accept(value);
        }
    }

    private String getFieldValue(Map<String, ExtractedField> fieldMap, String key) {
        ExtractedField field = fieldMap.get(key);
        return (field != null && field.getValue() != null) ? field.getValue() : null;
    }

    private ExtractedField getDefaultExtractedField() {
        Map<String, Double> boundingBoxMap = new HashMap<>();
        boundingBoxMap.put("x", DEFAULT_DOUBLE_VALUE);
        boundingBoxMap.put("y", DEFAULT_DOUBLE_VALUE);
        boundingBoxMap.put(WIDTH, DEFAULT_DOUBLE_VALUE);
        boundingBoxMap.put(HEIGHT, DEFAULT_DOUBLE_VALUE);
        JsonNode boundingBoxJsonNode = mapper.valueToTree(boundingBoxMap);

        return ExtractedField.builder()
                .value(DEFAULT_VALUE)
                .page(0)
                .confidence(0)
                .boundingBox(boundingBoxJsonNode)
                .build();
    }


    private void handleClinicalPresent(Map<String, ExtractedField> fieldMap,
                                       List<AdditionalProperties> additionalList,
                                       Map<String, String> configMap) {
        fieldMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("clinical_present"))
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    try {
                        ExtractedField field = entry.getValue();

                        if (field != null && hasValue(field.getValue())) {

                            AdditionalProperties prop = AdditionalProperties.builder()
                                    .propName(CLINICAL_PRESENT_PROP_VALUE)
                                    .propValue(field.getValue())
                                    .page(field.getPage())
                                    .confidence(Double.valueOf(field.getConfidence()))
                                    .boundingBox(field.getBoundingBox())
                                    .build();

                            additionalList.add(prop);

                            log.info("Added Clinical Present property as '{}' since total pages {}",
                                    field.getValue(), field.getPage());
                        }
                    } catch (Exception ex) {
                        log.error("Error processing clinical_present field: {}", entry.getKey(), ex);
                    }
                });
    }

    private void handleAuthAdditionalKeyword(Map<String, ExtractedField> fieldMap,
                                            List<AdditionalProperties> additionalList) {
        fieldMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("additional_auth_properties"))
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    try {
                        ExtractedField field = entry.getValue();

                        if (field == null || !hasValue(field.getValue())) {
                            log.info("AUTH_ADDL_KEYWORD not found or empty");
                            return;
                        }

                        String[] keywords = field.getValue().split(",");

                        for (String keyword : keywords) {
                            String trimmedKeyword = keyword.trim();
                            if (trimmedKeyword.isEmpty()) continue;

                            AdditionalProperties prop = AdditionalProperties.builder()
                                    .propName("AUTH_ADDL_KEYWORD")
                                    .propValue(trimmedKeyword)
                                    .page(field.getPage())
                                    .confidence(Double.valueOf(field.getConfidence()))
                                    .boundingBox(field.getBoundingBox())
                                    .build();

                            additionalList.add(prop);
                        }

                        log.info("Added {} AUTH_ADDL_KEYWORD entries", keywords.length);

                    } catch (Exception ex) {
                        log.error("Error while processing AUTH_ADDL_KEYWORD", ex);
                    }
                });
    }

    private void handleResponsibleArea(Map<String, ExtractedField> fieldMap,
                                      List<AdditionalProperties> additionalList) {

        fieldMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("responsible_area"))
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    try {
                        ExtractedField field = entry.getValue();

                        if (field != null && hasValue(field.getValue())) {
                            AdditionalProperties prop = AdditionalProperties.builder()
                                    .propName("SORTING_KEYWORD")
                                    .propValue(field.getValue())
                                    .page(field.getPage())
                                    .confidence(Double.valueOf(field.getConfidence()))
                                    .boundingBox(field.getBoundingBox())
                                    .build();

                            additionalList.add(prop);
                            log.info("Added SORTING_KEYWORD property for responsible_area");
                        }
                    } catch (Exception ex) {
                        log.error("Error processing responsible_area field: {}", entry.getKey(), ex);
                    }
                });
    }

    private void handleLevelOfCare(Map<String, ExtractedField> fieldMap,
                                   List<AdditionalProperties> additionalList) {
        List<Map.Entry<String, ExtractedField>> locEntries = fieldMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("level_of_care"))
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());

        for (Map.Entry<String, ExtractedField> entry : locEntries) {
            try {
                ExtractedField field = entry.getValue();

                if (field != null && hasValue(field.getValue())) {
                    String[] values = field.getValue().contains(",")
                            ? field.getValue().split(",")
                            : new String[]{field.getValue()};

                    doLevelOfCareEntries(entry, values, field, additionalList);
                }

            } catch (Exception ex) {
                log.error("Error processing level_of_care entry: {}", entry.getKey(), ex);
            }
        }
    }

    private void handleFaxReport(Map<String, ExtractedField> fieldMap,
                                 List<AdditionalProperties> additionalList) {
        try {
            ExtractedField faxField = fieldMap.get(FAX_REPORT_SOR_ITEM_NAME);

            if (faxField != null && hasValue(faxField.getValue())) {
                AdditionalProperties prop = AdditionalProperties.builder()
                        .propName("FAX_REPORT")
                        .propValue(faxField.getValue().toUpperCase())
                        .page(faxField.getPage())
                        .confidence(Double.valueOf(faxField.getConfidence()))
                        .boundingBox(faxField.getBoundingBox())
                        .build();
                additionalList.add(prop);
                log.info("Added FAX_REPORT property for Commercial");

            } else {
                log.info("FAX_REPORT missing or empty for Commercial case");
            }

        } catch (Exception ex) {
            log.error("Error while processing FAX_REPORT field", ex);
        }
    }

    private void doLevelOfCareEntries(Map.Entry<String, ExtractedField> entry,
                                     String[] levelOfCareValues,
                                     ExtractedField levelOfCareField,
                                     List<AdditionalProperties> additionalPropertiesList) {
        for (String value : levelOfCareValues) {
            value = value.trim();
            if (value.isEmpty()) {
                log.info("Skipped empty level_of_care entry for key: {}", entry.getKey());
                continue;
            }

            AdditionalProperties authKeywordProperty = AdditionalProperties.builder()
                    .propName("AUTH_KEYWORD")
                    .propValue(value)
                    .page(levelOfCareField.getPage())
                    .confidence(Double.valueOf(levelOfCareField.getConfidence()))
                    .boundingBox(levelOfCareField.getBoundingBox())
                    .build();

            additionalPropertiesList.add(authKeywordProperty);
            log.info("Added AUTH_KEYWORD property for level_of_care entry");
        }
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
