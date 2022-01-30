package testCluster;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import it.polimi.mw.compinf.tasks.Task;

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

    public void processMessage(Task msg) throws InterruptedException {
        log().info("Received {}", msg.getDirectoryName());
        Thread.sleep(2000);
        log().info("Slept {}", msg.getDirectoryName());
    }
}
