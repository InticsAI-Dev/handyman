package in.handyman.raven.util;

import in.handyman.raven.lambda.process.HRequestResolver;
import org.jasypt.util.text.AES256TextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;


public class PropertyHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyHandler.class);

    private static final String CONFIG_PROPERTIES = "config.properties";
    private static final Map<String, String> PROPS;

    static {

        String configPath = System.getProperty("config.file");
        if (configPath == null || configPath.isEmpty()) {
            LOGGER.info("Config file is not present, loading from default config.properties");
            try (var input = HRequestResolver.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES)) {

                var prop = new Properties();
                //load a properties file from class path, inside static method
                prop.load(input);
                var maps = prop.entrySet().stream().collect(
                        Collectors.toMap(
                                e -> String.valueOf(e.getKey()),
                                e -> String.valueOf(e.getValue()),
                                (prev, next) -> next, HashMap::new
                        ));
                PROPS = Collections.unmodifiableMap(maps);
                LOGGER.info("Successfully loaded properties from config.properties ");
            } catch (IOException e) {
                throw new UncheckedIOException("Sorry, unable to load " + CONFIG_PROPERTIES, e);
            }
        } else {
            final String encryptionPassword = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
            final AES256TextEncryptor aesEncryptor = new AES256TextEncryptor();

            if (encryptionPassword != null && !encryptionPassword.isEmpty()) {
                aesEncryptor.setPassword(encryptionPassword);
            } else {
                LOGGER.error("Encryption password is not set");
            }
            LOGGER.info("config.file is present");
            Map<String, String> tempProps;
            try (InputStream input = new FileInputStream(configPath)) {
                Properties prop = new Properties();
                prop.load(input);
                tempProps = prop.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> String.valueOf(e.getKey()),
                                e -> decryptPropertyByJasypt(String.valueOf(e.getValue()), aesEncryptor),
                                (prev, next) -> next, HashMap::new));
                LOGGER.info("Successfully loaded properties from config.file argument : {}", configPath);
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to load config.properties from path: " + configPath, e);
            }
            PROPS = Collections.unmodifiableMap(tempProps);
        }


    }

    private static String decryptPropertyByJasypt(String encryptedValue, AES256TextEncryptor aesEncryptor) {
        try {
            if (encryptedValue.startsWith("ENC(") && encryptedValue.endsWith(")")) {
                String encryptedText = encryptedValue.substring(4, encryptedValue.length() - 1);
                return aesEncryptor.decrypt(encryptedText);
            }
            return encryptedValue;
        } catch (Exception e) {
            LOGGER.error("Error decrypting property: {}", encryptedValue, e);
            return encryptedValue;
        }
    }

    private PropertyHandler() {
    }

    public static String get(final String key) {
        return PROPS.get(key);
    }

}
