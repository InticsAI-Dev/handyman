package in.handyman.raven.lib.adapters.comparison;

import java.util.HashMap;
import java.util.Map;

public class ComparisonAdapterFactory {
    private static final Map<String, ComparisonAdapter> registry = new HashMap<>();
    static {
        register(new DateComparisonAdapter());
        register(new GenderComparisonAdapter());
        register(new SimilarityComparisonAdapter());
        register(new ContainsComparisonAdapter());// fallback
    }

    public static void register(ComparisonAdapter adapter) {
        registry.put(adapter.getName().toLowerCase(), adapter);
    }

    public static ComparisonAdapter getAdapter(String type) {
        return registry.getOrDefault(type.toLowerCase(), registry.get("string"));
    }
}
