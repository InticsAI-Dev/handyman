package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Base64toActualVaue {

    public String base64toActual(String base64Value){

        log.info("Starting Base64 processing...");

        base64Value = base64Value.replaceAll("[^A-Za-z0-9+/=]", "");
        int missingPadding = base64Value.length() % 4;
        log.debug("Sanitized Base64 value");

        if (missingPadding > 0) {
            base64Value += "=".repeat(4 - missingPadding);
            log.debug("Added padding to Base64 value");
        }
        log.info("Base64 processing completed successfully");

        return base64Value;
    }
}
