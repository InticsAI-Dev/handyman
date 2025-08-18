package in.handyman.raven.lib.bsh.provider;

import in.handyman.raven.lib.custom.kvp.post.processing.bsh.ProviderTransformerFinal;
import in.handyman.raven.lib.custom.kvp.post.processing.bsh.ProviderTransformerOutputItem;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
class ProviderProcessorTest {
    public static void main(String[] args) {
        ProviderTransformerFinal processor = new ProviderTransformerFinal(log);

        // Create the provider data as provided
        List<Map<String, String>> providers = new ArrayList<>();


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

        // Process the providers
        List<ProviderTransformerOutputItem> results = processor.processProviders(providers);

        // Display results
        System.out.println("\nProcessed Provider Results:");
        for (ProviderTransformerOutputItem item : results) {
            System.out.println(item.toString());
        }
    }
}