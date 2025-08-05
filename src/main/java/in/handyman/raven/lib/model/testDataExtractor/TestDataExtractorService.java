package in.handyman.raven.lib.model.testDataExtractor;

import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import in.handyman.raven.lib.model.*;
import in.handyman.raven.lib.model.testDataExtractor.TestDataExtractorUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TestDataExtractorService {
    private final Tesseract tesseract;
    private final TestDataExtractorUtil util;

    public TestDataExtractorService() {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
        this.tesseract.setLanguage("eng");
        this.tesseract.setOcrEngineMode(TessAPI.TessOcrEngineMode.OEM_LSTM_ONLY);
        this.util = new TestDataExtractorUtil();
    }

    public void processTextExtraction(List<String> inputFilePaths, String outputPath) throws IOException, TesseractException {
        List<TesseractResponseDto> documentResults = new ArrayList<>();

        for (String filePath : inputFilePaths) {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IOException("File does not exist: " + filePath);
            }
            if (!filePath.toLowerCase().endsWith(".jpg") && !filePath.toLowerCase().endsWith(".jpeg")) {
                throw new IllegalArgumentException("Input file is not a JPEG: " + filePath);
            }
            TesseractResponseDto result = processDocument(file);
            documentResults.add(result);
        }

        TesseractBatchResponseDto response = TesseractBatchResponseDto.builder()
                .documents(documentResults)
                .build();

        util.saveOutput(response, outputPath);
    }

    public void processKeywordExtraction(List<String> inputFilePaths, List<String> keywords, String outputPath)
            throws IOException, TesseractException {
        List<String> effectiveKeywords = keywords != null ? keywords : getPredefinedKeywords();
        Trie trie = Trie.builder()
                .onlyWholeWords()
                .caseInsensitive()
                .addKeywords(effectiveKeywords)
                .build();

        List<DocumentMatchResultDto> matchedDocs = new ArrayList<>();

        for (String filePath : inputFilePaths) {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IOException("File does not exist: " + filePath);
            }
            if (!filePath.toLowerCase().endsWith(".jpg") && !filePath.toLowerCase().endsWith(".jpeg")) {
                throw new IllegalArgumentException("Input file is not a JPEG: " + filePath);
            }
            TesseractResponseDto result = processDocument(file);

            Map<Integer, List<KeywordMatchDto>> groupedMatches = new LinkedHashMap<>();

            for (Map.Entry<String, String> entry : result.getTextByPage().entrySet()) {
                int pageNo = Integer.parseInt(entry.getKey().replace("page", ""));
                String pageText = entry.getValue();

                List<KeywordMatchDto> matchedKeywords = new ArrayList<>();
                for (Emit emit : trie.parseText(pageText)) {
                    matchedKeywords.add(KeywordMatchDto.builder()
                            .originalKeyword(emit.getKeyword())
                            .extractionFromDocument(emit.getKeyword())
                            .build());
                }

                if (!matchedKeywords.isEmpty()) {
                    groupedMatches.put(pageNo, matchedKeywords);
                }
            }

            List<PageMatchGroupDto> pageMatchGroups = groupedMatches.entrySet().stream()
                    .map(entry -> PageMatchGroupDto.builder()
                            .paperNo(entry.getKey())
                            .matches(entry.getValue())
                            .build())
                    .collect(Collectors.toList());

            DocumentMatchResultDto.DocumentMatchResultDtoBuilder docBuilder = DocumentMatchResultDto.builder()
                    .fileName(result.getFileName())
                    .totalPages(result.getPages());

            if (pageMatchGroups.isEmpty()) {
                docBuilder.paperNoMatches(new ArrayList<>())
                        .message("no auth keyword is present");
            } else {
                docBuilder.paperNoMatches(pageMatchGroups);
            }

            matchedDocs.add(docBuilder.build());
        }

        KeywordMatchResponsePayload response = KeywordMatchResponsePayload.builder()
                .status("success")
                .documents(matchedDocs)
                .build();

        util.saveOutput(response, outputPath);
    }

    private TesseractResponseDto processDocument(File file) throws IOException, TesseractException {
        Map<String, String> extractedTexts;
        String fileName = file.getName();

        // Only process JPEG files
        String text = tesseract.doOCR(file);
        extractedTexts = new LinkedHashMap<>();
        extractedTexts.put("page1", text);

        return TesseractResponseDto.builder()
                .fileName(fileName)
                .pages(extractedTexts.size())
                .textByPage(extractedTexts)
                .build();
    }

    private List<String> getPredefinedKeywords() {
        return List.of(
                "ABA", "Acute Detox", "Acute Detoxification", "Acute Psych", "Acute psychiatric inpatient",
                "Acute Rehab", "Acute RTC", "Acute SA Rehab", "adult life skills", "Adult Mental Health Rehab",
                "AMHR", "Applied Behavioral Analysis", "ASAM 2.1", "ASAM 2.5", "ASAM 2.7", "ASAM 3.1", "ASAM 3.3",
                "ASAM 3.5", "ASAM 3.7", "ASAM 4.0", "ASAM 4.0 or Any", "ASD", "Atlanticare Regional Medical Center",
                "Aurora", "Autism Spectrum Disorder", "Behavior Therapy", "Bellin, Catalpa Health", "Catalpa Health",
                "Center for Discovery", "Children's Crisis Residence", "Christ Hospital", "Christian Family Solutions",
                "Core Treatment Services", "CRC", "Crisis Residence", "crisis Risk assessment and intervention",
                "Crisis Stab", "Crisis Stabilization", "Crisis Stabilization,", "CSM", "CSU", "Day Treatment",
                "Day Tx", "Dominion Hospital Reflections", "Essentia Health Duluth", "facility based crisis",
                "facility based crisis S9484", "Family Services", "Gunderson Lutheran", "IHS", "In-Home Services",
                "Inpatient Detox", "Inpatient Psych", "Inpatient psychiatric", "Inpatient psychiatric rehab",
                "Inpatient Substance Use Disorder Treatment", "Intensive Crisis Residence", "IOP", "IOP mental health",
                "IOP substance abuse", "IOP substance use disorder", "IP Detox", "IP Psych", "Iris", "LOCADTR",
                "LOCADTR or Any", "Mayo Clinic", "MCHS Eau Clair", "Meriter", "mobile crisis", "Monte Nido",
                "New Bridge Medical Center", "Northwest Journey", "op resi", "Out of Network Override request Form",
                "outpatient residential treatment", "Partial Hospitalization Substance Abuse",
                "Partial Hospitilization Psych", "Pathways", "PHP", "PHP Psych", "PHP Substance Abuse", "PIC",
                "Pine Counseling", "PMIC", "PRTF", "PSR", "Psychiatric Intensive Care",
                "Psychiatric Medical Institutions for Children", "psychiatric residential treatment facilities",
                "Psychiatric residential treatment facility", "Psychiatric RTC", "psychosocial rehab",
                "psychosocial rehabilitation", "QRTP", "qualified residential treatment program", "rehab day",
                "Residential", "Residential Crisis Support", "Residential/Inpatient Substance Use Disorder Treatment",
                "Rogers", "RTC", "RTC Psych", "RTC SA", "RTC SUD", "St. Agnes", "St. Clare's Hospital", "St. Elizabeth",
                "St. Mary Ozaukee", "substance abuse residential treatment", "Substance use disorder services", "SUD",
                "SUD RTC", "Tarrant County (MHMR)", "Tarrant County (MHMR) or Any", "Telecare", "Tellurian", "TGH",
                "ThedaCare", "Therapeutic Group Home", "therapeutic repetitive transcranial magnetic stimulation",
                "TLS Behavioral Crisis Resource Center", "TMS", "Transcranial Magnetic Stimulation", "Veritas",
                "Willow Creek", "Wood County Human Services", "URGENT", "urgent"
        );
    }
}