package in.handyman.raven.actor;


public class FileProcessorActor implements ActorCore {

    @Override
    public void preStart() {
        System.out.println("Actor Started");
    }

    @Override
    public void onReceive(Message message) {

        if (message instanceof ProcessFile) {

            ProcessFile msg = (ProcessFile) message;

            System.out.println(
                    "Processing file: " + msg.getFileId()
            );

            // pipeline logic here
        }
    }

    @Override
    public void postStop() {
        System.out.println("Actor Stopped");
    }
}
