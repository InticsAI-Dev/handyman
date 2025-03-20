package in.handyman.raven.datagen;

import com.github.javafaker.Faker;
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
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class SynthDataGeneration {


    @Test
    void readPdfFields() throws IOException {
        // Load the PDF file
        String pdfPath = "/home/anandh.andrews@zucisystems.com/Downloads/Prod-template/Additional_Template/ARAR_CAID_BH_AutismSpectrumDisorderTesting.pdf";
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath));

        // Extract AcroForm Fields
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDocument, false);
        if (form != null) {
            Map<String, PdfFormField> fields = form.getFormFields();
            for (Map.Entry<String, PdfFormField> entry : fields.entrySet()) {
                System.out.println("Field Name: " + entry.getKey());
                System.out.println("Field Value: " + entry.getValue().getValueAsString());
                System.out.println("------------------------------");
            }
        } else {
            System.out.println("No form fields found in the PDF.");
        }

        // Close the document
        pdfDocument.close();
    }

    @Test
    void fillPdfAndExport() throws IOException {
        // Load the original PDF
        String pdfPath = "/home/anandh.andrews@zucisystems.com/Downloads/VA_CAID_Precert_Request_Form_Template.pdf";
        String outputPdfPath = "/home/anandh.andrews@zucisystems.com/Downloads/VA_CAID_Precert_Request_Form_Template_output_1.pdf"; // New PDF path

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath), new PdfWriter(outputPdfPath));

        // Extract AcroForm Fields
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDocument, true);

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);


        if (form != null) {
            Map<String, PdfFormField> fields = form.getFormFields();
            System.out.println(fields.size());
            fields.forEach((s, pdfFormField) -> {
                fields.get(s).setValue(s);
                fields.get(s).setFont(font);                     // Set font
                fields.get(s).setFontSize(8);                   // Set font size
                fields.get(s).setColor(ColorConstants.BLUE);     // Set text color
//                System.out.println(pdfFormField.getValue());

            });
            // Find and update the "Full Name" field
//            if (fields.containsKey("Full name")) {
//                fields.get("Full name").setValue("John Doe");
//                System.out.println("Updated 'Full name' field.");
//            } else {
//                System.out.println("'Full name' field not found.");
//            }
        } else {
            System.out.println("No form fields found in the PDF.");
        }

        // Flatten the form (optional: makes the values non-editable)
        form.flattenFields();

        // Close the document
        pdfDocument.close();
        System.out.println("PDF saved at: " + outputPdfPath);
    }

    @Test
    void fillAllPdfsInFolderAndExport() throws IOException {
        String inputFolderPath = "/home/anandh.andrews@zucisystems.com/Downloads/Prod-template/";
        String outputFolderPath = "/home/anandh.andrews@zucisystems.com/Downloads/Prod-template-output/";

        File inputFolder = new File(inputFolderPath);
        File outputFolder = new File(outputFolderPath);

        // Ensure output folder exists
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        // List all PDF files in input folder
        File[] pdfFiles = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (pdfFiles == null || pdfFiles.length == 0) {
            System.out.println("No PDFs found in input folder.");
            return;
        }

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        for (File pdfFile : pdfFiles) {
            String inputPdfPath = pdfFile.getAbsolutePath();
            String outputPdfPath = outputFolderPath + pdfFile.getName(); // Save with same filename

            PdfDocument pdfDocument = new PdfDocument(new PdfReader(inputPdfPath), new PdfWriter(outputPdfPath));
            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDocument, true);

            if (form != null) {
                Map<String, PdfFormField> fields = form.getFormFields();
                System.out.println("Processing: " + pdfFile.getName() + " | Fields: " + fields.size());

                fields.forEach((fieldName, field) -> {
                    field.setValue(fieldName); // Set field value to its name (change this as needed)
                    field.setFont(font);
                    field.setFontSize(12);
                    field.setColor(ColorConstants.BLUE);
                });

                form.flattenFields(); // Make fields non-editable
            } else {
                System.out.println("No form fields found in " + pdfFile.getName());
            }

            pdfDocument.close();
            System.out.println("Processed: " + pdfFile.getName());
        }

        System.out.println("All PDFs processed and saved to: " + outputFolderPath);
    }

    @Test
    void fillAllPdfsInFolderAndExport1() throws IOException {
//        String inputFolderPath = "/home/anandh.andrews@zucisystems.com/Downloads/Prod-template/Additional_Template/";
//        String outputFolderPath = "/home/anandh.andrews@zucisystems.com/Downloads/ProcessedPdfs/";

        String inputFolderPath = "/home/anandh.andrews@zucisystems.com/Downloads/Prod-template/Additional_Template/";
        String outputFolderPath = "/home/anandh.andrews@zucisystems.com/Downloads/Prod-template-output/Additional_Template/";

        File inputFolder = new File(inputFolderPath);
        File outputFolder = new File(outputFolderPath);

        // Ensure output folder exists
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        // List all PDF files in input folder
        File[] pdfFiles = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (pdfFiles == null || pdfFiles.length == 0) {
            System.out.println("No PDFs found in input folder.");
            return;
        }

        for (File pdfFile : pdfFiles) {
            String inputPdfPath = pdfFile.getAbsolutePath();
            String outputPdfPath = outputFolderPath + pdfFile.getName(); // Save with same filename

            // Open PDF for reading and writing
            PdfDocument pdfDocument = new PdfDocument(new PdfReader(inputPdfPath), new PdfWriter(outputPdfPath));
            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDocument, true);

            // Create a new font for each document (Fixes PdfException)
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            if (form != null) {
                Map<String, PdfFormField> fields = form.getFormFields();
                System.out.println("Processing: " + pdfFile.getName() + " | Fields: " + fields.size());

                fields.forEach((fieldName, field) -> {
                    field.setValue(fieldName); // Set field value to its name (modify as needed)
                    field.setFont(font); // Assign the font unique to this document
                    field.setFontSize(12);
                    field.setColor(ColorConstants.BLUE);
                });

                form.flattenFields(); // Make fields non-editable
            } else {
                System.out.println("No form fields found in " + pdfFile.getName());
            }

            pdfDocument.close();
            System.out.println("Processed: " + pdfFile.getName());
        }

        System.out.println("All PDFs processed and saved to: " + outputFolderPath);
    }


    private static final int NUM_PDFS = 10;
    private static final String CSV_FILE_PATH = "/home/anandh.andrews@zucisystems.com/Downloads/generated/VA_CAID_Precert_Request_csv.csv";

    @Test
    void generateFilledPdfDocumentsAndCSV() throws IOException {
        String inputPdfPath = "/home/anandh.andrews@zucisystems.com/Downloads/Prod-template/Additional_Template/ARAR_CAID_BH_AutismSpectrumDisorderTesting.pdf";

        // List to store all generated data for CSV writing
        List<String[]> csvDataList = new ArrayList<>();

        // Headers for CSV
        String[] headers = {
                "member_medicaid_id", "member_zipcode", "member_last_name", "member_first_name",
                "member_phone", "member_gender", "member_state", "member_city",
                "member_date_of_birth", "member_group_id", "member_id", "member_age",
                "member_address_line1", "member_full_name"
        };
        csvDataList.add(headers);  // Add headers to CSV data

        // Loop to create multiple filled PDFs
        for (int i = 1; i <= NUM_PDFS; i++) {
            String outputPdfPath = "/home/anandh.andrews@zucisystems.com/Downloads/generate/VA_CAID_Filled_" + i + ".pdf";

            // Generate Fake Data
            Map<String, String> fakeData = generateFakeData();

            // Fill PDF
            fillPdfWithData(inputPdfPath, outputPdfPath, fakeData);
            System.out.println("Generated PDF: " + outputPdfPath);

            // Convert map values to array for CSV
            csvDataList.add(fakeData.values().toArray(new String[0]));
        }

        // Write data to CSV
        writeDataToCsv(csvDataList);
        System.out.println("CSV file saved at: " + CSV_FILE_PATH);
    }

    private void fillPdfWithData(String inputPdfPath, String outputPdfPath, Map<String, String> fakeData) throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(inputPdfPath), new PdfWriter(outputPdfPath));
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDocument, true);

        if (form != null) {
            Map<String, PdfFormField> fields = form.getFormFields();
            for (Map.Entry<String, PdfFormField> entry : fields.entrySet()) {
                String fieldName = entry.getKey();
                if (fakeData.containsKey(fieldName)) {
                    entry.getValue().setValue(fakeData.get(fieldName));
                }
            }
        } else {
            System.out.println("No form fields found in the PDF.");
        }

        // Flatten fields (optional, makes fields non-editable)
        form.flattenFields();
        pdfDocument.close();
    }

    private Map<String, String> generateFakeData() {
        Faker faker = new Faker();
        Map<String, String> fakeData = new LinkedHashMap<>(); // LinkedHashMap to maintain order
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-dd-MM");
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-dd-MM");
        LocalDate birthday = faker.date().birthday(18, 85).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        fakeData.put("Text5", faker.idNumber().valid());
        fakeData.put("Text7", faker.address().zipCode());
        fakeData.put("Text4", faker.name().lastName());
        fakeData.put("Text3", faker.name().firstName());
        fakeData.put("Text9", faker.phoneNumber().cellPhone());
        fakeData.put("Text8", birthday.format(formatter));

        fakeData.put("Text6", faker.address().streetAddress());


        return fakeData;
    }

    private void writeDataToCsv(List<String[]> csvDataList) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_PATH))) {
            writer.writeAll(csvDataList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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
                for (String column : csvColumns) {
                    if (rowData.containsKey(column)) {
                        mergedValue.append(rowData.get(column)).append(" ");
                    }
                }
                if (fields.containsKey(pdfField)) {
                    fields.get(pdfField).setValue(mergedValue.toString().trim());
                    fields.get(pdfField).setFontSize(10);
                }
            }
            form.flattenFields();
        }
        pdfDocument.close();
    }




}
