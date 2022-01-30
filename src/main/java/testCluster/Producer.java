package testCluster;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.FromConfig;
import it.polimi.mw.compinf.tasks.Task;

public class Producer extends AbstractActor {
    ActorRef router = getContext().actorOf(FromConfig.getInstance().props(), "workerNodeRouter");

    static Props props() {
        return Props.create(Producer.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Task.class, this::processMessage)
                .matchAny(o -> System.out.println("received unknown message"))
                .build();
    }

    public void processMessage(Task msg) {
        router.tell(msg, self());
    }
}
