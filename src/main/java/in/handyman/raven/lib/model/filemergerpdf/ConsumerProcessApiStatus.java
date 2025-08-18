package in.handyman.raven.lib.model.filemergerpdf;

import lombok.AllArgsConstructor;
import lombok.Getter;


    @Getter
    @AllArgsConstructor


    public enum ConsumerProcessApiStatus {

        COMPLETED("COMPLETED"),FAILED("FAILED");
        private final String statusDescription;

    }

