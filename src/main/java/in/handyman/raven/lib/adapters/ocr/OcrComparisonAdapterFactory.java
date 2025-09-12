package in.handyman.raven.lib.adapters.ocr;

import java.util.HashMap;
import java.util.Map;

public class OcrComparisonAdapterFactory {
    private static final Map<String, OcrComparisonAdapter> registry = new HashMap<>();

    static {
            register(new NameComparisonAdaptor());  // Assuming corrected
            register(new AddressAdaptor());
            register(new IdAdaptor());  // Add this for member_id
            // Add more default adapters as needed\
    }

    public static void register(OcrComparisonAdapter adapter) {
        registry.put(adapter.getName().toLowerCase(), adapter);
    }



    public static OcrComparisonAdapter getAdapter(String type) {
        return registry.getOrDefault(type.toLowerCase(), registry.get("alpha"));
    }
}