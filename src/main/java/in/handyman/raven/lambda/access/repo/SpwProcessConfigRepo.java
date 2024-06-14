package in.handyman.raven.lambda.access.repo;

import in.handyman.raven.lambda.doa.DoaConstant;
import in.handyman.raven.lambda.doa.config.SpwProcessConfig;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface SpwProcessConfigRepo {

    String COLUMNS = " id, process, variable, value, active, created_by, created_date, last_modified_by, last_modified_date, version ";

    @SqlUpdate("insert into " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SPC_TABLE_NAME + " (id, process, variable, value, active, created_by, created_date, last_modified_by, last_modified_date, version) " +
            "VALUES (:id, :process, :variable, :value,:active,:createdBy, :createdDate, :lastModifiedBy, :lastModifiedDate, :version)")
    void insert(@BindBean final SpwProcessConfig spwProcessConfig);

    @SqlUpdate("UPDATE " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SPC_TABLE_NAME + " SET active=:active, created_by= :createdBy , created_date= :createdDate," +
            " last_modified_by= :lastModifiedBy, last_modified_date= :lastModifiedDate ,  process= :process , variable= :variable , value= :value WHERE id = :id ")
    void update(@BindBean final SpwProcessConfig spwProcessConfig);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SPC_TABLE_NAME + " where active='TRUE'")
    @RegisterBeanMapper(value = SpwProcessConfig.class)
    List<SpwProcessConfig> findAll();

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SPC_TABLE_NAME + " where process= :process and active='TRUE' ")
    @RegisterBeanMapper(value = SpwProcessConfig.class)
    List<SpwProcessConfig> findAllByProcess(@Bind("process") final String process);

    @SqlQuery("SELECT count(1)+1 FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SPC_TABLE_NAME + " where process= :process and variable= :variable ")
    @RegisterBeanMapper(value = SpwProcessConfig.class)
    Long getNextVersion(@BindBean final SpwProcessConfig spwProcessConfig);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SPC_TABLE_NAME + " where process= :process and variable= :variable  and active='TRUE'")
    @RegisterBeanMapper(value = SpwProcessConfig.class)
    Optional<SpwProcessConfig> findOne(@Bind("process") final String process, @Bind("variable") final String variable);

    @SqlQuery("SELECT max(id)+1 FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SPC_TABLE_NAME )
    @RegisterBeanMapper(value = SpwProcessConfig.class)
    Long getId();

}
