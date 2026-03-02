package in.handyman.raven.lambda.access;

import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.access.repo.HandymanRepoImpl;
import in.handyman.raven.lambda.doa.config.SpwBshConfig;
import in.handyman.raven.lambda.doa.config.SpwResourceConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ConfigAccess {

    private static final HandymanRepo REPO = new HandymanRepoImpl();

    public static SpwResourceConfig getResourceConfig(final String name) {
        return REPO.getResourceConfig(name);
    }

    public static Map<String, String> getAllConfig(final String pipelineName) {
        return REPO.getAllConfig(pipelineName);
    }

    public static Map<String, String> getCommonConfig() {
        return REPO.getCommonConfig();
    }


    public static Set<String> getPackageAction() {
        return REPO.getPackageAction();
    }

    public static String getLambdaName(final String pipelineName) {
        if (pipelineName != null) {
            if (pipelineName.contains("#")) {
                return pipelineName.substring(0, pipelineName.lastIndexOf("#"));
            }
        }
        return pipelineName;
    }


    public static List<SpwBshConfig> findAllBshClassesByTenantId(Long tenantId) {
        return REPO.findAllBshClassesByTenantId(tenantId);
    }

}
