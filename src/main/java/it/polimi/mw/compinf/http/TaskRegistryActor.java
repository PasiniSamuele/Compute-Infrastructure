package it.polimi.mw.compinf.http;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.polimi.mw.compinf.message.TaskMessage;

import static it.polimi.mw.compinf.http.TaskRegistryMessage.*;

public class TaskRegistryActor extends AbstractActor {

    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public TaskRegistryActor(ActorRef actorRouter) {
        this.actorRouter = actorRouter;
    }

    //#task-case-classes
    /*public static class Task {
        private final int id;

        @JsonCreator
        public Task(@JsonProperty("id") int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }*/
    //#task-case-classes

    public static Props props(ActorRef actorRouter) {
        return Props.create(TaskRegistryActor.class, actorRouter);
    }

    private final ActorRef actorRouter;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateCompressionMessage.class, this::onCreateCompressionTask)
                //.match(CreateConversionTask.class, this::onCreateConversionTask)
                //.match(CreateDownloadTask.class, this::onCreateDownloadTask)
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private void onCreateCompressionTask(CreateCompressionMessage createCompressionMessage) {
        actorRouter.tell(createCompressionMessage.getCompressionTask(), ActorRef.noSender());
        getSender().tell(new ActionPerformed(
                String.format("Task %s terminated successfully.", createCompressionMessage.getCompressionTask().getUUID())), getSelf());
    }
}