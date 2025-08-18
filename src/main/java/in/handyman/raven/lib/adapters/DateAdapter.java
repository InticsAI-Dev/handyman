package in.handyman.raven.lib.adapters;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.interfaces.AdapterInterface;
import in.handyman.raven.lib.model.FieldValidator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateAdapter implements AdapterInterface {

    @Override
    public boolean getDateValidationModel(String dateValue, int comparableYear, String[] dateFormats) throws Exception {

        if (dateValue.trim().equals("")) {
            return false;
        }

        for (String format : dateFormats) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                dateFormat.setLenient(false);
                Date date = dateFormat.parse(dateValue);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int validateYear = calendar.get(Calendar.YEAR);
                int currentYear = Integer.parseInt(new SimpleDateFormat("yy").format(new Date()));
                boolean yearValidation;
                if (Integer.toString(validateYear).length() >= 4) {
                    yearValidation = validateYear < comparableYear;
                } else if (validateYear >= 00 && validateYear <= currentYear) {
                    String temp = "20" + validateYear;
                    yearValidation = Integer.parseInt(temp) < comparableYear;
                } else {
                    String temp = "19" + validateYear;
                    yearValidation = Integer.parseInt(temp) < comparableYear;
                }
                return yearValidation;
            } catch (ParseException ex) {
                //throw new HandymanException("Failed to execute", ex);
            }
        }
        return false;

    }

    @Override
    public boolean getValidationModel(String sentence, String requiredFeature, ActionExecutionAudit audit) throws Exception {
        return false;
    }

    @Override
    public boolean getNameValidationModel(FieldValidator input, String uri, ActionExecutionAudit audit) throws Exception {
        return false;
    }

    @Override
    public int getThresholdScore(String sentence) throws Exception {
        return 0;
    }

}
