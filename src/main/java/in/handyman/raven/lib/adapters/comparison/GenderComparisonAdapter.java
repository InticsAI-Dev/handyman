package in.handyman.raven.lib.adapters.comparison;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.ControlDataComparisonAction;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import org.slf4j.Logger;


public class GenderComparisonAdapter implements ComparisonAdapter{
    @Override
    public Long validate(ControlDataComparisonQueryInputTable controlDataComparisonLineItem, ActionExecutionAudit action, Logger log) {
        return genderValidation(
                controlDataComparisonLineItem, action, log
        );
    }

    public Long genderValidation(ControlDataComparisonQueryInputTable controlDataComparisonLineItem, ActionExecutionAudit action, Logger log) {
        String extractedGender = controlDataComparisonLineItem.getExtractedValue();
        String generatedGender = controlDataComparisonLineItem.getActualValue();
        String originId = controlDataComparisonLineItem.getOriginId();
        Long paperNo = controlDataComparisonLineItem.getPaperNo();
        String sorItemName = controlDataComparisonLineItem.getSorItemName();
        Long tenantId = controlDataComparisonLineItem.getTenantId();

        if (extractedGender == null) {
            log.warn("Invalid input for extractedGender. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
            return generatedGender == null ? 0L : (long) generatedGender.length();
        }
        if (generatedGender == null) {
            log.warn("Invalid input for generatedGender. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
            return (long) extractedGender.length();
        }
        String formattedExtractedGender = normalizeGender(extractedGender);
        String formattedGeneratedGender = normalizeGender(generatedGender);

        if (formattedExtractedGender.equals("Invalid") || formattedGeneratedGender.equals("Invalid")) {
            return 1L;
        }

        return formattedExtractedGender.equalsIgnoreCase(formattedGeneratedGender) ? 0L
                : (long) formattedExtractedGender.length();
    }

    private String normalizeGender(String gender) {
        if (gender == null) return "Invalid";

        gender = gender.trim().toLowerCase();

        switch (gender) {
            case "m":
            case "male":
                return "Male";
            case "f":
            case "female":
                return "Female";
            default:
                return "Invalid";
        }
    }


    @Override
    public String getName() {
        return "gender";
    }
}
