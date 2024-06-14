package in.handyman.raven.lambda.access.repo;

import in.handyman.raven.lambda.doa.DoaConstant;
import in.handyman.raven.lambda.doa.config.SpwResourceConfig;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface SpwResourceConfigRepo {

    String COLUMNS = " id, config_name, resource_url, active, user_name, password, driver_class, host, port, database_name, created_by, created_date, last_modified_by, last_modified_date, version ";

    @SqlUpdate("insert into " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SRC_TABLE_NAME + " (id, config_name, resource_url, active, user_name, password, driver_class, host, port, database_name, created_by, created_date, last_modified_by, last_modified_date, version) " +
            "VALUES ( :id, :configName, :resourceUrl, :active, :userName, :password, :driverClass, :host, :port, :databaseName,:createdBy, :createdDate, :lastModifiedBy, :lastModifiedDate, :version) RETURNING id")
    void insert(@BindBean final SpwResourceConfig spwResourceConfig);

    @SqlUpdate("UPDATE " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SRC_TABLE_NAME + " SET active=:active, created_by= :createdBy , created_date= :createdDate," +
            " last_modified_by= :lastModifiedBy, last_modified_date= :lastModifiedDate ,  config_name= :configName, resource_url= :resourceUrl, " +
            " user_name= :userName, password= :password, driver_class= :driverClass, host= :host, port= :port, database_name= :databaseName WHERE id = :id ")
    void update(@BindBean final SpwResourceConfig spwResourceConfig);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SRC_TABLE_NAME + " where active='TRUE'")
    @RegisterBeanMapper(value = SpwResourceConfig.class)
    List<SpwResourceConfig> findAll();

    @SqlQuery("SELECT count(1)+1 FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SRC_TABLE_NAME + " where config_name= :configName ")
    @RegisterBeanMapper(value = SpwResourceConfig.class)
    Long getNextVersion(@BindBean final SpwResourceConfig spwResourceConfig);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SRC_TABLE_NAME + " where config_name= :configName  and active='TRUE' ")
    @RegisterBeanMapper(value = SpwResourceConfig.class)
    Optional<SpwResourceConfig> findOne(@Bind("configName") final String variable);

    @SqlQuery("SELECT max(id)+1 FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SRC_TABLE_NAME )
    @RegisterBeanMapper(value = SpwResourceConfig.class)
    Long getId();
}
