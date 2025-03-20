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

}
