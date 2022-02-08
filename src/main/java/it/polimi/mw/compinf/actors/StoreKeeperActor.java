package it.polimi.mw.compinf.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import it.polimi.mw.compinf.http.TaskRegistryMessage;
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
    //private final ActorSelection registryActor;
    private final KafkaProducer<String, String> kafkaProducer;

    public StoreKeeperActor(String kafka) {
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        // subscribe to the topic named "StoreKeepers"
        mediator.tell(new DistributedPubSubMediator.Subscribe("StoreKeepers", getSelf()), getSelf());

        //this.registryActor = getContext().actorSelection("/user/taskRegistryActor");

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.kafkaProducer = new KafkaProducer<>(props);
    }

    public static Props props(String kafka) {
        return Props.create(StoreKeeperActor.class, kafka);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TaskResult.class, this::onTaskResult)
                .match(DistributedPubSubMediator.SubscribeAck.class, msg -> log().info("subscribed"))
                .build();
    }

    private void onTaskResult(TaskResult result) {
        // Creates directory if it does not exist and file
        try {
            Files.createDirectories(Path.of(result.getDirectoryName()));
            Files.write(Path.of(result.getDirectoryName() + File.separator + result.getUUID()), result.getFile());

            kafkaProducer.send(new ProducerRecord<>("completed", null, result.getUUID().toString()));

            //registryActor.tell(new TaskRegistryMessage.TaskExecutedMessage(result.getUUID()), self());

            log().info("Finished {}", result.getUUID());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
