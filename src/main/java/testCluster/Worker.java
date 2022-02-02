package testCluster;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import it.polimi.mw.compinf.tasks.Task;

import java.util.Optional;

public class Worker extends AbstractLoggingActor {
    static Props props() {
        return Props.create(Worker.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Task.class, this::processMessage)
                .build();
    }

    public void processMessage(Task msg) throws Exception {
        log().info("Received {}", msg.getDirectoryName());

        int randInt = (int) (Math.random() * 4 + 1);

        // Simulating random task failure
        if (randInt > 3) {
            log().error("Exception {}", msg.getDirectoryName());
            throw new Exception();
        }

        Thread.sleep(2000);
        log().info("Slept {}", msg.getDirectoryName());
    }

    @Override
    public void preRestart(Throwable reason, Optional<Object> message) {
        if (message.isPresent()) {
            try {
                Task taskMessage = (Task) message.get();

                log().warning("Restarting task {} due to failure", taskMessage.getDirectoryName());

                // Resending the message with a higher priority in order to process it first
                getContext().getSelf().tell(taskMessage.increasePriority(), getContext().getSender());
            } catch (ClassCastException e) {
                log().error("Invalid task message");
            }
        }
    }
}
