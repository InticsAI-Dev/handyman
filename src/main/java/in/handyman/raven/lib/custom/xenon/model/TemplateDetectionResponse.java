package in.handyman.raven.lib.custom.xenon.model;

import java.util.List;

public class TemplateDetectionResponse {

    private List<XenonResult> attributes;
    private Integer imageWidth;
    private Integer imageHeight;
    private Integer imageDPI;
    private String extractedImageUnit;

    public List<XenonResult> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<XenonResult> attributes) {
        this.attributes = attributes;
    }

    public Integer getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(Integer imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Integer getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(Integer imageHeight) {
        this.imageHeight = imageHeight;
    }

    public Integer getImageDPI() {
        return imageDPI;
    }

    public void setImageDPI(Integer imageDPI) {
        this.imageDPI = imageDPI;
    }

    public String getExtractedImageUnit() {
        return extractedImageUnit;
    }

    public void setExtractedImageUnit(String extractedImageUnit) {
        this.extractedImageUnit = extractedImageUnit;
    }

}


