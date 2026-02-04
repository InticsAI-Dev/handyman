package in.handyman.raven.lib;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DeepSiftCompensationLogicTest {

    @Test
    public void testExample1_NoBlanks_NoCompensation() {
        // Setup: page_range = "6-10, 23-27", page_count = ?? (Example says 10, but
        // ranges go up to 27? Probably meant 10 pages selected logic)
        // Let's create pages 1 to 30.
        List<DeepSiftCompensationLogic.ProcessedPage> allPages = createPages(30, new int[] {}); // No blanks

        List<DeepSiftCompensationLogic.RangeConfig> ranges = new ArrayList<>();
        ranges.add(new DeepSiftCompensationLogic.RangeConfig(6, 10, 1));
        ranges.add(new DeepSiftCompensationLogic.RangeConfig(23, 27, 2));

        List<Integer> result = DeepSiftCompensationLogic.selectPages(allPages, ranges, 0, false);

        // Expected: 6, 7, 8, 9, 10, 23, 24, 25, 26, 27
        List<Integer> expected = Arrays.asList(6, 7, 8, 9, 10, 23, 24, 25, 26, 27);
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void testExample2_BlankInRange1() {
        // Setup: page_range = "6-10, 23-27"
        // Blank pages: 8
        List<DeepSiftCompensationLogic.ProcessedPage> allPages = createPages(30, new int[] { 8 });

        List<DeepSiftCompensationLogic.RangeConfig> ranges = new ArrayList<>();
        ranges.add(new DeepSiftCompensationLogic.RangeConfig(6, 10, 1));
        ranges.add(new DeepSiftCompensationLogic.RangeConfig(23, 27, 2));

        List<Integer> result = DeepSiftCompensationLogic.selectPages(allPages, ranges, 0, false);

        // Expected: Range 1 (6-10) -> 8 is blank. We pick 6, 7, 8(skipped in valid
        // count but kept in output?),
        // Wait, the Requirement Example 2 says:
        // "Range 1 (6-10): 6, 7, 8✗, 9, 10" -> 4 non-blank pages.
        // "Compensation: 11"
        // "Final: 6, 7, 9, 10, 11, 23..." -> Wait, 8 is REMOVED from final?
        // Example output: "Final: 6, 7, 9, 10, 11, 23, 24, 25, 26, 27 (10 pages ✅)"
        // It seems blank page 8 is NOT included in the final set.
        // My Logic implementation included it in `currentRangeSelection` but didn't
        // count it as valid.
        // I should correct the logic to NOT include invalid pages in the result if they
        // are blank and filtered out.
        // BUT, usually we want to keep the original structure?
        // Let's look closely at Example 2 Expected Final: "6, 7, 9, 10, 11...". 8 is
        // missing.
        // So blank pages should be EXCLUDED from the result set if consider_blank_pages
        // is false.

        List<Integer> expected = Arrays.asList(6, 7, 9, 10, 11, 23, 24, 25, 26, 27);
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void testExample3_BlanksInBothRanges() {
        // Blank pages: 8, 9 (Range 1), 25 (Range 2)
        List<DeepSiftCompensationLogic.ProcessedPage> allPages = createPages(30, new int[] { 8, 9, 25 });

        List<DeepSiftCompensationLogic.RangeConfig> ranges = new ArrayList<>();
        ranges.add(new DeepSiftCompensationLogic.RangeConfig(6, 10, 1));
        ranges.add(new DeepSiftCompensationLogic.RangeConfig(23, 27, 2));

        List<Integer> result = DeepSiftCompensationLogic.selectPages(allPages, ranges, 0, false);

        // Expected: 6, 7, 10, 11, 12 (5 pages for R1), 23, 24, 26, 27, 28 (5 pages for
        // R2)
        // 8, 9, 25 skipped.
        List<Integer> expected = Arrays.asList(6, 7, 10, 11, 12, 23, 24, 26, 27, 28);
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void testExample4_EntireRangeBlank() {
        // Blank pages: 6, 7, 8, 9, 10 (Range 1)
        List<DeepSiftCompensationLogic.ProcessedPage> allPages = createPages(30, new int[] { 6, 7, 8, 9, 10 });

        List<DeepSiftCompensationLogic.RangeConfig> ranges = new ArrayList<>();
        ranges.add(new DeepSiftCompensationLogic.RangeConfig(6, 10, 1));
        ranges.add(new DeepSiftCompensationLogic.RangeConfig(23, 27, 2));

        List<Integer> result = DeepSiftCompensationLogic.selectPages(allPages, ranges, 0, false);

        // Expected: 11, 12, 13, 14, 15 (Comp for R1), 23, 24, 25, 26, 27 (R2)
        List<Integer> expected = Arrays.asList(11, 12, 13, 14, 15, 23, 24, 25, 26, 27);
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void testExample5_FieldPaperCountLimit() {
        // Setup: range 6-10 (5 pages), but we only want 2 pages.
        List<DeepSiftCompensationLogic.ProcessedPage> allPages = createPages(30, new int[] {});

        List<DeepSiftCompensationLogic.RangeConfig> ranges = new ArrayList<>();
        ranges.add(new DeepSiftCompensationLogic.RangeConfig(6, 10, 1));

        // Limit to 2 pages
        List<Integer> result = DeepSiftCompensationLogic.selectPages(allPages, ranges, 2, false);

        // Expected: 6, 7 (only 2 pages)
        List<Integer> expected = Arrays.asList(6, 7);
        Assertions.assertEquals(expected, result);
    }

    private List<DeepSiftCompensationLogic.ProcessedPage> createPages(int count, int[] blanks) {
        List<DeepSiftCompensationLogic.ProcessedPage> pages = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            boolean isBlank = false;
            for (int b : blanks) {
                if (b == i) {
                    isBlank = true;
                    break;
                }
            }
            pages.add(new DeepSiftCompensationLogic.ProcessedPage(i, isBlank));
        }
        return pages;
    }
}
