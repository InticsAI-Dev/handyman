package in.handyman.raven.lib.model.filemergerpdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
    public class FileMergerFileInfo {


        private String file_id;
        private Long process_id;
        private Long root_pipeline_id;
        private String file_checksum;
        private String file_extension;
        private String file_name;
        private String file_path;
        private String file_size;
    }

