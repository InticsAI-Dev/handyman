package in.handyman.raven.actor;


import lombok.Getter;

@Getter
public class ProcessFile implements Message {

    private final String fileId;

    public ProcessFile(String fileId) {
        this.fileId = fileId;
    }

}
