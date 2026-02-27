package in.handyman.raven.actor;

import java.util.concurrent.BlockingQueue;

public class ActorRef {

    private final BlockingQueue<Message> mailbox;

    public ActorRef(BlockingQueue<Message> mailbox) {
        this.mailbox = mailbox;
    }

    public void tell(Message message) {
        mailbox.offer(message);
    }
}
