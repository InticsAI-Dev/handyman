package in.handyman.raven.exception;

public class KafkaProducerNotInitializedException extends RuntimeException {
    public KafkaProducerNotInitializedException(String message) {
        super(message);
    }
}

