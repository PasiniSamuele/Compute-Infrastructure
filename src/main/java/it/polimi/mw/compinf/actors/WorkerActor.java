package it.polimi.mw.compinf.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import it.polimi.mw.compinf.tasks.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class WorkerActor extends AbstractLoggingActor {
    private final ActorSelection storeKeeper;
    private final KafkaProducer<String, String> kafkaProducer;

    private final static String compressionOutput = "Compression task executed!%nTask UUID: %s%nCompression Ratio: %s";
    private final static String conversionOutput = "Conversion task executed!%nTask UUID: %s%nTarget Format: %s";
    private final static String primeOutput = "Conversion task executed!%nTask UUID: %s%nTarget Format: %s";

    public WorkerActor() {
        this.storeKeeper = getContext().actorSelection("akka://cluster@127.0.0.1:25565/user/storeKeeper");

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.kafkaProducer = new KafkaProducer<>(props);
    }

    public static Props props() {
        return Props.create(WorkerActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CompressionTask.class, this::onCompressionTask)
                .match(ConversionTask.class, this::onConversionTask)
                .match(PrimeTask.class, this::onPrimeTask)
                .build();
    }

    private void onCompressionTask(CompressionTask task) throws Exception {
        UUID uuid = task.getUUID();
        log().info("Received Compression {}", uuid);

        kafkaProducer.send(new ProducerRecord<>("starting", null, task.getUUID().toString()));

        checkTaskFailure(task);

        // Dummy compression
        Thread.sleep(10000);

        String taskString = String.format(compressionOutput, uuid, task.getCompressionRatio());
        onFinishedTask(uuid, taskString.getBytes(), task.getDirectoryName());
    }

    private void onConversionTask(ConversionTask task) throws Exception {
        UUID uuid = task.getUUID();
        log().info("Received Conversion {}", uuid);

        kafkaProducer.send(new ProducerRecord<>("starting", null, task.getUUID().toString()));

        checkTaskFailure(task);

        // Dummy conversion
        Thread.sleep(10000);

        String taskString = String.format(conversionOutput, uuid, task.getTargetFormat());
        onFinishedTask(uuid, taskString.getBytes(), task.getDirectoryName());
    }

    private void onPrimeTask(PrimeTask task) throws Exception {
        UUID uuid = task.getUUID();
        log().info("Received Prime {}", uuid);

        kafkaProducer.send(new ProducerRecord<>("starting", null, task.getUUID().toString()));

        checkTaskFailure(task);

        // Dummy prime
        Thread.sleep(10000);

        String taskString = String.format(primeOutput, uuid, task.getUpperBound());
        onFinishedTask(uuid, taskString.getBytes(), task.getDirectoryName());
    }

    private void onFinishedTask(UUID uuid, byte[] file, String directoryName) {
        TaskResult taskResult = new TaskResult(uuid, file, directoryName);
        storeKeeper.tell(taskResult, self());
    }

    private void checkTaskFailure(Task task) throws Exception {
        if (task.getForceFailure() > 0) {
            throw new Exception();
        } else {
            Random rand = new Random();
            if (rand.nextInt(5) == 0) {
                throw new Exception();
            }
        }
    }

    @Override
    public void preRestart(Throwable reason, Optional<Object> message) {
        if (message.isPresent()) {
            try {
                Task taskMessage = (Task) message.get();
                log().warning("Restarting task {} due to failure", taskMessage.getDirectoryName());

                // Resending the message with a higher priority in order to process it first and decrease the number of remaining failures
                getContext().getSelf().tell(taskMessage.increasePriority().decreaseFailure(), getContext().getSender());
            } catch (ClassCastException e) {
                log().error("Invalid task message");
            }
        }
    }
}
