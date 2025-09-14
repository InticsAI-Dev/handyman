package in.handyman.raven.lib;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class FuzzyOcrMatch {

    @Test
    public void testFuzzyMatch() {
        String ocrText = "SCV Health new prod\n" +
                "6/5/2025 7:51:19 AM PDT PAGE 3/003 Fax Server\n" +
                "Encounter Date: 6/5/2025\n" +
                "SANTA LARA VALLEY\n" +
                "Hospital Account:\n" +
                "EALTHCARE\n" +
                "MRN:\n" +
                "Contact Serial #:\n" +
                "PATIENT\n" +
                "Name: Cxourtney, Frafnnie\n" +
                "DOB: 8/22/1967 (69 yrs)\n" +
                "Address: 56t Gateway Road\n" +
                "Sex: Male\n" +
                "City: Des Moines IA 50315\n" +
                "Primary Care Provider:\n" +
                "Primary Phone:\n" +
                "EMERGENCY CONTACT\n" +
                "\n" +
                ":\n" +
                "Contact Namc\n" +
                "Legal Guardian? Relationship to Pakicnt Home Phonc\n" +
                "Work Phonc\n" +
                "Mobile\n" +
                "1. Gomez,Jose\n" +
                "No\n" +
                "Brother\n" +
                "209-828-9864\n" +
                "2. Cortez,Lupe\n" +
                "No\n" +
                "Sister\n" +
                "408-729-8227\n" +
                "ENCOUNTER\n" +
                "Patient Class: Inpatient\n" +
                "Unit: OCH 4PCU\n" +
                "Hospital Service: Internal Medicine Hospitalist\n" +
                "Bed: 4213A\n" +
                "Admitling Provider: Lau, Nancy Che Lui, Md\n" +
                "Referring Physician:\n" +
                "Attending Provider: Lau, Nancy Che Lui, Md\n" +
                "Adm Diagnosis: Acnte decompensated heart failure *\n" +
                "GUARANTOR\n" +
                "Guarantor: Cxourtney, Frafnnie\n" +
                "DOB: 8/22/1967\n" +
                "Address: 56t Gateway Road\n" +
                "Sex: Male\n" +
                "Des Moines IA 50315\n" +
                "Relation to Patient: Self\n" +
                "Home Phone:\n" +
                "Work Phone:\n" +
                "Mobile:\n" +
                "Guarantor ID: 123480\n" +
                "669-340-7661\n" +
                "GUARANTOR: EMPLOYER:\n" +
                "Employer:\n" +
                "Status: RETIRED\n" +
                "COVERAGE\n" +
                "PRIMARY: NSURANCE\n" +
                "Payor: MEDICONNECT BLUE CROSS\n" +
                "Plan: MEDICONNECT BLUE CROSS\n" +
                "ADM: 07/18/2025\n" +
                "Responsible Area: Maternity admission\n" +
                "Groupl Number: IAMCRWPO\n" +
                "Subscriber DOB: 8/22/1967\n" +
                "Subscriber Name: Cxourtney, Frafnnie\n" +
                "Claim Address: P.O. BOX 60007\n" +
                "Subscriber ID: 151J56372\n" +
                "LOS ANGELES, CA 90060-0007\n" +
                "Pat. Rel. to Subscriber: Self\n" +
                "Primary Diagnosis Code: C00.0, C90.1\n" +
                "SECONDARY INSURANCE\n" +
                "Payor: MEDICARE MANAGED CARE GEN*\n" +
                "Plan: MEDICARE MANAGED CARE GEN*\n" +
                "Coverage Attn: OPTUM SAN FERNADO\n" +
                "Insurance Type: INDEMNITY\n" +
                "Group Number: IAMCRWP\n" +
                "Subscriber DOB: 8/22/1967\n" +
                "Subscriber Name: Cxourtney, Frafnnie\n" +
                "Claim Address: P.OBOX 60007\n" +
                "Subscriber ID: 151J56372\n" +
                "LOS ANGELES, CA 90060\n" +
                "Pat. Rel. to Subscriber: SELF\n" +
                "CPT Codes: 33786, 33784\n" +
                "Contact Serial! # (3104642866)\n" +
                "June 5, 2025\n" +
                "Chart ID (93135295-OCH ELECTRON-13)\n" +
                "s\n" +
                "RASS\n" +
                "PG:3/3 * RCVD:06/05/2025 10:52:37. AM ET JOB: ct18669591537-20250605075112601-286-24 * CSID:SCV Health new prod *AN:14062800769 DUR:01-25 mm-ss";

        String extractedValue = "Cxourney, Franhnie"; // target value

        // Split only on whitespace
        String[] tokens = ocrText.split("\\s+");

        // Build candidates: single, double, triple consecutive tokens
        List<String> candidates = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            candidates.add(tokens[i]); // single word
            if (i < tokens.length - 1) {
                candidates.add(tokens[i] + " " + tokens[i + 1]); // two-word phrase
            }
            if (i < tokens.length - 2) {
                candidates.add(tokens[i] + " " + tokens[i + 1] + " " + tokens[i + 2]); // three-word phrase
            }
        }

        // Fuzzy match
        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
        double bestScore = 0.0;
        String bestMatch = null;

        for (String candidate : candidates) {
            double score = similarity.apply(extractedValue, candidate);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = candidate;
            }
        }

        if (bestScore > 0.85) {
            System.out.println("Closest match");
        } else {
            System.out.println("No reliable match found.");
        }
    }
}
