package testCluster;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

public class Worker extends AbstractLoggingActor {
    static Props props() {
        return Props.create(Worker.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Integer.class, this::processMessage)
                .build();
    }

    public void processMessage(Integer num) {
        log().info("Received {}", num);

        try {
            Thread.sleep(num * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log().info("Slept {}", num);
    }
}
