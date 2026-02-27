package in.handyman.raven.actor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ActorCell implements Runnable {

    private final ActorCore actorCore;
    private final BlockingQueue<Message> mailbox;
    private volatile boolean running = true;

    public ActorCell(ActorCore actorCore) {
        this.actorCore = actorCore;
        this.mailbox = new LinkedBlockingQueue<>();
    }

    public ActorRef ref() {
        return new ActorRef(mailbox);
    }

    @Override
    public void run() {

        actorCore.preStart();

        while (running) {
            try {
                Message msg = mailbox.take();
                actorCore.onReceive(msg);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        actorCore.postStop();
    }

    public void stop() {
        running = false;
    }
}