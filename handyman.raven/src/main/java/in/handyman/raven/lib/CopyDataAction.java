package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ConfigAccess;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.Action;
import in.handyman.raven.lib.model.CopyData;
import in.handyman.raven.util.Table;
import in.handyman.raven.util.UniqueID;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.insert.Insert;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Auto Generated By Raven
 */
@ActionExecution(actionName = "CopyData")
public class CopyDataAction implements IActionExecution {

    private final Action action;
    private final Logger log;
    private final CopyData copyData;

    private final Marker aMarker;

    public CopyDataAction(final Action action, final Logger log, final Object copyData) {
        this.copyData = (CopyData) copyData;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker("CopyData");
    }

    @Override
    public void execute() throws Exception {
        //Retrieving the global config map for default value
        var configMap = ConfigAccess.getCommonConfig();
        var pipelineId = action.getPipelineId();
        var name = copyData.getName();
        var source = Optional.ofNullable(copyData.getSource()).map(String::trim)
                .filter(s -> !s.isEmpty() && !s.isBlank())
                .orElseThrow(() -> new HandymanException("source data source cannot be empty for copyData for " + name));
        var target = Optional.ofNullable(copyData.getTo()).map(String::trim)
                .filter(s -> !s.isEmpty() && !s.isBlank())
                .orElseThrow(() -> new HandymanException("target data source cannot be empty for copyData for " + name));
        var fetchSize = Optional.ofNullable(copyData.getFetchBatchSize())
                .map(String::trim)
                .map(Integer::valueOf)
                .filter(integer -> integer > 0)
                .orElseGet(() -> Integer.valueOf(configMap.getOrDefault(Constants.READ_SIZE, Constants.DEFAULT_READ_SIZE).trim()));
        var writeSize = Optional.ofNullable(copyData.getWriteBatchSize())
                .map(String::trim)
                .map(Integer::valueOf)
                .filter(integer -> integer > 0)
                .orElseGet(() -> Integer.valueOf(configMap.getOrDefault(Constants.WRITE_SIZE, Constants.DEFAULT_WRITE_SIZE).trim()));
        var upperThreadCount = Optional.ofNullable(copyData.getWriteThreadCount())
                .map(String::trim)
                .map(Integer::valueOf)
                .filter(integer -> integer > 0)
                .orElseGet(() -> Integer.valueOf(configMap.getOrDefault(Constants.WRITER_THREAD, Constants.DEFAULT_WRITER_COUNT).trim()));
        var lowerThreadCount = 1;
        //retrieving the insert into sql statement
        var insertStatement = Optional.ofNullable(copyData.getValue()).map(String::trim)
                .map(s -> s.replaceAll("\"", ""))
                .filter(s -> !s.isEmpty() && !s.isBlank())
                .orElseThrow(() -> new HandymanException("INSERT INTO SELECT .... cannot be empty for copyData for " + name));

        var givenStatement = CCJSqlParserUtil.parse(insertStatement);
        if (givenStatement instanceof Insert) {
            var insert = (Insert) givenStatement;
            var select = insert.getSelect();

            log.info("CopyData action input variables id:{},name: {}, source-database:{}, target-database:{}, fetchSize:{}, writeSize:{},threadCount:{} ", pipelineId, name, source, target, fetchSize, writeSize, upperThreadCount);
            log.info("CopyData Insert Sql input post parameter ingestion \n : {}", insert);
            log.info("CopyData Select Sql input post parameter ingestion \n : {}", insert);
            //initializing the connection related statement
            var hikariDataSource = ResourceAccess.rdbmsConn(source);
            final Long statementId = UniqueID.getId();
            final ExecutorService executor = Executors.newWorkStealingPool();
            var rand = new Random();
            var rowQueueMap = new LinkedHashMap<Integer, BlockingQueue<Table.Row>>();
            var rowsProcessed = new AtomicInteger(0);

            try (final Connection sourceConnection = hikariDataSource.getConnection()) {
                //TODO AUDIT
                try (final Statement stmt = sourceConnection.createStatement()) {
                    stmt.setFetchSize(fetchSize);
                    var countDownLatch = new CountDownLatch(upperThreadCount);
                    IntStream.range(lowerThreadCount, upperThreadCount + 1).forEach(i -> {
                        var rowQueue = new LinkedBlockingDeque<Table.Row>();
                        var poisonPill = new Table.Row(i, null);
                        log.info(aMarker, " action is prepping up writer thread with poison pill {}", poisonPill);
                        final CopyDataJdbcWriter jdbcWriter = new CopyDataJdbcWriter(configMap, insert, poisonPill, copyData,
                                action, rowQueue, countDownLatch);
                        executor.submit(jdbcWriter);
                        rowQueueMap.put(poisonPill.getRowId(), rowQueue);
                    });
                    //Retrieving the data from the source
                    var selectStatement = select.toString();
                    try (var rs = stmt.executeQuery(selectStatement)) {
                        var nrCols = rs.getMetaData().getColumnCount();
                        while (rs.next()) {
                            var startTime = System.currentTimeMillis();
                            var row = getRow(rs, nrCols);
                            addRowToQueue(upperThreadCount, lowerThreadCount, rand, rowQueueMap, row);
                            addAudit(pipelineId, name, fetchSize, statementId, rowsProcessed, startTime);
                            rowQueueMap.forEach((integer, rows) -> rows.add(new Table.Row(integer, null)));
                            try {
                                countDownLatch.await();
                            } catch (final InterruptedException ex) {
                                log.error(aMarker, "{} error during waiting for worker threads to finish their job", pipelineId, ex);
                                throw ex;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.error(aMarker, "{} error closing source connection for database {}", pipelineId, source, ex);
                throw ex;
            }
        } else {
            throw new HandymanException("Insert stmt not found");
        }
    }

    private Table.Row getRow(final ResultSet rs, final int nrCols) throws SQLException {
        var columnSet = new LinkedHashSet<Table.ColumnInARow>();
        var id = rs.getRow();
        IntStream.range(1, nrCols + 1).forEach(i -> {
            final Table.ColumnInARow column = createColumn(i, rs);
            columnSet.add(column);
        });
        return new Table.Row(id, columnSet);
    }

    private void addRowToQueue(final Integer upperThreadCount, final int lowerThreadCount, final Random rand, final LinkedHashMap<Integer, BlockingQueue<Table.Row>> rowQueueMap, final Table.Row row) {
        var queueNumber = rand.nextInt((upperThreadCount - lowerThreadCount) + 1) + lowerThreadCount;
        var rowQueue = rowQueueMap.get(queueNumber);
        rowQueue.add(row);
    }

    private void addAudit(final Long instanceId, final String name, final Integer fetchSize, final Long statementId, final AtomicInteger rowsProcessed, final long startTime) {
        if (rowsProcessed.incrementAndGet() % fetchSize == 0) {
            var endTime = System.currentTimeMillis();
            var timeTaken = endTime - startTime;
            //Taken care of batch audit
        }
    }

    private Table.ColumnInARow createColumn(final int i, final ResultSet rs) {
        try {
            final ResultSetMetaData rsMetaData = rs.getMetaData();
            var columnType = rsMetaData.getColumnType(i);
            var columnTypeName = rsMetaData.getColumnTypeName(i);
            var columnName = rsMetaData.getColumnName(i);
            var columnLabel = rsMetaData.getColumnLabel(i);
            var scale = rsMetaData.getScale(i);
            var value = rs.getObject(i);
            var isLastColumn = i == rsMetaData.getColumnCount();
            return new Table.ColumnInARow(columnType, columnTypeName, columnName, columnLabel,
                    scale, value, null, isLastColumn);
        } catch (Exception ex) {
            throw new HandymanException("Column mapping failed", ex);
        }
    }

    @Override
    public boolean executeIf() {
        return copyData.getCondition();
    }
}
