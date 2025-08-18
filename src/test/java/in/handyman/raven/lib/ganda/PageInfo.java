package in.handyman.raven.lib.ganda;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
public class PageInfo {
    private int id;
    private int pageNumber;
    private List<TextLine> textLines;
    private Form form;
    private List<Table> tables;
    private List<SelectionElement> selectionElements;
}
