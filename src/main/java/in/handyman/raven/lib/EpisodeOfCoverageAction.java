package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.agadia.eocsplitting.EocIdCoverage;
import in.handyman.raven.lib.agadia.eocsplitting.OriginCoverage;
import in.handyman.raven.lib.agadia.eocsplitting.QrCodeCoverage;
import in.handyman.raven.lib.agadia.eocsplitting.SorItemCoverage;
import in.handyman.raven.lib.model.EpisodeOfCoverage;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "EpisodeOfCoverage"
)
public class EpisodeOfCoverageAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;
    private final EpisodeOfCoverage episodeOfCoverage;

    private final Marker aMarker;

    public EpisodeOfCoverageAction(final ActionExecutionAudit action, final Logger log,
                                   final Object episodeOfCoverage) {
        this.episodeOfCoverage = (EpisodeOfCoverage) episodeOfCoverage;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" EpisodeOfCoverage:" + this.episodeOfCoverage.getName());
    }

    @Override
    public void execute() throws Exception {

        try {
            String name = episodeOfCoverage.getName();
            String eocIdCount = episodeOfCoverage.getEocIdCount();
            log.info(aMarker, "Episode of coverage with group by eoc-id has started name {} eoc_id_count {} ", name, eocIdCount);
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(episodeOfCoverage.getResourceConn());
            final String splittingPrioritySelector = Optional.ofNullable(action.getContext().get("sor.grouping.priority.selector")).orElse("true");

            final Boolean eocActivator = Boolean.valueOf(action.getContext().get("sor.grouping.eid.validator"));
            final Boolean qrActivator = Boolean.valueOf(action.getContext().get("sor.grouping.qid.validator"));
            final Boolean memberIdActivator = Boolean.valueOf(action.getContext().get("sor.grouping.pid.validator"));
            final Boolean patientInfoActivator = Boolean.valueOf(action.getContext().get("sor.grouping.pnd.validator"));


            if (log.isInfoEnabled()) {
                log.info("Sor grouping enabled the priority selector {}", splittingPrioritySelector);
            }
            if (Objects.equals(splittingPrioritySelector, "true")) {
                executeEocIdPriority(eocIdCount, jdbi, eocActivator, qrActivator, memberIdActivator, patientInfoActivator);

            } else {
                executeNonEocIdPriority(eocIdCount, jdbi, eocActivator, qrActivator, memberIdActivator, patientInfoActivator);

            }
        } catch (Exception e) {
            log.error(aMarker, "Episode of coverage Action for {} with group by eoc-id has failed for {}", episodeOfCoverage.getName(), ExceptionUtil.toString(e));
            throw new HandymanException("Error in sor grouping", e, action);
        }

    }

    private void executeEocIdPriority(String eocIdCount, Jdbi jdbi, Boolean eocActivator, Boolean qrActivator, Boolean memberIdActivator, Boolean patientInfoActivator) throws InterruptedException {
        boolean eocIdEliminated = executeEocIdGrouping(eocIdCount, jdbi,eocActivator);
        if (eocIdEliminated) {
            boolean qrEliminated = executeQrCodeGrouping(jdbi,qrActivator);
            if (qrEliminated) {
                boolean memberIdEliminated = executeMemberIdGrouping(jdbi,memberIdActivator);
                if (memberIdEliminated) {
                    boolean nameIdEliminated = executePatientNameGrouping(jdbi,patientInfoActivator);
                    if (nameIdEliminated) {
                        executeNidGrouping(jdbi);
                    }
                }
            }

        }
    }

    private void executeNonEocIdPriority(String eocIdCount, Jdbi jdbi, Boolean eocActivator, Boolean qrActivator, Boolean memberIdActivator, Boolean patientInfoActivator) throws InterruptedException {

        boolean qrEliminated = executeQrCodeGrouping(jdbi,qrActivator);
        if (qrEliminated) {
            boolean eocIdEliminated = executeEocIdGrouping(eocIdCount, jdbi,eocActivator);
            if (eocIdEliminated) {
                boolean memberIdEliminated = executeMemberIdGrouping(jdbi,memberIdActivator);

                if (memberIdEliminated) {
                    boolean nameIdEliminated = executePatientNameGrouping(jdbi,patientInfoActivator);
                    if (nameIdEliminated) {
                        executeNidGrouping(jdbi);
                    }
                }
            }

        }
    }

    private boolean executeEocIdGrouping(String eocIdCount, Jdbi jdbi,Boolean eocActivator) {
        log.info("patient instance check for Eoc id in aggregation count {} is activate {}", eocIdCount, eocActivator);
        if(Boolean.TRUE.equals(eocActivator)){
            EocIdCoverage eocIdCoverage = new EocIdCoverage(log, episodeOfCoverage, aMarker, action);
            Map<String, List<Integer>> sorIdPageNumbers = eocIdCoverage.SplitByEocId(jdbi, "patient_eoc");
            OutputQueryExecutor(jdbi, "EID", sorIdPageNumbers);
            log.info("patient instance checked for Eoc id in aggregation and the output result is {}", sorIdPageNumbers);
            return sorIdPageNumbers.isEmpty();
        }else {
            return true;
        }
    }

    private boolean executeQrCodeGrouping(Jdbi jdbi, Boolean qrActivator) throws InterruptedException {
        log.info("patient instance extract QR code from source of truth table if activated {}",qrActivator);
        if(Boolean.TRUE.equals(qrActivator)){
            QrCodeCoverage qrCodeCoverage = new QrCodeCoverage(log, episodeOfCoverage, aMarker, action);
            Map<String, List<Integer>> qrPagenumbers = qrCodeCoverage.splitByQrcode(jdbi, "qr_code");
            OutputQueryExecutor(jdbi, "QID", qrPagenumbers);
            log.info("patient instance checked for Qrcode from source of truth and the output result is {}", qrPagenumbers);
            return qrPagenumbers.isEmpty();
        }else{
            return true;
        }

    }

    private boolean executeMemberIdGrouping(Jdbi jdbi,Boolean memberIdActivator) {
        log.info("patient instance check for member_id from aggregation if activated {}", memberIdActivator);
        if(Boolean.TRUE.equals(memberIdActivator)){
            SorItemCoverage sorItemMemberIdCoverage = new SorItemCoverage(log, episodeOfCoverage, aMarker, action);
            Map<String, List<Integer>> patientMemberPageNumbers = sorItemMemberIdCoverage.splitBySorItem(jdbi, "patient_member_id");
            OutputQueryExecutor(jdbi, "PID", patientMemberPageNumbers);
            log.info("patient instance check for member_id from aggregation and the output result is {}", patientMemberPageNumbers);
            return patientMemberPageNumbers.isEmpty();
        }else{
            return true;
        }

    }
    private boolean executePatientNameGrouping(Jdbi jdbi, Boolean patientInfoActivator) {
        log.info("patient instance check for patient_name and patient_dob from aggregation if activated {}",patientInfoActivator);
        if(Boolean.TRUE.equals(patientInfoActivator)){
            SorItemCoverage sorItemPatientNameCoverage = new SorItemCoverage(log, episodeOfCoverage, aMarker, action);
            Map<String, List<Integer>> patientNamePageNumbers = sorItemPatientNameCoverage.splitBySorItem(jdbi, "patient_name");
            OutputQueryExecutor(jdbi, "PND", patientNamePageNumbers);
            log.info("patient instance checked for patient_name and patient_dob from aggregation and the output result is {}", patientNamePageNumbers);
            return patientNamePageNumbers.isEmpty();
        }else{
            return true;
        }

    }

    private boolean executeNidGrouping(Jdbi jdbi) {
        log.info("patient instance check for origin id from aggregation ");
        OriginCoverage originCoverage = new OriginCoverage(log, episodeOfCoverage, aMarker, action);
        Map<String, List<Integer>> originIdPageNumbers = originCoverage.aggregateByOrigin(jdbi, episodeOfCoverage.getOriginId());
        OutputQueryExecutor(jdbi, "NID", originIdPageNumbers);
        log.info("patient instance checked for no id from aggregation and the output result is {}", originIdPageNumbers);
        return originIdPageNumbers.isEmpty();
    }

    @Override
    public boolean executeIf() throws Exception {
        return episodeOfCoverage.getCondition();
    }


    public void OutputQueryExecutor(Jdbi jdbi, String sorItem, Map<String, List<Integer>> stringListMap) {
        Long tenantId = Long.valueOf(action.getContext().get("tenant_id"));
        stringListMap.forEach((s, integers) -> {
            for (Integer integer : integers) {
                CoverageEntity coverageEntity = CoverageEntity.builder()
                        .paperNo(integer)
                        .pahubId(s)
                        .originId(episodeOfCoverage.getOriginId())
                        .groupId(Integer.valueOf(episodeOfCoverage.getGroupId()))
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .sourceOfPahub(sorItem)
                        .tenantId(tenantId)
                        .build();
                try {
                    jdbi.useTransaction(handle -> {
                        handle.createUpdate("INSERT INTO " + episodeOfCoverage.getOutputTable() +
                                        "(pahub_id, origin_id, source_of_pahub,group_id,created_on, paper_no,status,stage,message,tenant_id)" +
                                        "VALUES (:pahubId , :originId, :sourceOfPahub ,:groupId,:createdOn, :paperNo, 'COMPLETED', 'SOR_GROUPING', 'sor grouping completed',:tenantId)")
                                .bindBean(coverageEntity).execute();
                    });
                    log.info(aMarker, "Completed insert for the patient instance {}", coverageEntity);

                } catch (Exception e) {
                    log.error(aMarker, "Failed in executed formatted query {} for this sor item {}", e, sorItem);
                    throw new HandymanException("Failed in executed formatted query {} for this sor item {}", e, action);
                }
            }
        });
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CoverageEntity {
        private String pahubId;

        private String originId;
        private Integer groupId;
        private String fileId;
        private Timestamp createdOn;
        private String sourceOfPahub;

        private Integer paperNo;
        private Long tenantId;
    }


}