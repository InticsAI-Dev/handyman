package in.handyman.raven.lib.agadia.eocsplitting;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.EpisodeOfCoverage;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.*;

public class OriginCoverage {

    private final Logger log;

    private final EpisodeOfCoverage episodeOfCoverage;

    private final Marker aMarker;


    private final ActionExecutionAudit action;

    public OriginCoverage(Logger log, EpisodeOfCoverage episodeOfCoverage, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.episodeOfCoverage = episodeOfCoverage;
        this.aMarker = aMarker;
        this.action = action;
    }


    public Map<String, List<Integer>> aggregateByOrigin(Jdbi jdbi, String originId) {
        Map<String, List<Integer>> stringObjectMap = new HashMap<>();
        String inputQuery = "SELECT max(paper_no) from info.source_of_truth where origin_id = '" + originId + "'";
        final Integer maxPaperNo = jdbi.withHandle(handle -> handle.createQuery(inputQuery).mapTo(Integer.class).one());
        List<Integer> paperNos = getPaperNos(maxPaperNo);
        final String fileName = getFileName(jdbi, originId);
        final String fileNameIdentifier = fileName.concat("_NID");
        stringObjectMap.put(fileNameIdentifier, paperNos);
        return stringObjectMap;
    }

    private List<Integer> getPaperNos(Integer maxPaperNo) {
        List<Integer> paperNoList = new ArrayList<>();
        final int startPage = 1;
        for (int index = startPage; index < maxPaperNo; index++) {
            paperNoList.add(index);
        }
        return paperNoList;
    }

    private String getFileName(Jdbi jdbi, String originId) {
        try {
            String fileIdQuery = "SELECT file_id from info.source_of_origin where origin_id = '" + originId + "'";
            final String fileId = jdbi.withHandle(handle -> handle.createQuery(fileIdQuery).mapTo(String.class).one());
            String fileNameQuery = "SELECT file_name from info.asset where origin_id = '" + fileId + "'";
            return jdbi.withHandle(handle -> handle.createQuery(fileNameQuery).mapTo(String.class).one());
        } catch (Exception e) {
            throw new HandymanException(String.valueOf(e));
        }
    }

}
