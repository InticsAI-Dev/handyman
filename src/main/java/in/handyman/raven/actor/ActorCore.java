package in.handyman.raven.actor;

public interface ActorCore {

    /**
     * Called once when actor starts
     */
    default void preStart() {}

    /**
     * Message handler
     */
    void onReceive(Message message);

    /**
     * Called before shutdown
     */
    default void postStop() {}
}
