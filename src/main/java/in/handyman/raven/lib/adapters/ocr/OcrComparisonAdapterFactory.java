package in.handyman.raven.lib.adapters.ocr;

import java.util.HashMap;
import java.util.Map;

public class OcrComparisonAdapterFactory {
    private static final Map<String, OcrComparisonAdapter> registry = new HashMap<>();

    static {
            register(new NameComparisonAdaptor());
            register(new AddressAdaptor());
            register(new IdAdaptor());
    }

    public static void register(OcrComparisonAdapter adapter) {
        registry.put(adapter.getName().toLowerCase(), adapter);
    }



    public static OcrComparisonAdapter getAdapter(String type) {
        return registry.getOrDefault(type.toLowerCase(), registry.get("NAME_ALPHA"));
    }
}