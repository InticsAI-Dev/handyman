package in.handyman.raven.lib.adapters.comparison;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.ControlDataComparisonAction;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class DateComparisonAdapter implements ComparisonAdapter{
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DateComparisonAdapter.class);
    @Override
    public Long validate(ControlDataComparisonQueryInputTable record, ActionExecutionAudit action, Logger log) {
        String inputFormat = action.getContext().get("control.data.date.comparison.format");
        return dateValidation(
                inputFormat,log,record,action
        );
    }
    public Long dateValidation(String inputFormat, Logger log, ControlDataComparisonQueryInputTable controlDataInputLineItem,ActionExecutionAudit action) {
        String extractedDate = controlDataInputLineItem.getExtractedValue();
        String actualDate = controlDataInputLineItem.getActualValue();
        String originId = controlDataInputLineItem.getOriginId();
        Long paperNo = controlDataInputLineItem.getPaperNo();
        String sorItemName = controlDataInputLineItem.getSorItemName();
        Long tenantId = controlDataInputLineItem.getTenantId();
        if (extractedDate == null || extractedDate.isEmpty()) {
            log.warn("Invalid input encountered for extractedDate. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
            return actualDate == null ? 0L : (long) actualDate.length();
        }
        if (actualDate == null || actualDate.isEmpty()) {
            log.warn("Invalid input encountered for actualDate. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
            return (long) extractedDate.length();
        }

        String extractedLocalDate = null;

        try {
            if (extractedDate.matches("\\d{8}")) {
                extractedLocalDate = parseEightDigitDate(extractedDate, inputFormat, originId, paperNo, sorItemName, tenantId);
            }

            if (extractedLocalDate == null) {
                extractedLocalDate = parseDateWithFormat(extractedDate, inputFormat, originId, paperNo, sorItemName, tenantId,action);
            }
        } catch (DateTimeParseException e) {
            log.warn("Invalid extracted date format. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
            return (long) actualDate.length();
        }

        String actualLocalDate;
        try {
            actualLocalDate = parseDateWithFormat(actualDate, inputFormat, originId, paperNo, sorItemName, tenantId,action);
        } catch (DateTimeParseException e) {
            log.warn("Invalid actual date format. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
            return (long) actualDate.length();
        }

        return getMismatchCount(extractedLocalDate, actualLocalDate);
    }


    private String parseDateWithFormat(String date, String inputFormat, String originId, Long paperNo, String sorItemName, Long tenantId,ActionExecutionAudit action) {
        String allowedFormats = action.getContext().get("date.input.formats");

        List<DateTimeFormatter> dateInputFormats = Optional.of(allowedFormats).map(s -> Arrays.stream(s.split(";")).map(DateTimeFormatter::ofPattern).collect(Collectors.toList())).orElse(Collections.emptyList());

        for (DateTimeFormatter inputFormatter : dateInputFormats) {
            try {
                LocalDate parsedDate = LocalDate.parse(date, inputFormatter);

                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(inputFormat);
                return parsedDate.format(outputFormatter);
            } catch (DateTimeParseException ignored) {
                log.warn("Error in parsing the date format from given input format to specified output format. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
            }
        }
        return date;
    }

    private String parseEightDigitDate(String date, String inputFormat, String originId, Long paperNo, String sorItemName, Long tenantId) {
        String[] possibleFormats = {"yyyyMMdd", "MMddyyyy", "ddMMyyyy", "MMyyyydd"};

        for (String format : possibleFormats) {
            try {
                String reformattedDate = reformatEightDigitDate(date, format, originId, paperNo, sorItemName, tenantId);
                if (reformattedDate.isEmpty()) continue;

                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate parsedDate = LocalDate.parse(reformattedDate, inputFormatter);

                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(inputFormat);
                return parsedDate.format(outputFormatter);
            } catch (DateTimeParseException | NullPointerException ignored) {
                log.warn("Error in parsing the Eight digit date format from given input format to specified output format. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
            }
        }
        return null;
    }


    private String reformatEightDigitDate(String date, String format, String originId, Long paperNo, String sorItemName, Long tenantId) {
        if (date.length() != 8) return "";
        try {
            switch (format) {
                case "yyyyMMdd":
                    return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
                case "MMddyyyy":
                    return date.substring(4, 8) + "-" + date.substring(0, 2) + "-" + date.substring(2, 4);
                case "ddMMyyyy":
                    return date.substring(4, 8) + "-" + date.substring(2, 4) + "-" + date.substring(0, 2);
            }
        } catch (Exception ignored) {
            log.warn("Error in reformatting the Eight digit date format from given input format to specified output format. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
        }
        return "";
    }


    private Long getMismatchCount(String str1, String str2) {
        int mismatchCount = 0;
        int length = Math.max(str1.length(), str2.length());

        for (int i = 0; i < length; i++) {
            char c1 = (i < str1.length()) ? str1.charAt(i) : '\0';
            char c2 = (i < str2.length()) ? str2.charAt(i) : '\0';

            if (c1 != c2) {
                mismatchCount++;
            }
        }

        return (long) mismatchCount;
    }


    @Override
    public String getName() {
        return "date";
    }
}
