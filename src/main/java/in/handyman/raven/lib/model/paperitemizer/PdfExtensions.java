package in.handyman.raven.lib.model.paperitemizer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PdfExtensions {
    EXTENSION_PDF("pdf"),EXTENSION_JPG("jpg"),EXTENSION_JPEG("jpeg"), EXTENSION_PNG("png");

    private final String eType;
}
