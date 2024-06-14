package in.handyman.raven.lambda.access.repo;

import in.handyman.raven.lambda.doa.DoaConstant;
import in.handyman.raven.lambda.doa.config.SpwInstanceConfig;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface SpwInstanceConfigRepo {

    String COLUMNS = " id, instance, process, variable, value, active, created_by, created_date, last_modified_by, last_modified_date, version ";

    @SqlUpdate("insert into " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SIC_TABLE_NAME + " (id, instance,process, variable, value, active, created_by, created_date, last_modified_by, last_modified_date, version) " +
            "VALUES (:id, :instance, :process, :variable, :value,:active,:createdBy, :createdDate, :lastModifiedBy, :lastModifiedDate, :version)")
    void insert(@BindBean final SpwInstanceConfig spwInstanceConfig);

    @SqlUpdate("UPDATE " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SIC_TABLE_NAME + " SET active=:active, created_by= :createdBy , created_date= :createdDate," +
            " last_modified_by= :lastModifiedBy, last_modified_date= :lastModifiedDate , instance= :instance, process= :process , variable= :variable , value= :value WHERE id = :id ")
    void update(@BindBean final SpwInstanceConfig spwInstanceConfig);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SIC_TABLE_NAME + " where active='TRUE'")
    @RegisterBeanMapper(value = SpwInstanceConfig.class)
    List<SpwInstanceConfig> findAll();

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SIC_TABLE_NAME + " where instance= :instance  and active='TRUE'")
    @RegisterBeanMapper(value = SpwInstanceConfig.class)
    List<SpwInstanceConfig> findAllByInstance(@Bind("instance") final String instance);

    @SqlQuery("SELECT count(1)+1 FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SIC_TABLE_NAME + " where instance= :instance  and variable= :variable  ")
    @RegisterBeanMapper(value = SpwInstanceConfig.class)
    Long getNextVersion(@BindBean final SpwInstanceConfig spwInstanceConfig);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SIC_TABLE_NAME + " where instance= :instance  and variable= :variable  and active='TRUE'")
    @RegisterBeanMapper(value = SpwInstanceConfig.class)
    Optional<SpwInstanceConfig> findOne(@Bind("instance") final String instance, @Bind("variable") final String variable);


    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SIC_TABLE_NAME + " where variable= :variable and active='true'  ")
    @RegisterBeanMapper(value = SpwInstanceConfig.class)
    List<SpwInstanceConfig> findAllByInstanceVariable(@Bind("variable") final String variable);

    @SqlQuery("select count(1) from " + DoaConstant.CONFIG_SCHEMA_NAME + " where name = :name and variable= :variable and config_type_id= :configTypeId")
    Integer count(@Bind("name") final String name, @Bind("variable") final String variable, @Bind("configTypeId") final Integer configTypeId);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SIC_TABLE_NAME + " where name = :name and variable= :variable and config_type_id= :configTypeId and active='true' order by version desc limit 1; ")
    @RegisterBeanMapper(value = SpwInstanceConfig.class)
    Optional<SpwInstanceConfig> get(@Bind("name") final String name, @Bind("variable") final String variable, @Bind("configTypeId") final Integer configTypeId);

    @SqlQuery("SELECT max(id)+1 FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.SIC_TABLE_NAME + "; ")
    @RegisterBeanMapper(value = SpwInstanceConfig.class)
    Long getId();
}
