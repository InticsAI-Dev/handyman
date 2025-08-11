package in.handyman.raven.lib;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class FileMergerPdfActionTest {

    @Test
    void pdfMetaCalculation() {
//        try (PDDocument document = PDDocument.load(new File("/home/manikandan.tm@zucisystems.com/Downloads/original-output/SYNT_166564144_19012024/SYNT_166564144_001.pdf"))) {
//
//            PDPage firstPage = document.getPage(0);
//
//            // Get page size (width and height)
//            float pageWidth = firstPage.getMediaBox().getWidth();
//            float pageHeight = firstPage.getMediaBox().getHeight();
//            System.out.println("pageWidth=" + pageWidth + " pageHeight=" + pageHeight);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }


    @Test
    void pdfBboxCalculation() {
//        try (PDDocument document = PDDocument.load(new File("/home/manikandan.tm@zucisystems.com/Downloads/original-output/SYNT_166564144_19012024/SYNT_166564144_001.pdf"))) {
//
//            PDPage firstPage = document.getPage(0);
//
//            // Get page size (width and height)
//            float pageWidth = firstPage.getMediaBox().getWidth();
//            float pageHeight = firstPage.getMediaBox().getHeight();
//
//            float width_inches = pageWidth / 72;
//            float height_inches = pageHeight / 72;
//
//            int pdf_dpi = (int) (pageWidth / width_inches);
//            System.out.println("pageWidth= " + pageWidth + ", pageHeight= " + pageHeight + ", dpi= " + pdf_dpi);
//            calculateBbox(pageWidth, pageHeight, 471.0f, 697.0f, 568.2f, 731.0f, 1653, 2339);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    private void calculateBbox(float pdfWidth, float pdfHeight, float x1Img, float y1Img, float x2Img, float y2Img, float imageWidth, float imageHeight) {

        float width_scale = pdfWidth / imageWidth;
        float height_scale = pdfHeight / imageHeight;
        System.out.println("width_scale = " + width_scale+ ", height_scale = " + height_scale);


        float x1_pdf, y1_pdf, x2_pdf, y2_pdf;
        x1_pdf = x1Img * width_scale;
        y1_pdf = y1Img * height_scale;
        x2_pdf = x2Img * width_scale;
        y2_pdf = y2Img * height_scale;
        System.out.println("topLeftX = " + x1_pdf+ ", bottomRightY = " + y1_pdf+ ", bottomRightX = " + x2_pdf+ ", topLeftY = " + y2_pdf);
    }
}