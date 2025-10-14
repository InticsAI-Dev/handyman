package in.handyman.raven.lib.adapters.selections;

import java.util.List;

public interface FieldSelectionAdapter {
    List<ExtractedField> filter(List<ExtractedField> fields);
    String getName();

}
