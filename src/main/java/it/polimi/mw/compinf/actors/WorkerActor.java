package it.polimi.mw.compinf.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import it.polimi.mw.compinf.tasks.*;

import java.util.Optional;
import java.util.UUID;

public class WorkerActor extends AbstractLoggingActor {
    private final ActorSelection storeKeeper = getContext().actorSelection("akka://cluster@127.0.0.1:25565/user/storeKeeper");

    private final static String compressionOutput = "Compression task executed!%nTask UUID: %s%nCompression Ratio: %s";
    private final static String conversionOutput = "Conversion task executed!%nTask UUID: %s%nTarget Format: %s";
    private final static String primeOutput = "Conversion task executed!%nTask UUID: %s%nTarget Format: %s";

    public static Props props() {
        return Props.create(WorkerActor.class);
    }

    @Override
    public Receive createReceive() {
        // TODO Task Failure
        return receiveBuilder()
                .match(CompressionTask.class, this::onCompressionTask)
                .match(ConversionTask.class, this::onConversionTask)
                .match(PrimeTask.class, this::onPrimeTask)
                .build();
    }

    private void onCompressionTask(CompressionTask task) throws Exception {
        UUID uuid = task.getUUID();
        log().info("Received Compression {}", uuid);

        // TODO Publish starting

        // Dummy compression
        Thread.sleep(10000);

        String taskString = String.format(compressionOutput, uuid, task.getCompressionRatio());
        onFinishedTask(uuid, taskString.getBytes(), task.getDirectoryName());
    }

    private void onConversionTask(ConversionTask task) throws Exception {
        UUID uuid = task.getUUID();
        log().info("Received Conversion {}", uuid);

        // TODO Publish starting

        // Dummy conversion
        Thread.sleep(10000);

        String taskString = String.format(conversionOutput, uuid, task.getTargetFormat());
        onFinishedTask(uuid, taskString.getBytes(), task.getDirectoryName());
    }

    private void onPrimeTask(PrimeTask task) throws Exception {
        UUID uuid = task.getUUID();
        log().info("Received Prime {}", uuid);

        // TODO Publish starting

        // Dummy prime
        Thread.sleep(10000);

        String taskString = String.format(primeOutput, uuid, task.getUpperBound());
        onFinishedTask(uuid, taskString.getBytes(), task.getDirectoryName());
    }

    private void onFinishedTask(UUID uuid, byte[] file, String directoryName) {
        TaskResult taskResult = new TaskResult(uuid, file, directoryName);
        storeKeeper.tell(taskResult, self());
    }

    @Override
    public void preRestart(Throwable reason, Optional<Object> message) {
        if (message.isPresent()) {
            try {
                Task taskMessage = (Task) message.get();
                log().warning("Restarting task {} due to failure", taskMessage.getDirectoryName());

                // Resending the message with a higher priority in order to process it first
                getContext().getSelf().tell(taskMessage.increasePriority(), getContext().getSender());
            } catch (ClassCastException e) {
                log().error("Invalid task message");
            }
        }
    }
}
