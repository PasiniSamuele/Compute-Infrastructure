package it.polimi.mw.compinf.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import it.polimi.mw.compinf.http.TaskRegistryMessage;
import it.polimi.mw.compinf.tasks.TaskResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StoreKeeperActor extends AbstractLoggingActor {
    private final ActorSelection registryActor = getContext().actorSelection("akka://cluster@127.0.0.1:7777/user/taskRegistryActor");

    public static Props props() {
        return Props.create(StoreKeeperActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TaskResult.class, this::onTaskResult)
                .build();
    }

    private void onTaskResult(TaskResult result) {
        // Creates directory if it does not exist and file
        try {
            Files.createDirectories(Path.of(result.getDirectoryName()));
            Files.write(Path.of(result.getDirectoryName() + File.separator + result.getUuid()), result.getFile());

            // TODO Publish completed

            registryActor.tell(new TaskRegistryMessage.TaskExecutedMessage(result.getUuid()), self());

            log().info("Finished {}", result.getUuid());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
