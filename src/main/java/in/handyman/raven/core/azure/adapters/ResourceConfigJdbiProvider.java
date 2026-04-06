package in.handyman.raven.core.azure.adapters;

import com.zaxxer.hikari.HikariDataSource;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.repo.HandymanRepoImpl;
import in.handyman.raven.lambda.doa.config.SpwResourceConfig;
import org.jdbi.v3.core.Jdbi;

public class ResourceConfigJdbiProvider {

    private final SpwResourceConfig resourceConfig;

    public ResourceConfigJdbiProvider(SpwResourceConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
    }

    public Jdbi get() {
        HikariDataSource ds = new HikariDataSource(HikariJdbiProvider.getHikariConfig(resourceConfig));
        return Jdbi.create(ds);
    }

    public static SpwResourceConfig fetchResourceConfig() {
        HandymanRepoImpl repo = new HandymanRepoImpl();
        return repo.findAllResourceConfigs().stream()
                .findFirst()
                .map(c -> repo.getResourceConfig(c.getConfigName()))
                .orElseThrow(() -> new HandymanException(
                        "No resource config found in spw_resource_config"));
    }

}
