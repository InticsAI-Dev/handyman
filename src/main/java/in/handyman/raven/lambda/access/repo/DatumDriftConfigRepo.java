package in.handyman.raven.lambda.access.repo;

import in.handyman.raven.lambda.doa.DoaConstant;
import in.handyman.raven.lambda.doa.config.DatumDriftConfig;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface DatumDriftConfigRepo {

    String COLUMNS = "id, schema_name, table_name, table_type, cron_execution_time, active, created_by, created_date, last_modified_by, last_modified_date, version";
    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.DDC_TABLE_NAME + " where active=true ; ")
    @RegisterBeanMapper(value = DatumDriftConfig.class)
    List<DatumDriftConfig> findAll();
}
