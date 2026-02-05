package in.handyman.raven.lib.model.bsh;

public class PostProcessingExecutorInput {

    private Long tenantId;
    private double aggregatedScore;
    private double maskedScore;
    private String originId;
    private Integer paperNo;
    private String extractedValue;
    private double vqaScore;
    private Integer rank;
    private Integer sorItemAttributionId;
    private String sorItemName;
    private String documentId;
    private Long accTransactionId;
    private String label;
    private String sectionAlias;
    private Long score;
    private String bBox;
    private Long rootPipelineId;
    private Long frequency;
    private Long questionId;
    private Long synonymId;
    private String modelRegistry;
    private String encryptionPolicy;
    private String isEncrypted;
    private String lineItemType;

    public String getExtractedValue() {
        return extractedValue;
    }

    public void setExtractedValue(String extractedValue) {
        this.extractedValue = extractedValue;
    }

    public void setLabel(String label) { this.label = label; }

    public void setSectionAlias(String sectionAlias) { this.sectionAlias = sectionAlias; }

    public void setBBox(String bBox) { this.bBox = bBox; }

    public String getLabel() { return label; }

    public String getSectionAlias() { return sectionAlias; }

    public String getBBox() { return bBox; }

    public String getSorItemName() { return sorItemName; }

    public void setSorItemName(String sorItemName) { this.sorItemName = sorItemName; }

}
