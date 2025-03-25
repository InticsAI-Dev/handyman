package in.handyman.raven.lib.bsh.latest;

import bsh.Interpreter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Slf4j
public class ProviderBshLatestOne {
    private static final Logger logger = LoggerFactory.getLogger(ProviderBshLatestOne.class);

    /**
     * Script content that will be evaluated by BeanShell
     */
    private String getProviderProcessorScript() {

        return "";
    }

    public List executeProviderProcessing() {
        try {
            // Create mock input data
            List providers = new Vector();

            Map provider1 = new Hashtable();
            provider1.put("Provider Type", "service provider");
            provider1.put("Provider Name", "Jonathan Wells");
            provider1.put("Provider NPI", "1891784518");
            provider1.put("Provider Specialty", "");
            provider1.put("Provider Tax ID", "672140448");
            provider1.put("Provider Address", "");
            provider1.put("Provider City", "");
            provider1.put("Provider State", "");
            provider1.put("Provider ZIP Code", "");

            Map provider2 = new Hashtable();
            provider2.put("Provider Type", "referre provider");
            provider2.put("Provider Name", "Michael Carter");
            provider2.put("Provider NPI", "");
            provider2.put("Provider Specialty", "");
            provider2.put("Provider Tax ID", "");
            provider2.put("Provider Address", "835 Spring Creek Rd Ste 100, CHATTANOOGA,TN,37412-3994");
            provider2.put("Provider City", "CHATTANOOGA");
            provider2.put("Provider State", "TN");
            provider2.put("Provider ZIP Code", "37412-3994");

            Map provider3 = new Hashtable();
            provider3.put("Provider Type", "service facility");
            provider3.put("Provider Name", "Daniel Patterson");
            provider3.put("Provider NPI", "1891784518");
            provider3.put("Provider Specialty", "");
            provider3.put("Provider Tax ID", "672140448");
            provider3.put("Provider Address", "");
            provider3.put("Provider City", "");
            provider3.put("Provider State", "");
            provider3.put("Provider ZIP Code", "");

            Map provider4 = new Hashtable();
            provider4.put("Provider Type", "information");
            provider4.put("Provider Name", "Samuel Green");
            provider4.put("Provider NPI", "1891784518");
            provider4.put("Provider Specialty", "");
            provider4.put("Provider Tax ID", "672140448");
            provider4.put("Provider Address", "");
            provider4.put("Provider City", "NEW YORK");
            provider4.put("Provider State", "");
            provider4.put("Provider ZIP Code", "");


            providers.add(provider1);
            providers.add(provider2);
            providers.add(provider3);
            providers.add(provider4);

            // Get the script
            String sourceCode = getProviderProcessorScript();
            logger.info("Starting provider processing with mock data");

            // Initialize the BeanShell interpreter
            Interpreter interpreter = new Interpreter();

            // Set variables before evaluating the script


            // Evaluate the script
            interpreter.eval(sourceCode);
            interpreter.set("logger",log);
            String classInstantiation = "ProcessProvider" + " mapper = new " + "ProcessProvider" + "(logger);";
            interpreter.eval(classInstantiation);
            interpreter.set("providers", providers);
            interpreter.eval("providerMap = mapper.processProviders(providers);");
            Object providerMapObject = interpreter.get("providerMap");


            if (providerMapObject instanceof List) {
                List resultList = (List) providerMapObject;

                return resultList;
            } else {
                logger.error("Result is not a List: {}", providerMapObject);
                return new Vector();
            }
        } catch (Exception e) {
            logger.error("Error executing BeanShell script: {}", e.getMessage(), e);
            return new Vector();
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        ProviderBshLatestOne executor = new ProviderBshLatestOne();
        List result = executor.executeProviderProcessing();
        for (int i = 0; i < result.size(); i++) {
            Object item = result.get(i);
            System.out.println(item.toString());  // Assuming OutputItem has a toString() method
        }
    }
}
