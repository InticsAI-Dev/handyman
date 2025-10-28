package in.handyman.raven.core.enums;

public final class ConsumerApiCountConstants {

    private ConsumerApiCountConstants() {
        throw new UnsupportedOperationException("ConsumerApiCountConstants is a utility class and cannot be instantiated");
    }

    public static final String P1_INGESTION_API = "asset.info.consumer.API.count";
    public static final String P2_PREPROCESS_API = "paper.itemizer.consumer.API.count";
    public static final String P3_PAPER_FILTER_API = "agentic.paper.filter.krypton.API.count";
    public static final String P4_KVP_KRYPTON_API = "Radon.kvp.consumer.API.count";
    public static final String P4_A1_OCR_COMPARATOR_API = "ocr.text.comparator.consumer.API.count";
    public static final String P6_DOC_EYE_CUE_API = "document.eye.cue.consumer.API.count";
    public static final String P7_KRYPTON_DEEP_SIFT_API = "deep.sift.extraction.krypton.API.count";
    public static final String P7_XENON_DEEP_SIFT_API = "deep.sift.extraction.xenon.API.count";
    public static final String P8_JSON_GENERATION_API = "alchemy.response.consumer.API.count";


}
