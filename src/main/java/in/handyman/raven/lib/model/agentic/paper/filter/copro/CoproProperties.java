package in.handyman.raven.lib.model.agentic.paper.filter.copro;

/**
 * Properties related to Copro retry and audit behavior.
 */
public class CoproProperties {

    /**
     * Flag to indicate whether audit logs should be encrypted.
     */
    private final boolean encryptAudit;

    /**
     * Default constructor with encryption enabled.
     */
    public CoproProperties() {
        this.encryptAudit = true;
    }

    /**
     * Constructor to set custom encryption behavior.
     *
     * @param encryptAudit true if audit logs should be encrypted.
     */
    public CoproProperties(boolean encryptAudit) {
        this.encryptAudit = encryptAudit;
    }

    /**
     * @return true if audit logs should be encrypted.
     */
    public boolean isEncryptAudit() {
        return encryptAudit;
    }

    @Override
    public String toString() {
        return "CoproProperties{" +
                "encryptAudit=" + encryptAudit +
                '}';
    }
}
