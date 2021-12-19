package it.polimi.mw.compinf.http;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.sse.ServerSentEvent;
import akka.japi.function.Creator;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.List;

import static it.polimi.mw.compinf.http.TaskRegistryMessage.*;

public class TaskRegistryActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private Source<ServerSentEvent, NotUsed> source;
    /*private Source<ServerSentEvent, SourceQueueWithComplete<ServerSentEvent>> sourceDecl;
    private Pair<SourceQueueWithComplete<ServerSentEvent>, Source<ServerSentEvent, NotUsed>> sourcePair;*/
    private final Materializer mat;

    public TaskRegistryActor(ActorRef actorRouter) {
        this.actorRouter = actorRouter;
        this.source = null;
        this.mat = Materializer.createMaterializer(context());
    }

    public static Props props(ActorRef actorRouter) {
        return Props.create(TaskRegistryActor.class, actorRouter);
    }

    private final ActorRef actorRouter;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateCompressionMessage.class, this::onCreateCompressionMessage)
                //.match(CreateConversionTask.class, this::onCreateConversionTask)
                //.match(CreateDownloadTask.class, this::onCreateDownloadTask)
                .match(CreateSSE.class, this::onCreateSSE)
                .match(TaskExecuted.class, this::onTaskExecuted)
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private void onCreateCompressionMessage(CreateCompressionMessage ccm) {
        actorRouter.tell(ccm.getCompressionTask(), getSelf());
        getSender().tell(new ActionPerformed(
                String.format("Task %s submitted successfully.", ccm.getCompressionTask().getUUID())), getSelf());
    }

    private void onCreateSSE(CreateSSE csse) {
        // FIXME check right way to avoid double initialization
        /*if (sourceDecl == null || sourcePair == null) {
            sourceDecl = Source.queue(100, OverflowStrategy.dropHead());
            sourcePair = sourceDecl.preMaterialize(mat);

            // Adding element before actual materialization
            sourcePair.first().offer(ServerSentEvent.create("pre materialization element"));
        }
        Source<ServerSentEvent, NotUsed> cocco = sourcePair.second();
        cocco.run(mat);*/

        if (source == null) {
            source = Source.from(List.of(ServerSentEvent.create("mamma")))
                    .keepAlive(Duration.ofSeconds(1), ServerSentEvent::heartbeat);
        }

        source = source.keepAlive(Duration.ofSeconds(1), ServerSentEvent::heartbeat)
                    .map(a -> "cocco 3001")

                .map(a -> ServerSentEvent.create(String.valueOf(a)))

                .mapMaterializedValue(c -> NotUsed.notUsed());


        getSender().tell(new GetSSE(source), getSelf());
    }

    private void onTaskExecuted(TaskExecuted te) {
        if (source != null) {
            source = source.map(a -> "cocco 3001")
                    .map(a -> ServerSentEvent.create(String.valueOf(a)))
                    .mapMaterializedValue(c -> NotUsed.notUsed());

            log.info(String.format("Messaggio inoltrato cocco 3000: %s", te.getUUID()), getSelf());
        }
    }

}