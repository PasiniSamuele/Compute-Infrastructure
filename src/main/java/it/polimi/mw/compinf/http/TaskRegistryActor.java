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
import akka.routing.FromConfig;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.SourceQueueWithComplete;
import it.polimi.mw.compinf.exceptions.InvalidUUIDException;
import it.polimi.mw.compinf.tasks.CompressionTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import static it.polimi.mw.compinf.http.TaskRegistryMessage.*;

public class TaskRegistryActor extends AbstractActor {

    // TODO Concurrent hash map
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<UUID, Pair<SourceQueueWithComplete<String>, Source<ServerSentEvent, NotUsed>>> sourceMap;
    private final Materializer mat;

    public TaskRegistryActor() {
        this.sourceMap = new HashMap<>();
        this.mat = Materializer.createMaterializer(getContext());
    }

    public static Props props() {
        return Props.create(TaskRegistryActor.class);
    }

    private final ActorRef actorRouter = getContext().actorOf(FromConfig.getInstance().props(), "workerNodeRouter");

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateCompressionMessage.class, this::onCreateCompressionMessage)
                //.match(CreateConversionTask.class, this::onCreateConversionTask)
                //.match(CreateDownloadTask.class, this::onCreateDownloadTask)
                .match(CreateSSEMessage.class, this::onCreateSSE)
                .match(TaskExecutedMessage.class, this::onTaskExecuted)
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private void onCreateCompressionMessage(CreateCompressionMessage ccm) {
        CompressionTask compressionTask = ccm.getCompressionTask();

        actorRouter.tell(compressionTask, getSelf());
        sourceMap.put(compressionTask.getUUID(), null);
        System.out.println(compressionTask.getDirectoryName());

        getSender().tell(new GenericMessage(
                String.format("Task %s submitted successfully.", compressionTask.getUUID())), getSelf());
    }

    private void onCreateSSE(CreateSSEMessage csse) {
        UUID uuid = csse.getUUID();

        // Check invalid UUID
        if (!sourceMap.containsKey(uuid)) {
            getSender().tell(new Status.Failure(new InvalidUUIDException(uuid.toString())), getSelf());
            return;
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

        getSender().tell(new GetSSEMessage(currPair.second()), getSelf());
        currPair.first().offer("2");
    }

    /**
     * Request is coming from backend (worker actor).
     *
     * @param te Task executed message.
     */
    private void onTaskExecuted(TaskExecutedMessage te) {
        UUID uuid = te.getUUID();

        // Invalid UUID provided
        if (!sourceMap.containsKey(uuid)) {
            log.error("Invalid task executed with UUID: {}", te.getUUID());
            return;
        }

        Pair<SourceQueueWithComplete<String>, Source<ServerSentEvent, NotUsed>> currPair = sourceMap.get(uuid);

        // Nobody connected to SSE for task updates
        if (currPair == null) {
            log.info("Nobody connected to SSE for task with UUID: {}", te.getUUID());
        } else {
            currPair.first().offer("Finished task with UUID: " + te.getUUID());
            // Closing SSE
            currPair.first().complete();
        }

        sourceMap.remove(uuid);
    }

}