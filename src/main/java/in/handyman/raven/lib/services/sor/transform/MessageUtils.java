package in.handyman.raven.lib.services.sor.transform;

public class MessageUtils {

    public static String appendMsg(String oldMessage, String message) {
        if (oldMessage == null || oldMessage.isBlank())
            return message;
        return oldMessage + " | " + message;
    }


}
