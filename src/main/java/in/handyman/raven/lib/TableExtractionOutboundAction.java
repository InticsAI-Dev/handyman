package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TableExtractionOutbound;
import in.handyman.raven.lib.model.tableextraction.outboud.TableExtractionAggregatedJson;
import in.handyman.raven.lib.model.tableextraction.outboud.TableExtractionOutboundInput;
import in.handyman.raven.lib.model.tableextraction.outboud.TableExtractionOutboundOutput;
import in.handyman.raven.util.CommonQueryUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "TableExtractionOutbound"
)
public class TableExtractionOutboundAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final TableExtractionOutbound tableExtractionOutbound;

    private final Marker aMarker;

    private Jdbi jdbi;

    public TableExtractionOutboundAction(final ActionExecutionAudit action, final Logger log,
                                         final Object tableExtractionOutbound) {
        this.tableExtractionOutbound = (TableExtractionOutbound) tableExtractionOutbound;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" TableExtractionOutbound:" + this.tableExtractionOutbound.getName());
    }

    @Override
    public void execute() throws Exception {

        log.info(aMarker, "Table extraction outbound Action has been started {}", tableExtractionOutbound);

        jdbi = ResourceAccess.rdbmsJDBIConn(tableExtractionOutbound.getResourceConn());
        jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
        final List<TableExtractionOutboundInput> tableInfos = new ArrayList<>();
        jdbi.useTransaction(handle -> {
            final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(tableExtractionOutbound.getQuerySet());
            AtomicInteger i = new AtomicInteger(0);
            formattedQuery.forEach(sqlToExecute -> {
                log.info(aMarker, "executing  query {} from index {}", sqlToExecute, i.getAndIncrement());
                Query query = handle.createQuery(sqlToExecute);
                ResultIterable<TableExtractionOutboundInput> resultIterable = query.mapToBean(TableExtractionOutboundInput.class);
                List<TableExtractionOutboundInput> detailList = resultIterable.stream().collect(Collectors.toList());
                tableInfos.addAll(detailList);
                log.info(aMarker, "executed query from index {}", i.get());
            });
        });

        List<TableExtractionOutboundOutput> tableExtractionOutboundOutputs = new ArrayList<>();

        if (tableExtractionOutbound.getInputAttribution().equals("csv")) {
            extractOutboundDateFromCsv(jdbi, tableInfos, tableExtractionOutboundOutputs);

        } else if (tableExtractionOutbound.getInputAttribution().equals("Json")) {
            extractOutboundDateFromJson(jdbi, tableInfos, tableExtractionOutboundOutputs);
        }


    }


    public void extractOutboundDateFromCsv(final Jdbi jdbi, final List<TableExtractionOutboundInput> tableInfos, List<TableExtractionOutboundOutput> tableExtractionOutboundOutputs) {
        tableInfos.forEach(tableExtractionOutboundInput -> {
            final String csvFilePathStr = tableExtractionOutboundInput.getProcessedFilePath();

            String columnHeader = tableExtractionOutboundInput.getColumnHeader();

            try {
                Long totalRowCount = extractRowCountBuffer(csvFilePathStr);
                log.info("Csv file path {} column header {} and total row count {}", csvFilePathStr, columnHeader, totalRowCount);
                if (!columnHeader.isBlank() && !columnHeader.isEmpty() && totalRowCount != 0) {

                    double sumOfColumnValue = extractedFromFilepath(csvFilePathStr, columnHeader, totalRowCount);

                    TableExtractionAggregatedJson aggregatedJson = aggregatedJsonBuilder(columnHeader, sumOfColumnValue, tableExtractionOutboundInput.getTableAggregateFunction(), tableExtractionOutboundInput.getSorItemId());

                    ObjectMapper objectMapper = new ObjectMapper();
                    String aggregatedJsonStr = objectMapper.writeValueAsString(aggregatedJson);
                    TableExtractionOutboundOutput tableExtractionOutboundOutput = transformInputOutput(tableExtractionOutboundInput, aggregatedJsonStr);
                    tableExtractionOutboundOutputs.add(tableExtractionOutboundOutput);
                } else {
                    log.info("column header is missing in the query set {}", columnHeader);
                }

            } catch (Exception e) {
                HandymanException exception = new HandymanException(e);
                exception.insertException("Error in processing the csv file path", exception, action);
            }


        });
        if (!tableExtractionOutboundOutputs.isEmpty()) {
            consumerBatch(jdbi, tableExtractionOutboundOutputs);
        }

    }

    public void consumerBatch(final Jdbi jdbi, List<TableExtractionOutboundOutput> tableExtractionOutboundOutputs) {
        try {
            jdbi.useTransaction(handle -> {
                final PreparedBatch batch = handle.prepareBatch("INSERT INTO " + tableExtractionOutbound.getResultTable() + " (process_id, group_id, tenant_id, template_id, origin_id, paper_no, processed_file_path, table_response, status, stage, message, created_on, root_pipeline_id, bboxes, croppedimage, column_headers, truth_entity_name, model_name, aggregated_json,sor_item_id,table_aggregate_function) " +
                        "VALUES(:process_id, :group_id, :tenant_id, :template_id, :origin_id, :paper_no, :processed_file_path, :table_response, :status, " +
                        ":stage, :message, :created_on, :root_pipeline_id, :bboxes, :croppedimage, :column_headers, :truth_entity_name, :model_name, :aggregatedJson, :sorItemId, :tableAggregateFunction);");

                Lists.partition(tableExtractionOutboundOutputs, 100).forEach(resultLineItems -> {
                    log.info(aMarker, "inserting into trinity model_action {}", resultLineItems.size());
                    resultLineItems.forEach(resultLineItem -> batch.bind("process_id", resultLineItem.getProcessId()).bind("group_id", resultLineItem.getGroupId())
                            .bind("tenant_id", resultLineItem.getTenantId()).bind("template_id", resultLineItem.getTemplateId()).bind("origin_id", resultLineItem.getOriginId())
                            .bind("paper_no", resultLineItem.getPaperNo()).bind("processed_file_path", resultLineItem.getProcessedFilePath()).bind("table_response", resultLineItem.getTableResponse())
                            .bind("status", resultLineItem.getStatus()).bind("stage", resultLineItem.getStage()).bind("message", resultLineItem.getMessage())
                            .bind("created_on", resultLineItem.getCreatedOn()).bind("root_pipeline_id", resultLineItem.getRootPipelineId()).bind("bboxes", resultLineItem.getBboxes())
                            .bind("croppedimage", resultLineItem.getCroppedimage()).bind("column_headers", resultLineItem.getColumnHeader()).bind("truth_entity_name", resultLineItem.getTruthEntityName())
                            .bind("model_name", resultLineItem.getModelName())
                            .bind("aggregatedJson", resultLineItem.getAggregatedJson())
                            .bind("tableAggregateFunction", resultLineItem.getTableAggregateFunction())
                            .bind("sorItemId", resultLineItem.getSorItemId())

                            .add());
                    int[] counts = batch.execute();
                    log.info(aMarker, " persisted {} in trinity model_action", counts);
                });
            });
        } catch (Exception e) {
            insertSummaryAudit(jdbi, 0, 0, tableExtractionOutboundOutputs.size(), "failed in batch insert", action.getRootPipelineId());
            HandymanException exception = new HandymanException(e);
            exception.insertException("error inserting result " + tableExtractionOutboundOutputs, exception, action);
        }
    }


    public void consumerBatchFailed(final Jdbi jdbi, List<TableExtractionOutboundOutput> tableExtractionOutboundOutputs) {
        try {
            jdbi.useTransaction(handle -> {
                final PreparedBatch batch = handle.prepareBatch("INSERT INTO " + tableExtractionOutbound.getResultTable() + " (process_id, group_id, tenant_id, template_id, origin_id, paper_no, processed_file_path, table_response, status, stage, message, created_on, root_pipeline_id, bboxes, croppedimage, column_headers, truth_entity_name, model_name, aggregated_json,sor_item_id) " +
                        "VALUES(:process_id, :group_id, :tenant_id, :template_id, :origin_id, :paper_no, :processed_file_path, :table_response, :status, " +
                        ":stage, :message, :created_on, :root_pipeline_id, :bboxes, :croppedimage, :column_headers, :truth_entity_name, :model_name, :aggregatedJson, :sorItemId);");

                Lists.partition(tableExtractionOutboundOutputs, 100).forEach(resultLineItems -> {
                    log.info(aMarker, "inserting into trinity model_action {}", resultLineItems.size());
                    resultLineItems.forEach(resultLineItem -> batch.bind("process_id", resultLineItem.getProcessId()).bind("group_id", resultLineItem.getGroupId())
                            .bind("tenant_id", resultLineItem.getTenantId()).bind("template_id", resultLineItem.getTemplateId()).bind("origin_id", resultLineItem.getOriginId())
                            .bind("paper_no", resultLineItem.getPaperNo()).bind("processed_file_path", resultLineItem.getProcessedFilePath()).bind("table_response", resultLineItem.getTableResponse())
                            .bind("status", resultLineItem.getStatus()).bind("stage", resultLineItem.getStage()).bind("message", resultLineItem.getMessage())
                            .bind("created_on", resultLineItem.getCreatedOn()).bind("root_pipeline_id", resultLineItem.getRootPipelineId()).bind("bboxes", resultLineItem.getBboxes())
                            .bind("croppedimage", resultLineItem.getCroppedimage()).bind("column_headers", resultLineItem.getColumnHeader()).bind("truth_entity_name", resultLineItem.getTruthEntityName())
                            .bind("model_name", resultLineItem.getModelName())
                            .bind("aggregatedJson", resultLineItem.getAggregatedJson())
                            .bind("sorItemId", resultLineItem.getSorItemId())

                            .add());
                    int[] counts = batch.execute();
                    log.info(aMarker, " persisted {} in trinity model_action", counts);
                });
            });
        } catch (Exception e) {
            insertSummaryAudit(jdbi, 0, 0, tableExtractionOutboundOutputs.size(), "failed in batch insert", action.getRootPipelineId());
            HandymanException exception = new HandymanException(e);
            exception.insertException("error inserting result " + tableExtractionOutboundOutputs, exception, action);
        }
    }

    void insertSummaryAudit(final Jdbi jdbi, int rowCount, int executeCount, int errorCount, String comments, Long tenantId) {
        try {
            SanitarySummary summary = new SanitarySummary().builder()
                    .rowCount(rowCount)
                    .correctRowCount(executeCount)
                    .errorRowCount(errorCount)
                    .comments(comments)
                    .tenantId(tenantId)
                    .build();
            jdbi.useTransaction(handle -> {
                Update update = handle.createUpdate(" INSERT INTO " + tableExtractionOutbound.getResultTable() + "_error ( row_count, correct_row_count, error_row_count,comments, created_at,root_pipeline_audit) " +
                        " VALUES(:rowCount, :correctRowCount, :errorRowCount, :comments, NOW(),:tenantId, :rootpipelineId);");
                Update bindBean = update.bindBean(summary);
                bindBean.execute();
            });
        } catch (Exception exception) {
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("error inserting into batch insert audit", handymanException, action);

        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SanitarySummary {
        private int rowCount;
        private int correctRowCount;
        private int errorRowCount;
        private String comments;
        private Long tenantId;

    }


    public TableExtractionAggregatedJson aggregatedJsonBuilder(final String columnHeader, final double sumOfColumnValue, final String aggregateFuntion, final Long sorItemId) {

        TableExtractionAggregatedJson tableExtractionAggregatedJson = new TableExtractionAggregatedJson();

        tableExtractionAggregatedJson.setColumnHeaderKey(columnHeader);
        tableExtractionAggregatedJson.setColumnHeaderValue(String.valueOf(sumOfColumnValue));
        tableExtractionAggregatedJson.setAggregateFunction(aggregateFuntion);

        return tableExtractionAggregatedJson;

    }

    private double extractedFromFilepath(String filePath, String columnName, Long totalRowCount) {
        double columnSum = 0.0;
        try {
            CSVParser csvParser = getCsvRecords(filePath);

            String rowAggregatePlaceholder=action.getContext().get("last.row.value.total.check");

            if(Objects.equals("true",rowAggregatePlaceholder)){
                columnSum = extractLastRowOnly(columnName, filePath);

            }else{

                for (CSVRecord csvRecord : csvParser) {

                        Long currentRowNumber = csvParser.getRecordNumber();

                        String cellValueStr = csvRecord.get(columnName.trim());

                        String cellValue = reformatCellValue(cellValueStr);

                        try {

                            if (!cellValue.isEmpty() && !cellValue.isBlank()) {
                                double value = Double.parseDouble(cellValue);
                                //checking the total row count matches the current row count then break the loop
                                if (!Objects.equals(totalRowCount, currentRowNumber)) {
                                    columnSum += value;
                                } else {
                                    //checking the total sum equals to the last cell of that column
                                    if (columnSum == value) {
                                        log.info("Breaked the process because last cell value matches the total sum of previous values.");
                                        break;
                                    } else {
                                        columnSum += value;
                                    }
                                }
                            } else {

                                if (Objects.equals(totalRowCount, currentRowNumber)) {
                                    CSVParser csvParser1 = getCsvRecords(filePath);
                                    CSVRecord cellRecordOut = csvParser1.getRecords().get((int) (totalRowCount - 2));
                                    String cellValueOut = cellRecordOut.get(columnName.trim());
                                    String cellValueOutStr = reformatCellValue(cellValueOut);
                                    double cellValueOutDouble = Double.parseDouble(cellValueOutStr);
                                    columnSum -= cellValueOutDouble;
                                    log.info("Last cell in this column was empty so checked the n-1 value is match the total");

                                }
                            }

                            //checking the cell value is empty or blank


                        } catch (Exception e) {
                            HandymanException handymanException = new HandymanException(e);
                            handymanException.insertException("cannot parse string " + cellValue, handymanException, action);
                        }


                    }
                }

            csvParser.close();

        } catch (Exception e) {
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in processing the csv file '" + filePath + "' ", handymanException, action);

        }
        return columnSum;
    }

    private Double extractLastRowOnly(String columnName, String filePath) throws IOException {
        List<Double> rowDataLineItem=new ArrayList<>();

        CSVParser csvParser = getCsvRecords(filePath);

        for (CSVRecord csvRecord : csvParser) {
            String cellValueStr = csvRecord.get(columnName.trim());

            String cellValue = reformatCellValue(cellValueStr);

            if( !cellValue.isEmpty() && !cellValue.isBlank()){
                    double value = Double.parseDouble(cellValue);
                    rowDataLineItem.add(value);
                }


        }
        if(!rowDataLineItem.isEmpty() && rowDataLineItem.size() > 1){
            return rowDataLineItem.get(rowDataLineItem.size()-1);
        }else{
            return rowDataLineItem.get(0);
        }

    }

    private Double extractSumOfTheRows(String columnName, String filePath) throws IOException {
        List<Double> rowDataLineItem=new ArrayList<>();


        CSVParser csvParser = getCsvRecords(filePath);

        for (CSVRecord csvRecord : csvParser) {
            String cellValueStr = csvRecord.get(columnName.trim());

            String cellValue = reformatCellValue(cellValueStr);


            if( !cellValue.isEmpty() && !cellValue.isBlank()){
                double value = Double.parseDouble(cellValue);
                rowDataLineItem.add(value);
            }
        }
        if(!rowDataLineItem.isEmpty()){
            return rowDataLineItem.stream().mapToDouble(Double::doubleValue).sum();
        }else{
            return 0.00;
        }

    }

    @NotNull
    private static CSVParser getCsvRecords(String filePath) throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(filePath));


        return new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
    }


    public String reformatCellValue(String cellValue) {
        String value = cellValue;
        value = value.replace(",", "");

        log.info("Input String value {} converted the string format into a number format {}", cellValue, value);

        return value;
    }

    public Long extractRowCount(String filePath) throws IOException {

        CSVParser csvParser = getCsvRecords(filePath);

        Long rowCount = 0L;
        for (CSVRecord singleRow : csvParser) {
            if (!singleRow.get(0).isEmpty()) {
                rowCount++;
            }

        }

        // Close the CSVParser
        csvParser.close();

        return rowCount;

    }

    public Long extractRowCountBuffer(final String filePath) {

        Long count = 0L;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Read each line of the file
            while (reader.readLine() != null) {
                // Increment the count for each line read
                count++;
            }
        } catch (IOException e) {
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("cannot get the row count ", handymanException, action);

        }
        log.info("No of row count {} in the given input {}", count, filePath);
        return count - 1;

    }

    public TableExtractionOutboundOutput transformInputOutput(final TableExtractionOutboundInput tableExtractionOutboundInput, final String aggregatedJson) {
        TableExtractionOutboundOutput tableExtractionOutboundOutput = new TableExtractionOutboundOutput();

        tableExtractionOutboundOutput.setProcessId(tableExtractionOutboundInput.getProcessId());
        tableExtractionOutboundOutput.setGroupId(tableExtractionOutboundInput.getGroupId());
        tableExtractionOutboundOutput.setTenantId(tableExtractionOutboundInput.getTenantId());
        tableExtractionOutboundOutput.setTemplateId(tableExtractionOutboundInput.getTemplateId());
        tableExtractionOutboundOutput.setOriginId(tableExtractionOutboundInput.getOriginId());
        tableExtractionOutboundOutput.setPaperNo(tableExtractionOutboundInput.getPaperNo());
        tableExtractionOutboundOutput.setProcessedFilePath(tableExtractionOutboundInput.getProcessedFilePath());
        tableExtractionOutboundOutput.setTableResponse(tableExtractionOutboundInput.getTableResponse());
        tableExtractionOutboundOutput.setStatus(tableExtractionOutboundInput.getStatus());
        tableExtractionOutboundOutput.setStage(tableExtractionOutboundInput.getStage());
        tableExtractionOutboundOutput.setMessage(tableExtractionOutboundInput.getMessage());
        tableExtractionOutboundOutput.setCreatedOn(tableExtractionOutboundInput.getCreatedOn());
        tableExtractionOutboundOutput.setRootPipelineId(tableExtractionOutboundInput.getRootPipelineId());
        tableExtractionOutboundOutput.setBboxes(tableExtractionOutboundInput.getBboxes());
        tableExtractionOutboundOutput.setCroppedimage(tableExtractionOutboundInput.getCroppedimage());
        tableExtractionOutboundOutput.setColumnHeader(tableExtractionOutboundInput.getColumnHeader());
        tableExtractionOutboundOutput.setTruthEntityName(tableExtractionOutboundInput.getTruthEntityName());
        tableExtractionOutboundOutput.setModelName(tableExtractionOutboundInput.getModelName());
        tableExtractionOutboundOutput.setSorItemId(tableExtractionOutboundInput.getSorItemId());
        tableExtractionOutboundOutput.setTableAggregateFunction(tableExtractionOutboundOutput.getTableAggregateFunction());
        tableExtractionOutboundOutput.setAggregatedJson(aggregatedJson);

        return tableExtractionOutboundOutput;
    }

    public void extractOutboundDateFromJson(final Jdbi jdbi, final List<TableExtractionOutboundInput> tableInfos, final List<TableExtractionOutboundOutput> tableExtractionOutboundOutputs) {
        //TODO implementation for the table extraction into outbound from Json should be completed

    }

    @Override
    public boolean executeIf() throws Exception {
        return tableExtractionOutbound.getCondition();
    }
}
