package testCluster;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.FromConfig;

public class Producer extends AbstractActor {
    ActorRef router = getContext().actorOf(FromConfig.getInstance().props(), "workerRouter");

    static Props props() {
        return Props.create(Producer.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Integer.class, this::processMessage)
                .build();
    }

    public void processMessage(Integer num) {
        router.tell(num, self());
    }
}
