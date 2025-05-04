package in.handyman.raven.lib.encryption;

public final class EncryptionConstants {
    private EncryptionConstants() {
        throw new UnsupportedOperationException("EncryptionConstants is a utility class and cannot be instantiated");
    }
    public static final String DEFAULT_ENCRYPTION_ALGORITHM = "pipeline.encryption.default.holder";
    public static final String ENCRYPT_ITEM_WISE_ENCRYPTION = "pipeline.end.to.end.encryption";
    public static final String ENCRYPT_REQUEST_RESPONSE = "pipeline.req.res.encryption";
    public static final String ENCRYPT_TEXT_EXTRACTION_OUTPUT = "pipeline.text.extraction.encryption";
    public static final String ENCRYPT_AGENTIC_FILTER_OUTPUT = "pipeline.agentic.paper.filter.encryption";
    public static final String ENCRYPT_TRANSFORM_QUERY = "transform.sql.encryption.activator";

    public static final String PROTEGRITY_ENCRYPT_URL = "protegrity.enc.api.url";
    public static final String PROTEGRITY_DECRYPT_URL = "protegrity.dec.api.url";


}

