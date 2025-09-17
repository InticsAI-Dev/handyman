package in.handyman.raven.core.monitoring;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        DataSource original = DataSourceBuilder.create()
                .url("jdbc:mysql://localhost:3306/test")
                .username("user")
                .password("password")
                .build();

        return new TrackingDataSource(original);
    }
}
