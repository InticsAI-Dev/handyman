package in.handyman.raven.core.enums;

public final class EncryptionConstants {
    private EncryptionConstants() {
        throw new UnsupportedOperationException("EncryptionConstants is a utility class and cannot be instantiated");
    }
    public static final String DEFAULT_ENCRYPTION_ALGORITHM = "pipeline.encryption.default.holder";
    public static final String ENCRYPT_ITEM_WISE_ENCRYPTION = "pipeline.end.to.end.encryption";
    public static final String ENCRYPT_REQUEST_RESPONSE = "pipeline.req.res.encryption";
    public static final String DOC_EYECUE_ENCRYPTION = "doc.eyecue.encryption";
    public static final String ENCRYPT_TEXT_EXTRACTION_OUTPUT = "pipeline.text.extraction.encryption";
    public static final String ENCRYPT_AGENTIC_FILTER_OUTPUT = "pipeline.agentic.paper.filter.encryption";
    public static final String ENCRYPT_DEEP_SIFT_OUTPUT = "pipeline.deep.sift.encryption";
    public static final String ENCRYPT_TRANSFORM_QUERY = "transform.sql.encryption.activator";
    public static final String KVP_JSON_PARSER_ENCRYPTION = "llm.json.parser.label.encryption";
    public static final String CONTROL_DATA_IS_ACTUAL_ENCRYPTED = "actual.encryption.variable";
    public static final String CONTROL_DATA_COMPARISON_STORE_AS_DECRYPTED = "control.data.comparison.store.as.decrypted";

    public static final String PROTEGRITY_API_RETRY_ACTIVATE = "protegrity.api.retry.activated";
    public static final String PROTEGRITY_API_RETRY_COUNT = "protegrity.api.retry.count";
    public static final String PROTEGRITY_API_RETRY_INTERVAL_SEC = "protegrity.api.retry.interval.secs";
    public static final String PROTEGRITY_API_RETRY_STATUS_CODES = "protegrity.api.retry.status.codes";

    public static final String PROTEGRITY_ENCRYPT_URL = "protegrity.enc.api.url";
    public static final String PROTEGRITY_DECRYPT_URL = "protegrity.dec.api.url";

    public static final String PROTEGRITY_API_TIMEOUT = "protegrity.timeout.seconds";
}

