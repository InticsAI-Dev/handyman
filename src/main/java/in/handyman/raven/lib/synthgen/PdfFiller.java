package in.handyman.raven.lib.synthgen;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfFiller {

//    public static void main(String[] args) throws IOException, CsvException {
//        String csvFilePath = "/home/anandh.andrews@zucisystems.com/Downloads/VA_CAID_Precert_Request_Form_1.csv"; // CSV file path
//        String templatePdfPath = "/home/anandh.andrews@zucisystems.com/Downloads/VA_CAID_Precert_Request_Form_Template.pdf"; // PDF template path
//        String outputFolder = "/home/anandh.andrews@zucisystems.com/Downloads/synth-output/"; // Folder to save PDFs
//
//        // Define CSV to PDF field mapping
//        //        csvToPdfMap.put("CSV Field", "PDF Field");
//        Map<String, String> csvToPdfMap = new HashMap<>();
//        csvToPdfMap.put("today_date", "Text1");
//        csvToPdfMap.put("member_first_name", "Text3");
//        csvToPdfMap.put("member_last_name", "Text4");
//        csvToPdfMap.put("member_id", "Text5");
//        csvToPdfMap.put("member_address_line1", "Text6");
//        csvToPdfMap.put("member_date_of_birth", "Text8");
//        csvToPdfMap.put("servicing_provider_address_line1", "Text26");
//        csvToPdfMap.put("servicing_provider_npi", "Text20");
//        csvToPdfMap.put("servicing_provider_tin", "Text22");
//        csvToPdfMap.put("servicing_provider_specialty", "Text28");
//        csvToPdfMap.put("servicing_facility_name", "Text29");
//        csvToPdfMap.put("servicing_facility_address_line1", "Text36");
//        csvToPdfMap.put("servicing_facility_npi", "Text30");
//        csvToPdfMap.put("servicing_facility_tin", "Text32");
//        csvToPdfMap.put("referring_provider_npi", "Text11");
//        csvToPdfMap.put("referring_provider_tin", "Text13");
//        csvToPdfMap.put("referring_provider_address_line1", "Text17");
//        csvToPdfMap.put("referring_provider_specialty", "Text19");
//        csvToPdfMap.put("service_code", "Text39");
//        csvToPdfMap.put("diagnosis_code", "Text38");
//
//
//        // Read CSV File
//        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
//            List<String[]> csvData = reader.readAll();
//            String[] headers = csvData.get(0); // First row is headers
//
//            // Process each row (excluding the header)
//            for (int i = 1; i < csvData.size(); i++) {
//                String[] row = csvData.get(i);
//                Map<String, String> rowData = new HashMap<>();
//                String outputPdfName = "default.pdf"; // Default name in case docid is missing
//
//                for (int j = 0; j < headers.length; j++) {
//                    if (headers[j].equalsIgnoreCase("docid")) {
//                        outputPdfName = row[j] + ".pdf"; // Use docid as PDF file name
//                    } else {
//                        rowData.put(headers[j], row[j]); // Store CSV row data
//                    }
//                }
//
//                // Generate the PDF for this row
//                fillPdfWithData(templatePdfPath, outputFolder + outputPdfName, rowData, csvToPdfMap);
//                System.out.println("Generated: " + outputPdfName);
//            }
//        }
//    }
//
//    private static void fillPdfWithData(String inputPdfPath, String outputPdfPath, Map<String, String> rowData, Map<String, String> csvToPdfMap) throws IOException {
//        PdfDocument pdfDocument = new PdfDocument(new PdfReader(inputPdfPath), new PdfWriter(outputPdfPath));
//        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDocument, true);
//        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
//        if (form != null) {
//            Map<String, PdfFormField> fields = form.getFormFields();
//            for (Map.Entry<String, String> entry : csvToPdfMap.entrySet()) {
//                String csvColumn = entry.getKey();
//                String pdfField = entry.getValue();
//
//                if (fields.containsKey(pdfField) && rowData.containsKey(csvColumn)) {
//                    fields.get(pdfField).setValue(rowData.get(csvColumn));
//                    fields.get(pdfField).setFontSize(10);
//
//                }
//            }
//            form.flattenFields(); // Make fields non-editable
//        }
//
//        pdfDocument.close();
//    }


    public static void main(String[] args) throws IOException, CsvException {
        String csvFilePath = "/home/anandh.andrews@zucisystems.com/Downloads/VA_CAID_Precert_Request_Form_1.csv";
        String templatePdfPath = "/home/anandh.andrews@zucisystems.com/Downloads/VA_CAID_Precert_Request_Form_Template.pdf";
        String outputFolder = "/home/anandh.andrews@zucisystems.com/Downloads/synth-output/";

        Map<String, String> csvToPdfMap = new HashMap<>();
        csvToPdfMap.put("today_date", "Text1");
        csvToPdfMap.put("member_first_name", "Text3");
        csvToPdfMap.put("member_last_name", "Text4");
        csvToPdfMap.put("member_id", "Text5");
        csvToPdfMap.put("member_address_line1", "Text6");
        csvToPdfMap.put("member_date_of_birth", "Text8");
        csvToPdfMap.put("servicing_provider_address_line1", "Text26");
        csvToPdfMap.put("servicing_provider_npi", "Text20");
        csvToPdfMap.put("servicing_provider_tin", "Text22");
        csvToPdfMap.put("servicing_provider_specialty", "Text28");
        csvToPdfMap.put("servicing_facility_name", "Text29");
        csvToPdfMap.put("servicing_facility_address_line1", "Text36");
        csvToPdfMap.put("servicing_facility_npi", "Text30");
        csvToPdfMap.put("servicing_facility_tin", "Text32");
        csvToPdfMap.put("referring_provider_npi", "Text11");
        csvToPdfMap.put("referring_provider_tin", "Text13");
        csvToPdfMap.put("referring_provider_address_line1", "Text17");
        csvToPdfMap.put("referring_provider_specialty", "Text19");
        csvToPdfMap.put("service_code", "Text39");
        csvToPdfMap.put("diagnosis_code", "Text38");
        csvToPdfMap.put("member_city+member_state+member_zipcode", "Text7");
        csvToPdfMap.put("servicing_provider_city+servicing_provider_state+servicing_provider_zipcode", "Text27");
        csvToPdfMap.put("referring_provider_city+referring_provider_state+referring_provider_zipcode", "Text18");
        csvToPdfMap.put("servicing_facility_city+servicing_facility_state+servicing_facility_zipcode", "Text37");
        csvToPdfMap.put("service_from_date+service_to_date", "Requested service for type of service check all that apply Datedate range of service");
        csvToPdfMap.put("servicing_provider_first_name+servicing_provider_last_name", "Full name_2");
        csvToPdfMap.put("referring_provider_first_name+referring_provider_last_name", "Full name");

        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            List<String[]> csvData = reader.readAll();
            String[] headers = csvData.get(0);
            for (int i = 1; i < csvData.size(); i++) {
                String[] row = csvData.get(i);
                Map<String, String> rowData = new HashMap<>();
                String outputPdfName = "default.pdf";

                for (int j = 0; j < headers.length; j++) {
                    if (headers[j].equalsIgnoreCase("docid")) {
                        outputPdfName = row[j] + ".pdf";
                    } else {
                        rowData.put(headers[j], row[j]);
                    }
                }
                fillPdfWithData(templatePdfPath, outputFolder + outputPdfName, rowData, csvToPdfMap);
                System.out.println("Generated: " + outputPdfName);
            }
        }
    }
    private static void fillPdfWithData(String inputPdfPath, String outputPdfPath, Map<String, String> rowData, Map<String, String> csvToPdfMap) throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(inputPdfPath), new PdfWriter(outputPdfPath));
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDocument, true);
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        if (form != null) {
            Map<String, PdfFormField> fields = form.getFormFields();
            for (Map.Entry<String, String> entry : csvToPdfMap.entrySet()) {
                String[] csvColumns = entry.getKey().split("\\+");
                String pdfField = entry.getValue();

                StringBuilder mergedValue = new StringBuilder();
                if (csvColumns.length == 2 && "service_from_date".equals(csvColumns[0]) && "service_to_date".equals(csvColumns[1])) {
                    String fromDate = rowData.getOrDefault("service_from_date", "");
                    String toDate = rowData.getOrDefault("service_to_date", "");
                    if (!fromDate.isEmpty() && !toDate.isEmpty()) {
                        mergedValue.append(fromDate).append(" to ").append(toDate);
                    } else {
                        mergedValue.append(fromDate).append(toDate);
                    }
                } else {
                    for (String column : csvColumns) {
                        if (rowData.containsKey(column)) {
                            mergedValue.append(rowData.get(column)).append(" ");
                        }
                    }
                }

                if (fields.containsKey(pdfField)) {
                    fields.get(pdfField).setValue(mergedValue.toString().trim());
                    fields.get(pdfField).setFontSize(10);
                }
            }

            if (rowData.containsKey("type_of_service")) {
                String typeOfServiceField = rowData.get("type_of_service");
                if (fields.containsKey(typeOfServiceField)) {
                    fields.get(typeOfServiceField).setValue("X");
                }
            }

            if (rowData.containsKey("place_of_service")) {
                String placeOfServiceField = rowData.get("place_of_service");
                if (fields.containsKey(placeOfServiceField)) {
                    fields.get(placeOfServiceField).setValue("X");
                }
            }

            form.flattenFields();
        }
        pdfDocument.close();
    }
}
