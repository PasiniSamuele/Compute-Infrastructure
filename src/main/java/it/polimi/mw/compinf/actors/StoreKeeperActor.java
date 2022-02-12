package it.polimi.mw.compinf.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import it.polimi.mw.compinf.http.InternalHttpMessage;
import it.polimi.mw.compinf.tasks.TaskResult;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class StoreKeeperActor extends AbstractLoggingActor {
    private final ActorRef httpRouter;
    private final KafkaProducer<String, String> kafkaProducer;

    private final String BASE_DIRECTORY = "results/";

    public StoreKeeperActor(String kafka, ActorRef httpRouter) {
        this.httpRouter = httpRouter;

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        kafkaProducer = new KafkaProducer<>(props);
    }

    public static Props props(String kafka, ActorRef httpRouter) {
        return Props.create(StoreKeeperActor.class, kafka, httpRouter);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TaskResult.class, this::onTaskResult)
                .matchAny(o -> log().info("Received unknown message"))
                .build();
    }

    private void onTaskResult(TaskResult result) {
        // Creates directory if it does not exist and file
        try {
            Files.createDirectories(Path.of(BASE_DIRECTORY + result.getDirectoryName()));
            Files.write(Path.of(BASE_DIRECTORY + result.getDirectoryName() + File.separator + result.getUUID()), result.getFile());

            kafkaProducer.send(new ProducerRecord<>("completed", null, result.getUUID().toString()));
            InternalHttpMessage.TaskExecutedMessage taskExecuted = new InternalHttpMessage.TaskExecutedMessage(result.getUUID());

            httpRouter.tell(taskExecuted, getSelf());

            log().info("Finished {}", result.getUUID());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
