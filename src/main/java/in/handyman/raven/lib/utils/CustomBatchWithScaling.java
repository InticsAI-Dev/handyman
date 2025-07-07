package in.handyman.raven.lib.utils;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;

import java.util.List;

public class CustomBatchWithScaling {

    private final Logger logger;

    private final ActionExecutionAudit action;

    public CustomBatchWithScaling(ActionExecutionAudit action, Logger logger) {
        this.action = action;
        this.logger = logger;
    }

    private CoreV1Api getApi() {
        try {
            String namespace = getNamespace();
            if (namespace == null) {
                return null;
            }
            ApiClient client = Config.fromCluster();
            Configuration.setDefaultApiClient(client);
            CoreV1Api coreV1Api = new CoreV1Api(client);
            logger.info("Kubernetes client initialized successfully");
            return coreV1Api;
        } catch (Exception e) {
            logger.info("Failed to reinitialize Kubernetes client: {}", e.getMessage());
            return null;
        }
    }

    private String getNamespace() {
        String namespace = System.getenv("POD_NAMESPACE");
        if (namespace == null || namespace.isEmpty()) {
            logger.info("POD_NAMESPACE env var not set");
            return null;
        }
        logger.info("Using namespace: {}", namespace);
        return namespace;
    }

    public boolean isKubeClientAvailable() {
        CoreV1Api kubeApi = getApi();
        if (kubeApi == null) {
            logger.info("Kubernetes client is not initialized");
            return false;
        }

        try {
            String namespace = getNamespace();
            if (namespace == null) {
                return false;
            }
            kubeApi.listNamespacedPod(namespace).limit(1).execute();
            return true;
        } catch (Exception e) {
            logger.error("Kube client check failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private int getKvpReadyPodCount() {
        String selector = action.getContext().get("kvp.pod.label.selector");
        if (selector == null || selector.trim().isEmpty()) {
            logger.warn("Missing or empty KVP pod label selector");
            return 0;
        }
        return getReadyPodCount("app=" + selector);
    }

    private int getPaperFilterReadyPodCount() {
        String selector = action.getContext().get("paper.filter.pod.label.selector");
        if (selector == null || selector.trim().isEmpty()) {
            logger.warn("Missing or empty paper filter pod label selector");
            return 0;
        }
        return getReadyPodCount("app=" + selector);
    }


    private int getReadyPodCount(String labelSelector) {
        if (!isKubeClientAvailable()) {
            logger.info("Kubernetes client not available — returning 0 Ready pods.");
            return 0;
        }

        try {
            CoreV1Api kubeApi = getApi();
            String namespace = getNamespace();

            if (namespace == null) {
                logger.info("Namespace not set — cannot fetch pods");
                return 0;
            }

            if (kubeApi == null) {
                logger.info("Kubernetes client is not initialized in get ready pod check method");
                return 0;
            }

            List<V1Pod> pods = kubeApi.listNamespacedPod(namespace)
                    .labelSelector(labelSelector)
                    .execute()
                    .getItems();

            long count = pods.stream()
                    .filter(pod -> pod.getStatus() != null && "Running".equalsIgnoreCase(pod.getStatus().getPhase()))
                    .filter(pod -> pod.getStatus().getConditions() != null &&
                            pod.getStatus().getConditions().stream()
                                    .anyMatch(condition ->
                                            "Ready".equalsIgnoreCase(condition.getType()) &&
                                                    "True".equalsIgnoreCase(condition.getStatus())))
                    .count();

            logger.info("Number of Ready pods for label '{}': {}", labelSelector, count);
            return (int) count;

        } catch (Exception e) {
            logger.error("Error while counting Ready pods for label '{}': {}", labelSelector, e.getMessage());
            return 0;
        }
    }


    public int computeSorTransactionApiCount() {
        try {
            int podCount = getKvpReadyPodCount();
            int workerCount = getKvpPodWorkerCount();
            int result = podCount * workerCount;
            logger.info("Computed SOR (KVP) transaction API count: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("Error computing SOR transaction API count", e);
            return 0;
        }
    }

    public int computePaperFilterApiCount() {
        try {
            int podCount = getPaperFilterReadyPodCount();
            int workerCount = getPaperFilterPodWorkerCount();
            int result = podCount * workerCount;
            logger.info("Computed paper filter API count: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("Error computing paper filter API count", e);
            return 0;
        }
    }

    private int getKvpPodWorkerCount() {
        return getWorkerCount("kvp.pod.worker.count", "KVP");
    }

    private int getPaperFilterPodWorkerCount() {
        return getWorkerCount("paper.filter.pod.worker.count", "Paper Filter");
    }

    private int getWorkerCount(String contextKey, String label) {
        String value = action.getContext().get(contextKey);
        if (value == null || value.trim().isEmpty()) {
            logger.warn("Worker count value missing or empty for {} ({})", label, contextKey);
            return 0;
        }
        try {
            int count = Integer.parseInt(value);
            logger.info("Configured worker count for {}: {}", label, count);
            return count;
        } catch (NumberFormatException e) {
            logger.error("Invalid worker count format for {}: '{}'", label, value, e);
            return 0;
        }
    }

    public boolean isPodScalingCheckEnabled() {
        String flag = action.getContext().get("pod.scale.check.activator");
        boolean enabled = Boolean.parseBoolean(flag);
        logger.info("Pod scaling check activator is enabled: {}", enabled);
        return enabled;
    }

}