package in.handyman.raven.lib.custom.kvp.post.processing.bsh;

import lombok.ToString;

import java.util.Map;

public class ProviderTransformerOutputItem {
    String key;
    String value;
    Map<String, Object> boundingBox;
    double confidence;
    String sorContainerName;

    public ProviderTransformerOutputItem(String key, String value, Map<String, Object> boundingBox, String sorContainerName) {
        this.key = key;
        this.value = value;
        this.boundingBox = boundingBox;
        this.confidence = 100.0;
        this.sorContainerName = sorContainerName;
    }

    // Getters and setters
    public Map<String, Object> getBoundingBox() {
        return boundingBox;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getSorContainerName() {
        return sorContainerName;
    }

    @Override
    public String toString() {
        return "ProviderTransformerOutputItem{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", boundingBox=" + boundingBox +
                ", confidence=" + confidence +
                ", sorContainerName='" + sorContainerName + '\'' +
                '}';
    }
}