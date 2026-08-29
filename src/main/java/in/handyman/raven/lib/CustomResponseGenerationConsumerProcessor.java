package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.lib.custom.outbound.dao.MetadataContext;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.custom.outbound.model.CustomResponseOutputTable;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CustomResponseGenerationConsumerProcessor implements CoproProcessor.ConsumerProcess<CustomResponseGenerationConsumerProcessor.CustomResponseGenerationInput, CustomResponseOutputTable> {
    private static final String CUSTOM_JSON_GENERATION_STRUCTURE = "custom.json.generation.structure";
    private static final Pattern BARE_PLACEHOLDER_VALUE_PATTERN = Pattern.compile("(:\\s*)(\\$\\{[^}]+})");
    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;
    private final ObjectMapper objectMapper;

    public CustomResponseGenerationConsumerProcessor(final Logger log, final Marker aMarker, final ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    @Override
    public List<CustomResponseOutputTable> process(URL endpoint, CustomResponseGenerationInput entity) throws Exception {
        if (entity == null || entity.getPredictions() == null || entity.getPredictions().isEmpty()) {
            return Collections.emptyList();
        }

        List<PredictionDTO> predictionDTOList = entity.getPredictions();
        PredictionDTO first = predictionDTOList.get(0);
        String originId = entity.getOriginId();
        String metadata = first.getMetadataJson();
        Long groupId = first.getGroupId();
        Long tenantId = first.getTenantId();
        String batchId = first.getBatchId();
        String rootPipelineId = first.getRootPipelineId();
        Long parsedRootPipelineId = rootPipelineId != null ? Long.valueOf(rootPipelineId) : 0L;
        Integer parsedProcessId = rootPipelineId != null ? Integer.valueOf(rootPipelineId) : 0;

        String template = action.getContext().getOrDefault(CUSTOM_JSON_GENERATION_STRUCTURE, "{}");
        JsonNode generatedJson = generateCustomJson(template, predictionDTOList);
        String customResponseStr = objectMapper.writeValueAsString(generatedJson);
        String inboundTransactionId = extractInboundTransactionId(metadata);

        CustomResponseOutputTable out = CustomResponseOutputTable.builder()
                .processId(parsedProcessId)
                .groupId(groupId)
                .customResponse(customResponseStr)
                .originId(originId)
                .tenantId(tenantId)
                .rootPipelineId(parsedRootPipelineId)
                .status("SUCCESS")
                .stage("Custom Response Generation")
                .message("Custom Response generated successfully")
                .triggeredUrl("")
                .feature("Custom")
                .batchId(batchId)
                .inboundTransactionId(inboundTransactionId)
                .createdOn(LocalDateTime.now())
                .lastUpdatedOn(LocalDateTime.now())
                .build();

        log.debug(aMarker, "Custom response generated for originId {}", originId);
        return Collections.singletonList(out);
    }

    JsonNode generateCustomJson(String templateJson, List<PredictionDTO> predictions) throws Exception {
        JsonNode root = parseTemplateJsonSafely(templateJson);
        Map<String, List<PredictionDTO>> bySorItem = predictions.stream()
                .filter(p -> p.getSorItemName() != null)
                .collect(Collectors.groupingBy(p -> p.getSorItemName().toLowerCase()));
        Map<String, List<PredictionDTO>> byNormalizedSorItem = predictions.stream()
                .filter(p -> p.getSorItemName() != null)
                .collect(Collectors.groupingBy(p -> normalizeKey(p.getSorItemName())));
        JsonNode filled = fillNode(root, bySorItem, byNormalizedSorItem, false);
        JsonNode outbound = filled;
        if (filled.isObject()) {
            ObjectNode filledObject = (ObjectNode) filled;
            ObjectNode target = filledObject;
            if (filledObject.has("root") && filledObject.get("root").isObject()) {
                target = (ObjectNode) filledObject.get("root");
                if (filledObject.size() == 1) {
                    // Avoid returning nested {"root":{"root":...}} in outbound payloads.
                    outbound = target;
                }
            }
            enrichRootEnvelope(target, predictions);
        }
        return outbound;
    }

    private JsonNode parseTemplateJsonSafely(String templateJson) {
        String incomingTemplate = templateJson == null ? "" : templateJson.trim();
        if (incomingTemplate.isEmpty()) {
            log.warn(aMarker, "Custom response template is empty. Proceeding with empty object.");
            return objectMapper.createObjectNode();
        }

        String candidate = incomingTemplate;
        for (int i = 0; i < 3; i++) {
            try {
                JsonNode parsed = objectMapper.readTree(quoteBarePlaceholders(candidate));
                if (parsed == null || parsed.isNull()) {
                    return objectMapper.createObjectNode();
                }
                if (!parsed.isTextual()) {
                    return parsed;
                }
                String textValue = parsed.asText("").trim();
                if (textValue.isEmpty()) {
                    return objectMapper.createObjectNode();
                }
                if (action != null && action.getContext() != null && action.getContext().containsKey(textValue)) {
                    candidate = action.getContext().get(textValue);
                    if (candidate == null || candidate.trim().isEmpty()) {
                        return objectMapper.createObjectNode();
                    }
                    continue;
                }
                candidate = textValue;
            } catch (Exception ex) {
                log.error(aMarker, "Invalid custom response template JSON. Proceeding with empty object.", ex);
                return objectMapper.createObjectNode();
            }
        }
        return objectMapper.createObjectNode();
    }

    private JsonNode fillNode(JsonNode node, Map<String, List<PredictionDTO>> bySorItem, Map<String, List<PredictionDTO>> byNormalizedSorItem, boolean insideAumiPayload) {
        if (node == null || node.isNull()) {
            return node;
        }
        if (node.isObject()) {
            ObjectNode obj = ((ObjectNode) node).deepCopy();

            if (obj.has("value")) {
                return obj;
            }

            List<String> fieldNames = new ArrayList<>();
            obj.fieldNames().forEachRemaining(fieldNames::add);
            for (String fieldName : fieldNames) {
                JsonNode child = obj.get(fieldName);
                boolean childInsideAumiPayload = insideAumiPayload || "aumipayload".equalsIgnoreCase(fieldName);
                if (isLeafTemplateNode(child)) {
                    PredictionDTO match = firstPredictionForLeaf((ObjectNode) child, fieldName, bySorItem, byNormalizedSorItem);
                    if (match != null) {
                        ObjectNode populatedLeaf = populateLeaf((ObjectNode) child.deepCopy(), match);
                        if (childInsideAumiPayload && isLeafValueMissing(populatedLeaf)) {
                            obj.remove(fieldName);
                        } else {
                            obj.set(fieldName, populatedLeaf);
                        }
                    } else if (childInsideAumiPayload) {
                        obj.remove(fieldName);
                    }
                } else {
                    JsonNode filledChild = fillNode(child, bySorItem, byNormalizedSorItem, childInsideAumiPayload);
                    if (childInsideAumiPayload && shouldPruneNode(filledChild)) {
                        obj.remove(fieldName);
                    } else {
                        obj.set(fieldName, filledChild);
                    }
                }
            }

            maybeAssignProviderCategory(obj, node, bySorItem, insideAumiPayload);
            return obj;
        }

        if (node.isArray()) {
            ArrayNode inputArray = (ArrayNode) node;
            ArrayNode resultArray = objectMapper.createArrayNode();
            if (inputArray.isEmpty()) {
                return resultArray;
            }

            // Template-driven additional properties:
            // each item carries propName as { "value": "..." } or a string; propValue is filled from matching sorItemName.
            if (isTemplateDrivenPropertyArray(inputArray)) {
                for (JsonNode item : inputArray) {
                    ObjectNode itemCopy = (ObjectNode) item.deepCopy();
                    String propName = templateDrivenPropertyPropName(itemCopy);
                    if (!propName.isEmpty()) {
                        JsonNode propValueNode = itemCopy.get("propValue");
                        String placeholder = null;
                        if (propValueNode != null && propValueNode.isTextual()) {
                            placeholder = extractPlaceholder(propValueNode.asText(null));
                        }
                        if (shouldExpandTemplateDrivenPropertyPerPrediction(propName, placeholder)) {
                            List<PredictionDTO> allForPlaceholder = allPredictionsForField(placeholder, bySorItem, byNormalizedSorItem);
                            if (!allForPlaceholder.isEmpty()) {
                                for (PredictionDTO p : allForPlaceholder) {
                                    ObjectNode oneRow = (ObjectNode) item.deepCopy();
                                    applyTemplateDrivenPropertyMatch(oneRow, propName, p);
                                    if (!(insideAumiPayload && shouldPruneTemplateDrivenPropertyItem(oneRow))) {
                                        resultArray.add(oneRow);
                                    }
                                }
                                continue;
                            }
                        }
                        PredictionDTO match = null;
                        if (propValueNode != null && propValueNode.isTextual()) {
                            if (placeholder != null && !placeholder.isEmpty()) {
                                match = firstPrediction(placeholder, bySorItem, byNormalizedSorItem);
                            }
                        }
                        if (match == null) {
                            match = firstPrediction(propName, bySorItem, byNormalizedSorItem);
                        }
                        applyTemplateDrivenPropertyMatch(itemCopy, propName, match);
                    }
                    if (!(insideAumiPayload && shouldPruneTemplateDrivenPropertyItem(itemCopy))) {
                        resultArray.add(itemCopy);
                    }
                }
                return resultArray;
            }

            JsonNode templateItem = inputArray.get(0);
            if (!templateItem.isObject()) {
                inputArray.forEach(item -> {
                    JsonNode filledItem = fillNode(item, bySorItem, byNormalizedSorItem, insideAumiPayload);
                    if (!(insideAumiPayload && shouldPruneNode(filledItem))) {
                        resultArray.add(filledItem);
                    }
                });
                return resultArray;
            }

            Map<String, List<PredictionDTO>> groupedByInstance = new HashMap<>();
            List<String> placeholders = new ArrayList<>();
            collectLeafPlaceholders(templateItem, placeholders);
            placeholders.forEach(field -> {
                List<PredictionDTO> matches = allPredictionsForField(field, bySorItem, byNormalizedSorItem);
                for (PredictionDTO p : matches) {
                    String instanceKey = p.getSorContainerInstance() == null || p.getSorContainerInstance().trim().isEmpty()
                            ? "0" : p.getSorContainerInstance();
                    groupedByInstance.computeIfAbsent(instanceKey, k -> new ArrayList<>()).add(p);
                }
            });

            if (groupedByInstance.keySet().stream().allMatch("0"::equals)) {
                groupedByInstance.clear();
            }

            if (groupedByInstance.isEmpty()) {
                int expandedRows = expandedArrayRowCount(templateItem, bySorItem, byNormalizedSorItem);
                if (expandedRows > 1) {
                    for (int idx = 0; idx < expandedRows; idx++) {
                        JsonNode indexedItem = fillNodeForArrayIndex(templateItem.deepCopy(), bySorItem, byNormalizedSorItem, idx, insideAumiPayload);
                        if (!(insideAumiPayload && shouldPruneNode(indexedItem))) {
                            resultArray.add(indexedItem);
                        }
                    }
                    return resultArray;
                }
                JsonNode filledItem = fillNodeForArrayIndex(templateItem.deepCopy(), bySorItem, byNormalizedSorItem, 0, insideAumiPayload);
                if (!(insideAumiPayload && shouldPruneNode(filledItem))) {
                    resultArray.add(filledItem);
                }
                return resultArray;
            }

            groupedByInstance.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .forEach(entry -> {
                        Map<String, List<PredictionDTO>> scopedBySor = entry.getValue().stream()
                                .filter(p -> p.getSorItemName() != null)
                                .collect(Collectors.groupingBy(p -> p.getSorItemName().toLowerCase()));
                        Map<String, List<PredictionDTO>> scopedByNormalizedSor = entry.getValue().stream()
                                .filter(p -> p.getSorItemName() != null)
                                .collect(Collectors.groupingBy(p -> normalizeKey(p.getSorItemName())));
                        JsonNode filledItem = fillNode(templateItem.deepCopy(), scopedBySor, scopedByNormalizedSor, insideAumiPayload);
                        if (!(insideAumiPayload && shouldPruneNode(filledItem))) {
                            resultArray.add(filledItem);
                        }
                    });
            return resultArray;
        }
        return node;
    }

    private boolean isLeafTemplateNode(JsonNode node) {
        return node != null && node.isObject() && node.has("value");
    }

    private void maybeAssignProviderCategory(ObjectNode obj,
                                             JsonNode templateNode,
                                             Map<String, List<PredictionDTO>> bySorItem,
                                             boolean insideAumiPayload) {
        if (!insideAumiPayload) {
            return;
        }
        if (!(templateNode instanceof ObjectNode)) {
            return;
        }
        ObjectNode templateObj = (ObjectNode) templateNode;
        JsonNode providerCategoryTemplate = templateObj.get("providerCategory");
        if (!isLeafTemplateNode(providerCategoryTemplate)) {
            return;
        }
        if (obj.has("providerCategory")) {
            return;
        }
        if (!hasAnyAssignedProviderDetails(obj)) {
            return;
        }

        PredictionDTO categoryPrediction = deriveProviderCategoryPrediction(bySorItem);
        ObjectNode categoryLeaf = ((ObjectNode) providerCategoryTemplate).deepCopy();

        if (categoryPrediction != null && isNonBlank(categoryPrediction.getPredictedValue())) {
            categoryLeaf = populateLeaf(categoryLeaf, categoryPrediction);
        } else {
            // Keep providerCategory node when provider details exist, even if category cannot be derived.
            categoryLeaf.put("value", "");
        }

        if (!isLeafValueMissing(categoryLeaf) || hasAnyAssignedProviderDetails(obj)) {
            obj.set("providerCategory", categoryLeaf);
            moveFieldToFront(obj, "providerCategory");
        }
    }

    private boolean hasAnyAssignedProviderDetails(ObjectNode providerObj) {
        String[] detailFields = new String[]{
                "providerNPI", "providerTIN", "providerFirstName", "providerLastName",
                "providerAddressLine1", "providerAddressLine2", "providerCity",
                "providerState", "providerZipCode"
        };
        for (String field : detailFields) {
            JsonNode node = providerObj.get(field);
            if (node != null && node.isObject() && node.has("value") && isNonBlank(node.path("value").asText(""))) {
                return true;
            }
        }
        return false;
    }

    private void moveFieldToFront(ObjectNode obj, String fieldName) {
        if (obj == null || fieldName == null || !obj.has(fieldName)) {
            return;
        }
        JsonNode selected = obj.get(fieldName);
        ObjectNode reordered = objectMapper.createObjectNode();
        reordered.set(fieldName, selected);
        obj.fields().forEachRemaining(entry -> {
            if (!fieldName.equals(entry.getKey())) {
                reordered.set(entry.getKey(), entry.getValue());
            }
        });
        obj.removeAll();
        obj.setAll(reordered);
    }

    private int expandedArrayRowCount(JsonNode templateNode, Map<String, List<PredictionDTO>> bySorItem, Map<String, List<PredictionDTO>> byNormalizedSorItem) {
        List<String> placeholders = new ArrayList<>();
        collectLeafPlaceholders(templateNode, placeholders);
        int max = 1;
        for (String placeholder : placeholders) {
            int size = allPredictionsForField(placeholder, bySorItem, byNormalizedSorItem).size();
            if (size > max) {
                max = size;
            }
        }
        return max;
    }

    private JsonNode fillNodeForArrayIndex(JsonNode node,
                                           Map<String, List<PredictionDTO>> bySorItem,
                                           Map<String, List<PredictionDTO>> byNormalizedSorItem,
                                           int index,
                                           boolean insideAumiPayload) {
        if (node == null || node.isNull()) {
            return node;
        }
        if (node.isObject()) {
            ObjectNode obj = ((ObjectNode) node).deepCopy();
            if (obj.has("value")) {
                return obj;
            }
            List<String> fieldNames = new ArrayList<>();
            obj.fieldNames().forEachRemaining(fieldNames::add);
            for (String fieldName : fieldNames) {
                JsonNode child = obj.get(fieldName);
                boolean childInsideAumiPayload = insideAumiPayload || "aumipayload".equalsIgnoreCase(fieldName);
                if (isLeafTemplateNode(child)) {
                    ObjectNode leaf = (ObjectNode) child.deepCopy();
                    String placeholderKey = extractPlaceholder(leaf.path("value").asText(null));
                    String lookupKey = placeholderKey != null && !placeholderKey.isEmpty() ? placeholderKey : fieldName;
                    PredictionDTO match = predictionForFieldAtIndex(lookupKey, bySorItem, byNormalizedSorItem, index);
                    if (match != null) {
                        ObjectNode populatedLeaf = populateLeaf(leaf, match);
                        if (childInsideAumiPayload && isLeafValueMissing(populatedLeaf)) {
                            obj.remove(fieldName);
                        } else {
                            obj.set(fieldName, populatedLeaf);
                        }
                    } else if (childInsideAumiPayload) {
                        obj.remove(fieldName);
                    }
                } else {
                    JsonNode filledChild = fillNodeForArrayIndex(child, bySorItem, byNormalizedSorItem, index, childInsideAumiPayload);
                    if (childInsideAumiPayload && shouldPruneNode(filledChild)) {
                        obj.remove(fieldName);
                    } else {
                        obj.set(fieldName, filledChild);
                    }
                }
            }
            Map<String, List<PredictionDTO>> categoryScope = buildCategoryScopeForArrayRow(node, bySorItem, byNormalizedSorItem, index);
            maybeAssignProviderCategory(obj, node, categoryScope, insideAumiPayload);
            return obj;
        }
        if (node.isArray()) {
            ArrayNode inputArray = (ArrayNode) node;
            ArrayNode resultArray = objectMapper.createArrayNode();
            for (JsonNode item : inputArray) {
                JsonNode filledItem = fillNodeForArrayIndex(item, bySorItem, byNormalizedSorItem, index, insideAumiPayload);
                if (!(insideAumiPayload && shouldPruneNode(filledItem))) {
                    resultArray.add(filledItem);
                }
            }
            return resultArray;
        }
        return node;
    }

    private PredictionDTO predictionForFieldAtIndex(String fieldName,
                                                    Map<String, List<PredictionDTO>> bySorItem,
                                                    Map<String, List<PredictionDTO>> byNormalizedSorItem,
                                                    int index) {
        List<PredictionDTO> allMatches = allPredictionsForField(fieldName, bySorItem, byNormalizedSorItem);
        if (allMatches.isEmpty()) {
            return null;
        }
        if (index < allMatches.size()) {
            return allMatches.get(index);
        }
        return allMatches.get(allMatches.size() - 1);
    }

    private List<PredictionDTO> allPredictionsForField(String fieldName,
                                                       Map<String, List<PredictionDTO>> bySorItem,
                                                       Map<String, List<PredictionDTO>> byNormalizedSorItem) {
        List<PredictionDTO> all = new ArrayList<>();
        for (String candidateFieldName : candidateFieldNames(fieldName, bySorItem)) {
            List<PredictionDTO> direct = bySorItem.get(candidateFieldName.toLowerCase());
            if (direct != null) {
                all.addAll(direct);
            }
            List<PredictionDTO> normalized = byNormalizedSorItem.get(normalizeKey(candidateFieldName));
            if (normalized != null) {
                all.addAll(normalized);
            }
        }
        all = new ArrayList<>(new LinkedHashSet<>(all));
        Map<String, List<PredictionDTO>> byKey = all.stream().collect(Collectors.groupingBy(
                this::predictionDeduplicationKey,
                LinkedHashMap::new,
                Collectors.toList()));
        List<PredictionDTO> collapsed = new ArrayList<>();
        for (List<PredictionDTO> group : byKey.values()) {
            long nonBlankInGroup = group.stream().filter(p -> isNonBlank(p.getPredictedValue())).count();
            boolean mergeToSingleRow = group.size() == 1
                    || nonBlankInGroup == 0
                    || (nonBlankInGroup == 1 && group.size() > 1);
            if (mergeToSingleRow) {
                PredictionDTO best = chooseBestPrediction(group);
                if (best != null) {
                    collapsed.add(best);
                }
            } else {
                List<PredictionDTO> multi = new ArrayList<>(group);
                multi.sort(Comparator.comparing((PredictionDTO p) -> !isNonBlank(p.getPredictedValue())));
                collapsed.addAll(multi);
            }
        }
        return collapsed;
    }

    private String predictionDeduplicationKey(PredictionDTO p) {
        if (p == null || p.getSorItemName() == null) {
            return "\0";
        }
        String instance = p.getSorContainerInstance() == null ? "" : p.getSorContainerInstance().trim();
        return p.getSorItemName().toLowerCase(Locale.ROOT) + "\0" + instance;
    }

    /**
     * Predictions that belong to one expanded array row (same index), plus any rows that share a non-blank
     * {@link PredictionDTO#getSorContainerInstance()} with those picks. Used so {@code providerCategory}
     * reflects only that provider entry, not the entire payload.
     */
    private Map<String, List<PredictionDTO>> buildCategoryScopeForArrayRow(JsonNode rowTemplate,
                                                                           Map<String, List<PredictionDTO>> bySorItem,
                                                                           Map<String, List<PredictionDTO>> byNormalizedSorItem,
                                                                           int rowIndex) {
        List<String> placeholders = new ArrayList<>();
        collectLeafPlaceholders(rowTemplate, placeholders);
        LinkedHashSet<PredictionDTO> rowPredictions = new LinkedHashSet<>();
        for (String placeholder : placeholders) {
            PredictionDTO p = predictionForFieldAtIndex(placeholder, bySorItem, byNormalizedSorItem, rowIndex);
            if (p != null) {
                rowPredictions.add(p);
            }
        }
        enrichRowPredictionsWithSharedContainer(rowPredictions, bySorItem);
        Map<String, List<PredictionDTO>> scoped = new HashMap<>();
        for (PredictionDTO p : rowPredictions) {
            if (p.getSorItemName() == null) {
                continue;
            }
            String key = p.getSorItemName().toLowerCase(Locale.ROOT);
            scoped.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }
        return scoped;
    }

    private void enrichRowPredictionsWithSharedContainer(LinkedHashSet<PredictionDTO> rowPredictions,
                                                         Map<String, List<PredictionDTO>> bySorItem) {
        Set<String> instances = rowPredictions.stream()
                .map(p -> p.getSorContainerInstance() == null ? "" : p.getSorContainerInstance().trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (instances.isEmpty()) {
            return;
        }
        for (List<PredictionDTO> bucket : bySorItem.values()) {
            if (bucket == null) {
                continue;
            }
            for (PredictionDTO p : bucket) {
                if (p == null || p.getSorItemName() == null) {
                    continue;
                }
                String inst = p.getSorContainerInstance() == null ? "" : p.getSorContainerInstance().trim();
                if (!inst.isEmpty() && instances.contains(inst)) {
                    rowPredictions.add(p);
                }
            }
        }
    }

    private void collectLeafPlaceholders(JsonNode node, List<String> placeholders) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            if (node.has("value")) {
                String placeholder = extractPlaceholder(node.path("value").asText(null));
                if (placeholder != null && !placeholder.isEmpty()) {
                    placeholders.add(placeholder);
                }
                return;
            }
            node.fields().forEachRemaining(entry -> collectLeafPlaceholders(entry.getValue(), placeholders));
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collectLeafPlaceholders(child, placeholders));
        }
    }

    private PredictionDTO firstPrediction(String fieldName, Map<String, List<PredictionDTO>> bySorItem, Map<String, List<PredictionDTO>> byNormalizedSorItem) {
        if ("provider_category".equalsIgnoreCase(fieldName)) {
            // provider_category is not a source SOR item; derive it only from provider details.
            return deriveProviderCategoryPrediction(bySorItem);
        }
        for (String candidateFieldName : candidateFieldNames(fieldName, bySorItem)) {
            List<PredictionDTO> matches = bySorItem.get(candidateFieldName.toLowerCase());
            if (matches != null && !matches.isEmpty()) {
                return chooseBestPrediction(matches);
            }
            List<PredictionDTO> normalizedMatches = byNormalizedSorItem.get(normalizeKey(candidateFieldName));
            if (normalizedMatches != null && !normalizedMatches.isEmpty()) {
                return chooseBestPrediction(normalizedMatches);
            }
        }
        return null;
    }

    private PredictionDTO deriveProviderCategoryPrediction(Map<String, List<PredictionDTO>> bySorItem) {
        String category = detectConsistentProviderCategory(bySorItem);
        if (category == null) {
            category = firstPopulatedProviderCategory(bySorItem);
        }
        if (category != null) {
            PredictionDTO base = bestPredictionForProviderCategory(bySorItem, category);
            return providerCategoryPrediction(base, category);
        }
        return null;
    }

    /**
     * Outbound {@code providerCategory.value} tokens derived from {@code sorItemName} prefix
     * (e.g. {@code service_provider_npi} → {@code service_provider}).
     */
    private String providerCategoryFromSorItemKey(String sorItemKey) {
        if (sorItemKey == null) {
            return null;
        }
        String k = sorItemKey.toLowerCase(Locale.ROOT);
        if (k.startsWith("service_provider_") || k.startsWith("servicing_provider_")) {
            return "Servicing Provider";
        }
        if (k.startsWith("servicing_facility_")) {
            return "Servicing Facility";
        }
        if (k.startsWith("ordering_provider_")) {
            return "Ordering Provider";
        }
        if (k.startsWith("undefined_provider_")) {
            return "Undetermined";
        }
        if (k.startsWith("referring_provider_")) {
            return "Requesting Provider";
        }
        return null;
    }

    private boolean isProviderDetailSorKey(String sorItemKey) {
        if (sorItemKey == null) {
            return false;
        }
        String k = sorItemKey.toLowerCase(Locale.ROOT);
        String[] prefixes = new String[]{
                "service_provider_",
                "servicing_provider_",
                "servicing_facility_",
                "referring_provider_",
                "ordering_provider_",
                "undefined_provider_"
        };
        String[] providerSuffixes = new String[]{
                "npi", "tin", "first_name", "last_name", "address_line1", "address_line_1", "city", "state", "zipcode", "zip_code"
        };
        for (String prefix : prefixes) {
            if (!k.startsWith(prefix)) {
                continue;
            }
            String suffix = k.substring(prefix.length());
            for (String providerSuffix : providerSuffixes) {
                if (providerSuffix.equals(suffix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String detectConsistentProviderCategory(Map<String, List<PredictionDTO>> bySorItem) {
        Set<String> categories = new LinkedHashSet<>();
        for (Map.Entry<String, List<PredictionDTO>> entry : bySorItem.entrySet()) {
            String key = entry.getKey();
            if (!isProviderDetailSorKey(key)) {
                continue;
            }
            String category = providerCategoryFromSorItemKey(key);
            if (category == null) {
                continue;
            }
            PredictionDTO best = chooseBestPrediction(entry.getValue());
            if (best != null && isNonBlank(best.getPredictedValue())) {
                categories.add(category);
            }
        }
        if (categories.isEmpty()) {
            return detectConsistentProviderCategoryFromContainer(bySorItem);
        }
        if (categories.size() == 1) {
            return categories.iterator().next();
        }
        return null;
    }

    private String detectConsistentProviderCategoryFromContainer(Map<String, List<PredictionDTO>> bySorItem) {
        List<String> detected = new ArrayList<>();
        for (List<PredictionDTO> predictions : bySorItem.values()) {
            if (predictions == null || predictions.isEmpty()) {
                continue;
            }
            PredictionDTO best = chooseBestPrediction(predictions);
            String category = providerCategoryByContainerName(best != null ? best.getContainerName() : null);
            if (category != null) {
                detected.add(category);
            }
        }
        List<String> unique = detected.stream().distinct().collect(Collectors.toList());
        return unique.size() == 1 ? unique.get(0) : null;
    }

    private String firstPopulatedProviderCategory(Map<String, List<PredictionDTO>> bySorItem) {
        String[] priority = new String[]{
                "service_provider",
                "servicing_facility",
                "ordering_provider",
                "undefined_providers",
                "referring_provider"
        };
        for (String target : priority) {
            for (Map.Entry<String, List<PredictionDTO>> entry : bySorItem.entrySet()) {
                String key = entry.getKey();
                if (!target.equals(providerCategoryFromSorItemKey(key)) || !isProviderDetailSorKey(key)) {
                    continue;
                }
                PredictionDTO best = chooseBestPrediction(entry.getValue());
                if (best != null && isNonBlank(best.getPredictedValue())) {
                    return target;
                }
            }
        }
        return detectConsistentProviderCategoryFromContainer(bySorItem);
    }

    private String detectProviderPrefix(Map<String, List<PredictionDTO>> bySorItem) {
        String category = detectConsistentProviderCategory(bySorItem);
        if (category == null) {
            category = firstPopulatedProviderCategory(bySorItem);
        }
        return firstSorPrefixForCategory(bySorItem, category);
    }

    private String firstSorPrefixForCategory(Map<String, List<PredictionDTO>> bySorItem, String category) {
        if (category == null) {
            return null;
        }
        List<String> prefixes = new ArrayList<>();
        switch (category) {
            case "service_provider":
                prefixes.add("service_provider_");
                prefixes.add("servicing_provider_");
                break;
            case "servicing_facility":
                prefixes.add("servicing_facility_");
                break;
            case "ordering_provider":
                prefixes.add("ordering_provider_");
                break;
            case "undefined_providers":
                prefixes.add("undefined_provider_");
                break;
            case "referring_provider":
                prefixes.add("referring_provider_");
                break;
            default:
                return null;
        }
        for (String prefix : prefixes) {
            PredictionDTO p = bestPredictionForProviderPrefix(bySorItem, prefix);
            if (p != null && isNonBlank(p.getPredictedValue())) {
                return prefix;
            }
        }
        return null;
    }

    private PredictionDTO bestPredictionForProviderCategory(Map<String, List<PredictionDTO>> bySorItem, String category) {
        PredictionDTO best = null;
        for (Map.Entry<String, List<PredictionDTO>> entry : bySorItem.entrySet()) {
            String key = entry.getKey();
            if (!category.equals(providerCategoryFromSorItemKey(key)) || !isProviderDetailSorKey(key)) {
                continue;
            }
            PredictionDTO candidate = chooseBestPrediction(entry.getValue());
            if (candidate == null) {
                continue;
            }
            if (best == null) {
                best = candidate;
                continue;
            }
            best = chooseBestPrediction(List.of(best, candidate));
        }
        return best;
    }

    private PredictionDTO bestPredictionForProviderPrefix(Map<String, List<PredictionDTO>> bySorItem, String prefix) {
        PredictionDTO best = null;
        for (Map.Entry<String, List<PredictionDTO>> entry : bySorItem.entrySet()) {
            String key = entry.getKey();
            if (key == null || !key.startsWith(prefix)) {
                continue;
            }
            PredictionDTO candidate = chooseBestPrediction(entry.getValue());
            if (candidate == null) {
                continue;
            }
            if (best == null) {
                best = candidate;
                continue;
            }
            best = chooseBestPrediction(List.of(best, candidate));
        }
        return best;
    }

    private String providerCategoryByContainerName(String containerName) {
        if (containerName == null || containerName.trim().isEmpty()) {
            return null;
        }
        switch (containerName.trim().toUpperCase(Locale.ROOT)) {
            case "SERVICING_PROVIDER_DETAILS":
                return "service_provider";
            case "SERVICING_FACILITY_DETAILS":
                return "servicing_facility";
            case "REFERRING_PROVIDER_DETAILS":
                return "referring_provider";
            case "ORDERING_PROVIDER_DETAILS":
                return "ordering_provider";
            case "UNDEFINED_PROVIDER_DETAILS":
                return "undefined_providers";
            default:
                return null;
        }
    }

    private PredictionDTO providerCategoryPrediction(PredictionDTO base, String category) {
        return PredictionDTO.builder()
                .predictedValue(category)
                .paperNo(base != null ? base.getPaperNo() : 0)
                .precision(base != null && base.getPrecision() != null ? base.getPrecision() : 1.0)
                .leftPos(base != null && base.getLeftPos() != null ? base.getLeftPos() : 0.0)
                .rightPos(base != null && base.getRightPos() != null ? base.getRightPos() : 0.0)
                .upperPos(base != null && base.getUpperPos() != null ? base.getUpperPos() : 0.0)
                .lowerPos(base != null && base.getLowerPos() != null ? base.getLowerPos() : 0.0)
                .build();
    }

    private PredictionDTO firstPredictionForLeaf(ObjectNode leafNode, String fieldName,
                                                 Map<String, List<PredictionDTO>> bySorItem,
                                                 Map<String, List<PredictionDTO>> byNormalizedSorItem) {
        String placeholderKey = extractPlaceholder(leafNode.path("value").asText(null));
        if (placeholderKey != null && !placeholderKey.isEmpty()) {
            PredictionDTO matchByPlaceholder = firstPrediction(placeholderKey, bySorItem, byNormalizedSorItem);
            if (matchByPlaceholder != null) {
                return matchByPlaceholder;
            }
        }
        return firstPrediction(fieldName, bySorItem, byNormalizedSorItem);
    }

    private ObjectNode populateLeaf(ObjectNode leaf, PredictionDTO p) {
        leaf.put("value", p.getPredictedValue() == null ? "" : p.getPredictedValue());
        if (leaf.has("page")) {
            leaf.put("page", p.getPaperNo() == null ? 0 : p.getPaperNo());
        }
        if (leaf.has("confidence")) {
            int confidence = p.getPrecision() == null ? 0 : (int) Math.round(p.getPrecision() * 100);
            leaf.put("confidence", confidence);
        }
        if (leaf.has("boundingBox") && leaf.get("boundingBox").isObject()) {
            ObjectNode bbox = (ObjectNode) leaf.get("boundingBox");
            bbox.put("x", toCoord(p.getLeftPos()));
            bbox.put("width", toCoord(p.getRightPos()));
            bbox.put("y", toCoord(p.getUpperPos()));
            bbox.put("height", toCoord(p.getLowerPos()));
            leaf.set("boundingBox", bbox);
        }
        return leaf;
    }

    private boolean isLeafValueMissing(ObjectNode leaf) {
        if (leaf == null || !leaf.has("value")) {
            return true;
        }
        String value = leaf.path("value").asText("");
        return value.trim().isEmpty() || extractPlaceholder(value) != null;
    }

    private boolean shouldPruneNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return true;
        }
        if (node.isObject()) {
            return node.size() == 0;
        }
        if (node.isArray()) {
            return node.size() == 0;
        }
        return false;
    }

    private boolean shouldPruneTemplateDrivenPropertyItem(ObjectNode item) {
        JsonNode propValueNode = item.get("propValue");
        if (propValueNode == null || propValueNode.isNull()) {
            return true;
        }
        if (propValueNode.isTextual()) {
            String value = propValueNode.asText("");
            return value.trim().isEmpty() || extractPlaceholder(value) != null;
        }
        if (propValueNode.isObject() && propValueNode.has("value")) {
            return isLeafValueMissing((ObjectNode) propValueNode);
        }
        return false;
    }

    private String defaultTemplateDrivenPropertyValue(String propName) {
        if ("MEMBER_INDICATOR".equalsIgnoreCase(propName)) {
            return "N";
        }
        return "";
    }

    /**
     * When the template carries multiple predictions for the same placeholder (e.g. several
     * {@code additional_auth_properties} rows), emit one JSON object per prediction instead of collapsing
     * to {@link #firstPrediction}.
     */
    private boolean shouldExpandTemplateDrivenPropertyPerPrediction(String propName, String placeholder) {
        if (placeholder == null || placeholder.isEmpty()) {
            return false;
        }
        if ("AUTH_ADDL_KEYWORD".equalsIgnoreCase(propName) && "additional_auth_properties".equalsIgnoreCase(placeholder)) {
            return true;
        }
        return "SORTING_KEY".equalsIgnoreCase(propName) && "responsible_area".equalsIgnoreCase(placeholder);
    }

    private void applyTemplateDrivenPropertyMatch(ObjectNode itemCopy, String propName, PredictionDTO match) {
        JsonNode propValueNode = itemCopy.get("propValue");
        if (propValueNode != null && propValueNode.isObject() && ((ObjectNode) propValueNode).has("value")) {
            ObjectNode populated = populateLeaf((ObjectNode) propValueNode.deepCopy(), match != null ? match : PredictionDTO.builder().build());
            if (match == null) {
                populated.put("value", defaultTemplateDrivenPropertyValue(propName));
            }
            itemCopy.set("propValue", populated);
        } else if (match != null) {
            itemCopy.put("propValue", match.getPredictedValue() == null ? "" : match.getPredictedValue());
            itemCopy.put("page", match.getPaperNo() == null ? 0 : match.getPaperNo());
            int confidence = match.getPrecision() == null ? 0 : (int) Math.round(match.getPrecision() * 100);
            itemCopy.put("confidence", confidence);
            ObjectNode bbox = objectMapper.createObjectNode();
            bbox.put("x", toCoord(match.getLeftPos()));
            bbox.put("width", toCoord(match.getRightPos()));
            bbox.put("y", toCoord(match.getUpperPos()));
            bbox.put("height", toCoord(match.getLowerPos()));
            itemCopy.set("boundingBox", bbox);
        } else if (propValueNode != null && propValueNode.isTextual()) {
            itemCopy.put("propValue", defaultTemplateDrivenPropertyValue(propName));
        }
    }

    private String templateDrivenPropertyPropName(ObjectNode item) {
        JsonNode propName = item.get("propName");
        if (propName == null || propName.isNull()) {
            return "";
        }
        if (propName.isTextual()) {
            return propName.asText("").trim();
        }
        if (propName.isObject()) {
            return propName.path("value").asText("").trim();
        }
        return "";
    }

    private String extractInboundTransactionId(String metadataJson) {
        if (metadataJson == null || metadataJson.trim().isEmpty()) {
            return null;
        }
        try {
            MetadataContext metadataContext = objectMapper.readValue(metadataJson, MetadataContext.class);
            return metadataContext.getInboundTransactionId();
        } catch (Exception e) {
            log.warn(aMarker, "Failed to parse metadata for inboundTransactionId", e);
            return null;
        }
    }
    
    private boolean isTemplateDrivenPropertyArray(ArrayNode inputArray) {
        for (JsonNode item : inputArray) {
            if (!item.isObject()) {
                return false;
            }
            JsonNode propName = item.get("propName");
            if (propName == null || propName.isNull()) {
                return false;
            }
            if (propName.isTextual()) {
                if (propName.asText("").trim().isEmpty()) {
                    return false;
                }
            } else if (propName.isObject() && propName.has("value")) {
                // ok
            } else {
                return false;
            }
            JsonNode propValue = item.get("propValue");
            if (propValue == null) {
                return false;
            }
        }
        return inputArray.size() > 0;
    }

    private void enrichRootEnvelope(ObjectNode root, List<PredictionDTO> predictions) {
        if (predictions == null || predictions.isEmpty()) {
            return;
        }
        PredictionDTO first = predictions.get(0);
        MetadataContext metadataContext = parseMetadataFromPredictions(predictions);

        if (root.has("requestTxnId")) {
            String requestTxnId = metadataContext != null ? metadataContext.getRequestTxnId() : null;
            if (requestTxnId == null || requestTxnId.isEmpty()) {
                requestTxnId = first.getTransactionId();
            }
            if (requestTxnId != null) {
                root.put("requestTxnId", requestTxnId);
            }
        }
        if (root.has("status")) {
            root.put("status", "SUCCESS");
        }
        if (root.has("errorMessage")) {
            root.putNull("errorMessage");
        }
        if (root.has("errorMessageDetail")) {
            root.putNull("errorMessageDetail");
        }
        if (root.has("errorCd")) {
            root.putNull("errorCd");
        }
        if (root.has("documentId")) {
            String documentId = metadataContext != null ? metadataContext.getDocumentId() : null;
            if (documentId == null || documentId.isEmpty()) {
                documentId = first.getOriginId();
            }
            if (documentId != null) {
                root.put("documentId", documentId);
            }
        }
        if (root.has("inboundTransactionId")) {
            String inboundTransactionId = metadataContext != null ? metadataContext.getInboundTransactionId() : null;
            if ((inboundTransactionId == null || inboundTransactionId.isEmpty()) && first.getTransactionId() != null) {
                inboundTransactionId = first.getTransactionId();
            }
            if (inboundTransactionId != null) {
                root.put("inboundTransactionId", inboundTransactionId);
            }
        }

        if (root.has("metadata") && root.get("metadata").isObject()) {
            ObjectNode metadata = (ObjectNode) root.get("metadata");
            if (metadataContext != null) {
                if (metadata.has("documentType") && metadataContext.getDocumentType() != null) {
                    metadata.put("documentType", metadataContext.getDocumentType());
                }
                if (metadata.has("documentExtension") && metadataContext.getDocumentExtension() != null) {
                    metadata.put("documentExtension", metadataContext.getDocumentExtension());
                }
                if (metadata.has("transactionId") && metadataContext.getTransactionId() != null) {
                    metadata.put("transactionId", metadataContext.getTransactionId());
                } else if (metadata.has("transactionId") && first.getTransactionId() != null) {
                    metadata.put("transactionId", first.getTransactionId());
                }
                if (metadata.has("inboundDocumentName") && metadataContext.getInboundDocumentName() != null) {
                    metadata.put("inboundDocumentName", metadataContext.getInboundDocumentName());
                }
                if (metadata.has("processStartTime") && metadataContext.getProcessStartTime() != null) {
                    metadata.put("processStartTime", metadataContext.getProcessStartTime());
                }
                if (metadata.has("processEndTime") && metadataContext.getProcessEndTime() != null) {
                    metadata.put("processEndTime", metadataContext.getProcessEndTime());
                }
                if (metadata.has("processedAt") && metadataContext.getProcessedAt() != null) {
                    metadata.put("processedAt", metadataContext.getProcessedAt());
                }
                if (metadata.has("candidatePaper")) {
                    ArrayNode candidate = objectMapper.createArrayNode();
                    if (metadataContext.getCandidatePapers() != null) {
                        metadataContext.getCandidatePapers().forEach(candidate::add);
                    }
                    metadata.set("candidatePaper", candidate);
                }
                if (metadata.has("processingTimeMs") && metadataContext.getProcessStartTime() != null && metadataContext.getProcessEndTime() != null) {
                    try {
                        LocalDateTime s = LocalDateTime.parse(metadataContext.getProcessStartTime());
                        LocalDateTime e = LocalDateTime.parse(metadataContext.getProcessEndTime());
                        metadata.put("processingTimeMs", Duration.between(s, e).toMillis());
                    } catch (Exception ignored) {
                    }
                }
            }
            if (metadata.has("pageCount")) {
                int pageCount = predictions.stream()
                        .map(PredictionDTO::getPaperNo)
                        .filter(p -> p != null)
                        .max(Integer::compareTo)
                        .orElse(0);
                metadata.put("pageCount", pageCount);
            }
            if (metadata.has("overallConfidence")) {
                int overall = (int) Math.round(predictions.stream()
                        .map(PredictionDTO::getPrecision)
                        .filter(p -> p != null)
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0) * 100);
                metadata.put("overallConfidence", overall);
            }
        }
    }

    private MetadataContext parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.trim().isEmpty()) {
            return null;
        }
        String candidate = metadataJson.trim();
        for (int i = 0; i < 3; i++) {
            try {
                JsonNode node = objectMapper.readTree(candidate);
                if (node == null || node.isNull()) {
                    return null;
                }
                if (node.isTextual()) {
                    candidate = node.asText("").trim();
                    if (candidate.isEmpty()) {
                        return null;
                    }
                    continue;
                }
                return objectMapper.treeToValue(node, MetadataContext.class);
            } catch (Exception ignored) {
                break;
            }
        }
        try {
            return objectMapper.readValue(metadataJson, MetadataContext.class);
        } catch (Exception e) {
            return null;
        }
    }

    private MetadataContext parseMetadataFromPredictions(List<PredictionDTO> predictions) {
        for (PredictionDTO prediction : predictions) {
            if (prediction == null) {
                continue;
            }
            MetadataContext parsed = parseMetadata(prediction.getMetadataJson());
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private String extractPlaceholder(String valueTemplate) {
        if (valueTemplate == null) {
            return null;
        }
        String trimmed = valueTemplate.trim();
        if (trimmed.startsWith("${") && trimmed.endsWith("}") && trimmed.length() > 3) {
            return trimmed.substring(2, trimmed.length() - 1).trim();
        }
        return null;
    }

    private String normalizeKey(String key) {
        if (key == null) {
            return "";
        }
        return key.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }

    private List<String> candidateFieldNames(String fieldName, Map<String, List<PredictionDTO>> bySorItem) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String trimmed = fieldName.trim();
        List<String> candidates = new ArrayList<>();
        candidates.add(trimmed);
        candidates.add(trimmed.replace("_line_1", "_line1").replace("_zip_code", "_zipcode"));
        candidates.add(trimmed.replace("_line1", "_line_1").replace("_zipcode", "_zip_code"));
        if (trimmed.startsWith("provider_")) {
            String suffix = trimmed.substring("provider_".length());
            String preferredPrefix = detectProviderPrefix(bySorItem);
            if (preferredPrefix != null) {
                candidates.add(preferredPrefix + suffix);
            }
            candidates.add("service_provider_" + suffix);
            candidates.add("servicing_provider_" + suffix);
            candidates.add("servicing_facility_" + suffix);
            candidates.add("referring_provider_" + suffix);
            candidates.add("ordering_provider_" + suffix);
            candidates.add("undefined_provider_" + suffix);
        }
        if ("service_modifier".equals(trimmed)) {
            candidates.add("service_code_modifier");
        }
        if ("service_unit".equals(trimmed)) {
            candidates.add("service_quantity_units");
        }
        if ("service_visit".equals(trimmed)) {
            candidates.add("service_quantity_visits");
        }
        return candidates.stream().distinct().collect(Collectors.toList());
    }

    private PredictionDTO chooseBestPrediction(List<PredictionDTO> matches) {
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        return matches.stream()
                .min(Comparator
                        .comparing((PredictionDTO p) -> !isNonBlank(p.getPredictedValue()))
                        .thenComparing(p -> p.getPrecision() == null ? 0.0 : p.getPrecision(), Comparator.reverseOrder())
                        .thenComparing(p -> !hasAnyBoundingBoxCoordinate(p)))
                .orElse(matches.get(0));
    }

    private static double toCoord(Double value) {
        return value == null ? 0.0 : value;
    }

    private boolean isNonBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean hasAnyBoundingBoxCoordinate(PredictionDTO prediction) {
        return prediction.getLeftPos() != null
                || prediction.getRightPos() != null
                || prediction.getUpperPos() != null
                || prediction.getLowerPos() != null;
    }

    private String quoteBarePlaceholders(String rawJson) {
        if (rawJson == null || rawJson.isEmpty()) {
            return rawJson;
        }
        Matcher matcher = BARE_PLACEHOLDER_VALUE_PATTERN.matcher(rawJson);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replacement = matcher.group(1) + "\"" + matcher.group(2) + "\"";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class CustomResponseGenerationInput {
        private String originId;
        private List<PredictionDTO> predictions;
    }

}
