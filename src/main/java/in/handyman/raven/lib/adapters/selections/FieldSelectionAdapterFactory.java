package in.handyman.raven.lib.adapters.selections;

import in.handyman.raven.lib.adapters.comparison.*;

import java.util.HashMap;
import java.util.Map;

public class FieldSelectionAdapterFactory {
    private static final Map<String, FieldSelectionAdapter> registry = new HashMap<>();
    static {
        register(new BlacklistFilterAdapter());
        register(new WhitelistFilterAdapter());
    }

    public static void register(FieldSelectionAdapter adapter) {
        registry.put(adapter.getName().toLowerCase(), adapter);
    }

    public static FieldSelectionAdapter getAdapter(String type) {
        return registry.getOrDefault(type.toLowerCase(), registry.get("blacklist"));
    }
}
