package it.polimi.mw.compinf.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
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
    private final ActorRef storeKeeperRouter;

    private final KafkaProducer<String, String> kafkaProducer;

    private final static String compressionOutput = "Compression task executed!%nTask UUID: %s%nCompression Ratio: %f";
    private final static String conversionOutput = "Conversion task executed!%nTask UUID: %s%nTarget Format: %s";
    private final static String primeOutput = "Prime task executed!%nTask UUID: %s%nUpper Bound: %d";

    public WorkerActor(String kafka, ActorRef storeKeeperRouter) {
        this.storeKeeperRouter = storeKeeperRouter;

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        kafkaProducer = new KafkaProducer<>(props);
    }

    public static Props props(String kafka, ActorRef storeKeeperRouter) {
        return Props.create(WorkerActor.class, kafka, storeKeeperRouter);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CompressionTask.class, this::onCompressionTask)
                .match(ConversionTask.class, this::onConversionTask)
                .match(PrimeTask.class, this::onPrimeTask)
                .matchAny(o -> log().info("Received unknown message"))
                .build();
    }

    private void onCompressionTask(CompressionTask task) throws Exception {
        UUID uuid = task.getUUID();
        log().info("Received Compression {}", uuid);

        kafkaProducer.send(new ProducerRecord<>("starting", null, task.getUUID().toString()));

        checkTaskFailure(task);

        // Dummy compression
        taskExecution(5, 7);

        String taskString = String.format(compressionOutput, uuid, task.getCompressionRatio());
        onFinishedTask(uuid, taskString.getBytes(), task.getDirectoryName());
    }

    private void onConversionTask(ConversionTask task) throws Exception {
        UUID uuid = task.getUUID();
        log().info("Received Conversion {}", uuid);

        kafkaProducer.send(new ProducerRecord<>("starting", null, task.getUUID().toString()));

        checkTaskFailure(task);

        // Dummy conversion
        taskExecution(10, 15);

        String taskString = String.format(conversionOutput, uuid, task.getTargetFormat());
        onFinishedTask(uuid, taskString.getBytes(), task.getDirectoryName());
    }

    private void onPrimeTask(PrimeTask task) throws Exception {
        UUID uuid = task.getUUID();
        log().info("Received Prime {}", uuid);

        kafkaProducer.send(new ProducerRecord<>("starting", null, task.getUUID().toString()));

        checkTaskFailure(task);

        // Dummy prime
        taskExecution(7, 12);

        String taskString = String.format(primeOutput, uuid, task.getUpperBound());
        onFinishedTask(uuid, taskString.getBytes(), task.getDirectoryName());
    }

    private void onFinishedTask(UUID uuid, byte[] file, String directoryName) {
        TaskResult taskResult = new TaskResult(uuid, file, directoryName);
        storeKeeperRouter.tell(taskResult, getSelf());
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

    private void taskExecution(int minExecTime, int maxExecTime) throws InterruptedException {
        int randInt = (int) (Math.random() * (maxExecTime - minExecTime + 1) + minExecTime);

        // Simulating execution time
        Thread.sleep(randInt * 1000L);
    }

    @Override
    public void preRestart(Throwable reason, Optional<Object> message) {
        if (message.isPresent()) {
            try {
                Task taskMessage = (Task) message.get();
                log().warning("Restarting task {} due to failure", taskMessage.getUUID());

                // Resending the message with a higher priority in order to process it first and decrease the number of remaining failures
                getContext().getSelf().tell(taskMessage.increasePriority().decreaseFailure(), getContext().getSender());
            } catch (ClassCastException e) {
                log().error("Invalid task message");
            }
        }
    }
}
