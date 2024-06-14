package in.handyman.raven.lambda.process;

import com.google.common.io.Resources;
import in.handyman.raven.exception.HandymanException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class HRequestResolver {

    private static final String BASE_PATH = "basepath";

    public static String doResolve(final String lambdaName, final String processLoadType, final Map<String, String> context) {
        final LoadType loadType = LoadType.valueOf(processLoadType);
        if (LoadType.DB == loadType) {
            return dbResolve(context, loadType);
        } else if (LoadType.FILE == loadType) {
            return fileResolve(lambdaName, context, loadType);
        } else {
            throw new HandymanException("Lambda definition for " + lambdaName + " is unknown");
        }
    }

    private static String dbResolve(final Map<String, String> context, final LoadType loadType) {
        return context.get(loadType.getVariable());
    }

    private static String fileResolve(final String lambdaName, final Map<String, String> context, final LoadType loadType) {
        final String filePath = context.getOrDefault(loadType.getVariable(), "");
        final String basePath = context.getOrDefault(BASE_PATH, "");
        if (filePath.isEmpty()) {
            throw new HandymanException("File path configuration for process " + lambdaName + " is missing, check spw_process_config or spw_instance_config");
        }
        return readFile(filePath, basePath);
    }

    protected static String readFile(final String filePath, final String basePath) {
        try {
            return doPathResolve(filePath, basePath);
        } catch (IOException e) {
            throw new HandymanException("Read process definition is failed", e);
        }
    }

    private static String doPathResolve(final String fileRelativePath, final String basePath) throws IOException {
        final String fullPath = (Objects.nonNull(basePath) && !basePath.isEmpty() ? (basePath + "/") : "") + fileRelativePath;
        log.info("Trying to find the process file {}", fullPath);
        final URL finalPathUrl = HRequestResolver.class.getClassLoader().getResource(fullPath);
        final File finalPathInHost = new File(fullPath);
        if (finalPathUrl != null) {
            return Resources.toString(finalPathUrl, StandardCharsets.UTF_8);
        } else if (finalPathInHost.exists()) {
            return Files.readString(Path.of(finalPathInHost.getAbsolutePath()));
        } else {
            throw new HandymanException("Process definition for " + fullPath + " is absent");
        }
    }

    @Getter
    @AllArgsConstructor
    public enum LoadType {
        DB("processContent"), FILE("filepath"), REST("");
        private final String variable;
    }

}
