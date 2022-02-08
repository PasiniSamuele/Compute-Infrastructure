package it.polimi.mw.compinf.http;

import akka.NotUsed;
import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
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
import it.polimi.mw.compinf.tasks.ConversionTask;
import it.polimi.mw.compinf.tasks.PrimeTask;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static it.polimi.mw.compinf.http.TaskRegistryMessage.*;

public class TaskRegistryActor extends AbstractLoggingActor {
    private final ActorRef actorRouter;
    private final KafkaProducer<String, String> kafkaProducer;

    private final Map<UUID, Optional<Pair<SourceQueueWithComplete<String>, Source<ServerSentEvent, NotUsed>>>> sourceMap;
    private final Materializer mat;

    public TaskRegistryActor(String kafka) {
        sourceMap = new ConcurrentHashMap<>();
        mat = Materializer.createMaterializer(getContext());
        actorRouter = getContext().actorOf(FromConfig.getInstance().props(), "workerNodeRouter");

        ActorRef pubSubMediator = DistributedPubSub.get(getContext().system()).mediator();
        pubSubMediator.tell(new DistributedPubSubMediator.Subscribe("TaskExecuted", getSelf()), getSelf());

        final Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        kafkaProducer = new KafkaProducer<>(props);
    }

    public static Props props(String kafka) {
        return Props.create(TaskRegistryActor.class, kafka);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateCompressionMessage.class, this::onCreateCompressionMessage)
                .match(CreateConversionMessage.class, this::onCreateConversionMessage)
                .match(CreatePrimeMessage.class, this::onCreatePrimeMessage)
                .match(CreateSSEMessage.class, this::onCreateSSE)
                .match(TaskExecutedMessage.class, this::onTaskExecuted)
                .match(DistributedPubSubMediator.SubscribeAck.class, msg -> log().info("Subscribed to 'TaskExecuted' topic"))
                .matchAny(o -> log().info("Received unknown message"))
                .build();
    }

    private void onCreateCompressionMessage(CreateCompressionMessage ccm) {
        CompressionTask compressionTask = ccm.getCompressionTask();

        actorRouter.tell(compressionTask, getSelf());
        sourceMap.put(compressionTask.getUUID(), Optional.empty());

        kafkaProducer.send(new ProducerRecord<>("pending", null, compressionTask.getUUID().toString()));

        getSender().tell(new TaskCreationMessage("Compression task submitted successfully!", compressionTask.getUUID()), getSelf());
    }

    private void onCreateConversionMessage(CreateConversionMessage ccm) {
        ConversionTask conversionTask = ccm.getConversionTask();

        actorRouter.tell(conversionTask, getSelf());
        sourceMap.put(conversionTask.getUUID(), Optional.empty());

        kafkaProducer.send(new ProducerRecord<>("pending", null, conversionTask.getUUID().toString()));

        getSender().tell(new TaskCreationMessage("Conversion task submitted successfully!", conversionTask.getUUID()), getSelf());
    }

    private void onCreatePrimeMessage(CreatePrimeMessage cpm) {
        PrimeTask primeTask = cpm.getPrimeTask();

        actorRouter.tell(primeTask, getSelf());
        sourceMap.put(primeTask.getUUID(), Optional.empty());

        kafkaProducer.send(new ProducerRecord<>("pending", null, primeTask.getUUID().toString()));

        getSender().tell(new TaskCreationMessage("Prime task submitted successfully!", primeTask.getUUID()), getSelf());
    }

    private void onCreateSSE(CreateSSEMessage csse) {
        UUID uuid = csse.getUUID();

        // Check invalid UUID
        if (!sourceMap.containsKey(uuid)) {
            getSender().tell(new Status.Failure(new InvalidUUIDException(uuid.toString())), getSelf());
            return;
        }

        // Check already created SSE
        if (sourceMap.get(uuid).isEmpty()) {
            Pair<SourceQueueWithComplete<String>, Source<ServerSentEvent, NotUsed>> sourcePair = Source.<String>queue(100, OverflowStrategy.dropHead())
                    .map(ServerSentEvent::create)
                    .keepAlive(Duration.ofSeconds(1), ServerSentEvent::heartbeat)
                    .preMaterialize(mat);

            // Actual materialization
            sourcePair.second()
                    .to(Sink.ignore())
                    .run(mat);

            sourceMap.put(uuid, Optional.of(sourcePair));
        }

        Pair<SourceQueueWithComplete<String>, Source<ServerSentEvent, NotUsed>> currPair = sourceMap.get(uuid).get();

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
            log().error("Invalid task executed with UUID: {}", te.getUUID());
            return;
        }

        if (sourceMap.get(uuid).isPresent()) {
            Pair<SourceQueueWithComplete<String>, Source<ServerSentEvent, NotUsed>> currPair = sourceMap.get(uuid).get();
            currPair.first().offer("Finished task with UUID: " + te.getUUID());
            // Closing SSE
            currPair.first().complete();
        } else {
            // Nobody connected to SSE for task updates
            log().info("Nobody connected to SSE for task with UUID: {}", te.getUUID());
        }

        sourceMap.remove(uuid);
    }

}