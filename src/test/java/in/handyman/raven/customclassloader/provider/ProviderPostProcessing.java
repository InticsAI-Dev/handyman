package in.handyman.raven.customclassloader.provider;

import bsh.EvalError;
import bsh.Interpreter;
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
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class ProviderPostProcessing {

    @Test
    public void customClassLoader() throws EvalError {
        Map<String,String> updatedPredictionKeyMap=new HashMap<>();
        String sourceCode="";
        String className="";
        Interpreter interpreter = new Interpreter();
        interpreter.eval(sourceCode);
        String classInstantiation = className + " mapper = new " + className + "();";
        interpreter.eval(classInstantiation);
        interpreter.set("predictionKeyMap", updatedPredictionKeyMap);
        interpreter.eval("predictionMap = mapper.doCustomPredictionMapping(predictionKeyMap);");
        Object predictionMapObject = interpreter.get("predictionMap");
        if (predictionMapObject instanceof Map) {
            updatedPredictionKeyMap = (Map<String, String>) predictionMapObject;
            log.info("Updated the prediction map with {} entries for class {}", updatedPredictionKeyMap.size(), className);
        }
    }

    @Test
    void readPdfFields() throws IOException {
        // Load the PDF file
        String pdfPath = "/home/anandh.andrews@zucisystems.com/Downloads/VA_CAID_Precert_Request_Form.pdf";
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
        String pdfPath = "/home/anandh.andrews@zucisystems.com/Downloads/VA_CAID_Precert_Request_Form.pdf";
        String outputPdfPath = "/home/anandh.andrews@zucisystems.com/Downloads/VA_CAID_Precert_Request_Form_2.pdf"; // New PDF path

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
                fields.get(s).setFontSize(12);                   // Set font size
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

    private static final int NUM_PDFS = 5;
    private static final String CSV_FILE_PATH = "/home/anandh.andrews@zucisystems.com/Downloads/generated/VA_CAID_Precert_Request_csv.csv";

    @Test
    void generateFilledPdfDocumentsAndCSV() throws IOException {
        String inputPdfPath = "/home/anandh.andrews@zucisystems.com/Downloads/VA_CAID_Precert_Request_Form.pdf";

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
}
