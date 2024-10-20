package in.handyman.raven.lib;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.Alias;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.LoadExtractedData;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "LoadExtractedData"
)
public class LoadExtractedDataAction implements IActionExecution {
    private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
    private final ActionExecutionAudit action;
    private final Logger log;
    private final LoadExtractedData loadExtractedData;
    private final Marker aMarker;
    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String URI;
    private final String esUsername;
    private final String esPassword;
    private final String esHostname;


    public LoadExtractedDataAction(final ActionExecutionAudit action, final Logger log,
                                   final Object loadExtractedData) {
        this.loadExtractedData = (LoadExtractedData) loadExtractedData;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" LoadExtractedData:" + this.loadExtractedData.getName());
        this.esUsername = action.getContext().get("es.username");
        this.esPassword = action.getContext().get("es.password");
        this.esHostname = action.getContext().get("es.hostname");
        this.elasticsearchClient = getElasticsearchClient(esUsername, esPassword, esHostname);
        this.URI = action.getContext().get("copro.data-extraction.url");
    }

    private static ElasticsearchClient getElasticsearchClient(String userName, String password, String hostName) {

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
        var restClient = RestClient.builder(new HttpHost(hostName, Integer.parseInt("9200"))).setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(6000000).setSocketTimeout(6000000)).setHttpClientConfigCallback(httpAsyncClientBuilder -> {
            httpAsyncClientBuilder.setMaxConnTotal(500);
            httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            httpAsyncClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(10).build());
            return httpAsyncClientBuilder;
        }).build();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        var transport = new RestClientTransport(restClient, new JacksonJsonpMapper(mapper));
        return new ElasticsearchClient(transport);
    }

    @Override
    public void execute() throws Exception {

        log.info(aMarker, "<-------Store Data in Elastic Search Action for {} has been started------->" + loadExtractedData.getName());
        final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES).build();
        log.info(aMarker, " Input variables id : {}", action.getActionId());

        final String indexName = "source_of_truth";
        Set<String> alias = Collections.emptySet();

        try {
            final boolean exists = elasticsearchClient.indices().exists(builder -> builder.index(indexName)).value();
            if (!exists) {
                var createIndexResponse = elasticsearchClient.indices()
                        .create(new CreateIndexRequest.Builder().index(indexName)
                                .aliases(alias.stream().collect(Collectors.toMap(o -> o, s ->
                                        new Alias.Builder().isWriteIndex(true).build()))).build());
                log.info(aMarker, "response status {} shard {}", createIndexResponse.acknowledged(),
                        createIndexResponse.shardsAcknowledged());
            }
            final ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("inputFilePath", loadExtractedData.getFilePath());
            objectNode.put("outputDir", loadExtractedData.getTargetDir());

            Request request = new Request.Builder().url(URI)
                    .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();

            try (Response response = httpclient.newCall(request).execute()) {
                String responseBody = response.body().string();
                JsonNode extractedResult = mapper.readTree(responseBody);
                String name = loadExtractedData.getName() + "_response";
                if (response.isSuccessful()) {
                    log.info(aMarker, "The Successful Response for {} --> {}", name, responseBody);
                    action.getContext().put(name.concat(".error"), "false");
                } else {
                    log.info(aMarker, "The Failure Response {} --> {}", name, responseBody);
                    action.getContext().put(name.concat(".error"), "true");
                }


                String indexId = String.valueOf(System.currentTimeMillis());
                ObjectNode indexNode = mapper.createObjectNode();
                indexNode.put("type", "AGADIA_SOT");
                indexNode.put("created_at", String.valueOf(LocalDateTime.now()));
                indexNode.put("file_path", loadExtractedData.getFilePath());
                indexNode.put("intics_reference_id", loadExtractedData.getInticsReferenceId());
                indexNode.put("page_no", loadExtractedData.getPaperNo());
                indexNode.put("batch_id", loadExtractedData.getBatchId());
                indexNode.put("page_content", extractedResult.get("pageContent"));
                IndexResponse indexResponse = elasticsearchClient.index(IndexRequest.of(indexReq -> indexReq
                        .index(indexName)
                        .id(indexId)
                        .document(indexNode))
                );
                log.info(aMarker, "response status {} payloadID {}", indexResponse.index(), indexResponse.id());
            } catch (Exception e) {
                log.info(aMarker, "The Exception occurred ", e);
                throw new HandymanException("Failed to execute", e);
            }
        } catch (IOException ex) {
            throw new Exception("Index save failed", ex);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return loadExtractedData.getCondition();
    }
}
