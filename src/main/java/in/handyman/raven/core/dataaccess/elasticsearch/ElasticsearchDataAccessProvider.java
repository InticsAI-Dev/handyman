package in.handyman.raven.core.dataaccess.elasticsearch;

import in.handyman.raven.core.dataaccess.DataAccessKind;
import in.handyman.raven.core.dataaccess.DataAccessProvider;
import in.handyman.raven.lambda.doa.config.SpwResourceConfig;

public class ElasticsearchDataAccessProvider implements DataAccessProvider {

    private final String name;
    private final SpwResourceConfig resource;

    public ElasticsearchDataAccessProvider(String name, SpwResourceConfig resource) {
        this.name = name;
        this.resource = resource;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DataAccessKind kind() {
        return DataAccessKind.ELASTICSEARCH;
    }

    @Override
    public Object asElastic() {
        throw new UnsupportedOperationException(
                "Elasticsearch client not wired yet. Add 'co.elastic.clients:elasticsearch-java' to pom.xml " +
                "and implement client construction here using resource: " + resource);
    }
}
