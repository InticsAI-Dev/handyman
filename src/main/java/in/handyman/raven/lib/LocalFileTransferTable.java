package in.handyman.raven.lib;

import jakarta.json.Json;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;


    @Getter
    @Setter
    @Entity
    @Table(schema = "onboard_wizard_info", name = "local_directory_info")
//    @Builder
//    @RequiredArgsConstructor
//    @AllArgsConstructor
//    @NoArgsConstructor

    public class LocalFileTransferTable  {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private Long tenantId;
        private Json directoryFileList;
        private String sourceDirectoryPath;
        private String destinationDirectoryPath;
        private Integer rootPipelineId;
        private String message;
        private LocalDateTime lastProcessedOn;
        private boolean isdirectoryexists;



}
