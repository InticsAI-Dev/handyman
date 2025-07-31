package in.handyman.raven.lib.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class TestDataExtractor {
  private Boolean condition;
  private String name;
  private List<MultipartFile> files;
  private String mode;
  private String outputPath;
  private List<String> keywords;
}
