package in.handyman.raven.lib;

import com.opencsv.CSVReader;
import com.zaxxer.hikari.HikariDataSource;
import in.handyman.raven.connection.ResourceAccess;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lib.model.LoadCsv;
import in.handyman.raven.lym.action.ActionExecution;
import in.handyman.raven.lym.action.IActionExecution;
import in.handyman.raven.lym.doa.Action;
import in.handyman.raven.util.UniqueID;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UnknownFormatConversionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Auto Generated By Raven
 */
@ActionExecution(actionName = LoadCsvAction.LOAD_CSV)
public class LoadCsvAction implements IActionExecution {

    protected static final String LOAD_CSV = "LoadCsv";


    private final Action action;
    private final Logger log;
    private final LoadCsv loadCsv;

    private final Marker aMarker;

    public LoadCsvAction(final Action action, final Logger log, final Object loadCsv) {
        this.loadCsv = (LoadCsv) loadCsv;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(LOAD_CSV);
    }

    @Override
    public void execute() throws Exception {
        var csvFile = loadCsv.getSource();
        var sqlList = loadCsv.getValue().replace("\"", "");
        final String fileName;
        if (csvFile.contains("\\")) {
            var counter = csvFile.length() - csvFile.replace("\\\\", "").length();
            var file = csvFile.split("\\\\", counter + 1);
            fileName = file[counter];
        } else {
            var counter = csvFile.length() - csvFile.replace("\\/", "").length();
            var file = csvFile.split("/", counter + 1);
            fileName = file[counter];
        }
        log.info(aMarker, "id#{}, name#{}, from#{}, sqlList#{}", action.getActionId(), loadCsv.getName(), loadCsv.getSource(), sqlList);
        final String csvExtension = ".csv";
        final String tsvExtension = ".tsv";
        try (final CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
            final Iterator<String[]> iterator = csvReader.iterator();
            final String[] firstLine = iterator.next();
            final String ct = convertArrayToInsertLine(firstLine, "`VARCHAR(344),`");
            if (fileName.contains(csvExtension)) {
                var column = convertArrayToInsertLine(firstLine, "`,`");
                perform(fileName,
                        csvExtension,
                        iterator, column, ct);
            } else if (fileName.contains(tsvExtension)) {

                final String atos = String.join("", firstLine).replace("\t", ",");
                var tsvFirstLine = atos.split(",");
                var column = convertArrayToInsertLine(tsvFirstLine, "`,`");

                perform(fileName,
                        tsvExtension,
                        iterator, column, ct);
            } else {
                log.info("File format is invalid");
                throw new UnknownFormatConversionException("File format is invalid");
            }
        }


    }

    private String convertArrayToInsertLine(final String[] firstLine, final String delimiter) {
        var sb = new StringBuilder();
        if (firstLine != null) {
            for (var str : firstLine) {
                sb.append(str.replace(" ", "")).append(delimiter);
            }
        }
        return sb.substring(0, sb.length() - 2);
    }

    private void perform(final String fileName, final String extension, final Iterator<String[]> iterator, final String column, final String ct) throws SQLException {
        var pid = loadCsv.getPid();
        var db = loadCsv.getTo();
        var name = loadCsv.getName();
        var id = action.getActionId();
        var limit = Integer.parseInt(loadCsv.getLimit());
        final HikariDataSource hikariDataSource = ResourceAccess.rdbmsConn(db);
        try (final Connection connection = hikariDataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (final Statement st = connection.createStatement()) {
                final Long statementId = UniqueID.getId();
                //TODO
                final String dQuery = "drop table if exists `" + pid + "_" + fileName.replace(extension, "") + "`;";
                log.info(aMarker, "id#{}, name#{}, from#{}, Query#{}", id, name, db, dQuery);
                st.execute(dQuery);

                final String cQuery = "CREATE TABLE `" + pid + "_" + fileName.replace(extension, "") + "` (" + "`" + ct + ");";
                log.info(aMarker, "id#{}, name#{}, from#{}, Query#{}", id, name, db, dQuery);
                st.execute(cQuery);
            }
            final AtomicInteger count = new AtomicInteger();
            final List<String> iQuery = new ArrayList<>();
            try (final Statement st = connection.createStatement()) {
                final Long statementId = UniqueID.getId();
                //TODO
                iterator.forEachRemaining(nextLine -> {
                    if (Objects.nonNull(nextLine)) {
                        final String row = String.join("", nextLine).replace("\t", "~ ").replace("\"\"", "\\\"");
                        var tsvRow = row.split("~");
                        final String values = convertArrayToInsertLine(tsvRow, "','")
                                .replace("''", "'NULL'")
                                .replace("00:00:00.0", "");
                        iQuery.add("('" + values + ")");
                        final int currentCount = count.incrementAndGet();
                        if (currentCount % limit == 0) {
                            final String finalQuery = getFinalQuery(pid, fileName, column, iQuery);
                            try {
                                st.addBatch(finalQuery);
                            } catch (SQLException e) {
                                throw new HandymanException("Insert query failed in LoadCSV", e);
                            }
                            iQuery.clear();
                        }
                    }
                });
                final String finalQuery = getFinalQuery(pid, fileName, column, iQuery);
                st.addBatch(finalQuery);
                log.info(aMarker, " id#{}, name#{}, from#{}", pid, name, db);
                iQuery.clear();
                try {
                    st.executeBatch();
                } catch (SQLException e) {
                    throw new HandymanException("Insert query failed in LoadCSV", e);
                }
            } catch (SQLException e) {
                throw new HandymanException("Insert query failed in LoadCSV", e);
            }
            connection.commit();
        }
    }

    private String getFinalQuery(final String pid, final String fileName, final String column, final List<String> iQuery) {
        return "INSERT IGNORE INTO  `" + pid + "_" + fileName.replace(".csv", "") + "`  (" + "`" + column + ")" + "VALUES " + String.join(",", iQuery) + " ;";
    }

    @Override
    public boolean executeIf() {
        return false;
    }
}
