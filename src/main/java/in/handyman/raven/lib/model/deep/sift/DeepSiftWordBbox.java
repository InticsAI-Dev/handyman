package in.handyman.raven.lib.model.deep.sift;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Word;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeepSiftWordBbox {

    private String text;
    private int x;
    private int y;
    private int w;
    private int h;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String captureWordBoxesJson(ITesseract tesseract, File imageFile) throws Exception {
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) return "[]";
        List<Word> words = tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD);
        List<DeepSiftWordBbox> boxes = new ArrayList<>();
        for (Word word : words) {
            String text = word.getText() == null ? "" : word.getText().trim();
            if (text.isEmpty()) continue;
            Rectangle r = word.getBoundingBox();
            if (r == null) continue;
            boxes.add(new DeepSiftWordBbox(text, r.x, r.y, r.width, r.height));
        }
        return MAPPER.writeValueAsString(boxes);
    }



    public static List<int[]> resolveAllPhraseBboxes(String wordBoxesJson, String phrase) {
        List<int[]> results = new ArrayList<>();
        if (wordBoxesJson == null || wordBoxesJson.isBlank() || phrase == null || phrase.isBlank()) return results;
        List<DeepSiftWordBbox> boxes;
        try {
            boxes = MAPPER.readValue(wordBoxesJson, new TypeReference<List<DeepSiftWordBbox>>() {});
        } catch (Exception e) {
            return results;
        }
        if (boxes.isEmpty()) return results;

        String[] tokens = norm(phrase).split("\\s+");
        if (tokens.length == 0) return results;

        for (int i = 0; i + tokens.length <= boxes.size(); i++) {
            boolean allMatch = true;
            for (int t = 0; t < tokens.length; t++) {
                String wordText = norm(boxes.get(i + t).getText());
                if (!wordText.equals(tokens[t]) && !wordText.contains(tokens[t])) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                results.add(union(boxes.subList(i, i + tokens.length)));
                i += tokens.length - 1;
            }
        }
        return results;
    }

    private static int[] union(List<DeepSiftWordBbox> group) {
        int x1 = Integer.MAX_VALUE, y1 = Integer.MAX_VALUE, x2 = Integer.MIN_VALUE, y2 = Integer.MIN_VALUE;
        for (DeepSiftWordBbox b : group) {
            x1 = Math.min(x1, b.x);
            y1 = Math.min(y1, b.y);
            x2 = Math.max(x2, b.x + b.w);
            y2 = Math.max(y2, b.y + b.h);
        }
        return new int[]{x1, y1, x2, y2};
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }
}
