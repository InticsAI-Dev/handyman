package in.handyman.raven.lambda.access.repo;

import in.handyman.raven.lambda.doa.DoaConstant;
import in.handyman.raven.lambda.doa.config.SpwBshConfig;
import in.handyman.raven.lambda.doa.config.SpwCommonConfig;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface SpwBshConfigRepo {

    String COLUMNS = " id, tenant_id, created_on, created_user_id, last_updated_on, last_updated_user_id, caller_name, class_name, source_code, reference_id, version, status";

    
    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.CONFIG_SCHEMA_NAME + "." + DoaConstant.BSH_TABLE_NAME + " where tenant_id =:tenantId AND status='ACTIVE' ; ")
    @RegisterBeanMapper(value = SpwBshConfig.class)
    List<SpwBshConfig> findAllByTenantId(@Bind("tenantId") Long tenantId);


}
