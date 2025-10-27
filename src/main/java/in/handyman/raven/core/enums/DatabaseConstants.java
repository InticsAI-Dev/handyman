package in.handyman.raven.core.enums;

public final class DatabaseConstants {

    private DatabaseConstants() {
        throw new UnsupportedOperationException("DatabaseConstants is a utility class and cannot be instantiated");
    }

    public static final String DB_INSERT_WRITE_BATCH_SIZE = "write.batch.size";
    public static final String DB_SELECT_READ_BATCH_SIZE = "read.batch.size";
    public static final String DOCUMENT_EYE_CUE_WRITE_BATCH_SIZE = "document.eye.cue.write.batch.size";
    public static final String DOCUMENT_EYE_CUE_READ_BATCH_SIZE = "document.eye.cue.read.batch.size";
}
