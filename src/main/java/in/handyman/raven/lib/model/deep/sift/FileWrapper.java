package in.handyman.raven.lib.model.deep.sift;

public class FileWrapper {
    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public FileWrapper(String name, String originalFilename, String contentType, byte[] content) {
        if (name == null || originalFilename == null || contentType == null || content == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getBytes() {
        return content.clone(); // Return a copy to prevent external modification
    }

    public long getSize() {
        return content.length;
    }

    public boolean isEmpty() {
        return content == null || content.length == 0;
    }
}
