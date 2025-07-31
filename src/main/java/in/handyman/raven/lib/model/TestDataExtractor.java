package in.handyman.raven.lib.model;

import lombok.Data;
import in.handyman.raven.lambda.action.ActionContext;
import in.handyman.raven.lambda.action.IActionContext;
import java.lang.Boolean;
import java.lang.String;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ActionContext(
        actionName = "DeepSift"
)
public class TestDataExtractor {
  private Boolean condition = true;
  private String name;
  private List<MultipartFile> files;
  private String mode;
  private String outputPath;
  private List<String> keywords;
  private String resourceConn;
  private String resultTable;
  private String endPoint;
  private String processId;
  private String querySet;
}
