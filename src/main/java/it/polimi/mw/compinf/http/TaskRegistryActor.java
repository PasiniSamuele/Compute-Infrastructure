package it.polimi.mw.compinf.http;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Status;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.sse.ServerSentEvent;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.SourceQueueWithComplete;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import static it.polimi.mw.compinf.http.TaskRegistryMessage.*;

public class TaskRegistryActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<UUID, Pair<SourceQueueWithComplete<String>, Source<ServerSentEvent, NotUsed>>> sourceMap;
    private final Materializer mat;

    public TaskRegistryActor(ActorRef actorRouter) {
        this.actorRouter = actorRouter;
        this.sourceMap = new HashMap<>();
        this.mat = Materializer.createMaterializer(getContext());
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
        sourceMap.put(ccm.getCompressionTask().getUUID(), null);
        getSender().tell(new ActionPerformed(
                String.format("Task %s submitted successfully.", ccm.getCompressionTask().getUUID())), getSelf());
    }

    private void onCreateSSE(CreateSSE csse) {
        UUID uuid = csse.getUUID();

        // Check invalid UUID
        if (!sourceMap.containsKey(uuid)) {
            getSender().tell(new Status.Failure(new RuntimeException(uuid.toString())), getSelf());
        }

        // Check already created SSE
        if (sourceMap.get(uuid) == null) {
            Pair<SourceQueueWithComplete<String>, Source<ServerSentEvent, NotUsed>> sourcePair = Source.<String>queue(100, OverflowStrategy.dropHead())
                    .map(ServerSentEvent::create)
                    .keepAlive(Duration.ofSeconds(1), ServerSentEvent::heartbeat)
                    .preMaterialize(mat);

            // Actual materialization
            sourcePair.second()
                    .to(Sink.ignore())
                    .run(mat);

            sourceMap.put(uuid, sourcePair);
        }

        Pair<SourceQueueWithComplete<String>, Source<ServerSentEvent, NotUsed>> currPair = sourceMap.get(uuid);

        getSender().tell(new GetSSE(currPair.second()), getSelf());
        currPair.first().offer("2");
    }

    /**
     * Request is coming from backend (worker actor).
     * @param te Task executed message.
     */
    private void onTaskExecuted(TaskExecuted te) {
        UUID uuid = te.getUUID();

        if (!sourceMap.containsKey(uuid)) {
            log.error("Invalid task executed with UUID: {}", te.getUUID());
            return;
        }

        Pair<SourceQueueWithComplete<String>, Source<ServerSentEvent, NotUsed>> currPair = sourceMap.get(uuid);

        currPair.first().offer("Finished task with UUID: " + te.getUUID());

        // Closing SSE
        currPair.first().complete();
    }

}