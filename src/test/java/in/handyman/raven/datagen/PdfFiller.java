package in.handyman.raven.datagen;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
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

    public static void main(String[] args) throws IOException, CsvException {
        String csvFilePath = "/home/anandh.andrews@zucisystems.com/Downloads/Return_Fax_Form.csv"; // CSV file path
        String templatePdfPath = "template.pdf"; // PDF template path
        String outputFolder = "output_pdfs/"; // Folder to save PDFs

        // Define CSV to PDF field mapping
        Map<String, String> csvToPdfMap = new HashMap<>();
        csvToPdfMap.put("member_id", "MemberID");
        csvToPdfMap.put("member_full_name", "FullName");
        csvToPdfMap.put("member_phone", "PhoneNumber");
        csvToPdfMap.put("member_date_of_birth", "DOB");
        csvToPdfMap.put("member_address_line1", "Address");

        // Read CSV File
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            List<String[]> csvData = reader.readAll();
            String[] headers = csvData.get(0); // First row is headers

            // Process each row (excluding the header)
            for (int i = 1; i < csvData.size(); i++) {
                String[] row = csvData.get(i);
                Map<String, String> rowData = new HashMap<>();
                String outputPdfName = "default.pdf"; // Default name in case docid is missing

                for (int j = 0; j < headers.length; j++) {
                    if (headers[j].equalsIgnoreCase("docid")) {
                        outputPdfName = row[j] + ".pdf"; // Use docid as PDF file name
                    } else {
                        rowData.put(headers[j], row[j]); // Store CSV row data
                    }
                }

                // Generate the PDF for this row
                fillPdfWithData(templatePdfPath, outputFolder + outputPdfName, rowData, csvToPdfMap);
                System.out.println("Generated: " + outputPdfName);
            }
        }
    }

    private static void fillPdfWithData(String inputPdfPath, String outputPdfPath, Map<String, String> rowData, Map<String, String> csvToPdfMap) throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(inputPdfPath), new PdfWriter(outputPdfPath));
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDocument, true);

        if (form != null) {
            Map<String, PdfFormField> fields = form.getFormFields();
            for (Map.Entry<String, String> entry : csvToPdfMap.entrySet()) {
                String csvColumn = entry.getKey();
                String pdfField = entry.getValue();

                if (fields.containsKey(pdfField) && rowData.containsKey(csvColumn)) {
                    fields.get(pdfField).setValue(rowData.get(csvColumn));
                }
            }
            form.flattenFields(); // Make fields non-editable
        }

        pdfDocument.close();
    }
}
