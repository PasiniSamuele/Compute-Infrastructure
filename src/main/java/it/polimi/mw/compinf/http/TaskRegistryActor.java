package it.polimi.mw.compinf.http;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.mw.compinf.message.TaskMessage;

import java.util.ArrayList;
import java.util.List;

public class TaskRegistryActor extends AbstractActor {

    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public TaskRegistryActor(ActorRef actorRouter) {
        this.actorRouter = actorRouter;
    }

    //#task-case-classes
    public static class Task {
        private final int id;

        @JsonCreator
        public Task(@JsonProperty("id") int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public static class Tasks {
        private final List<Task> tasks;

        public Tasks() {
            this.tasks = new ArrayList<>();
        }

        public Tasks(List<Task> tasks) {
            this.tasks = tasks;
        }

        public List<Task> getTasks() {
            return tasks;
        }
    }
    //#task-case-classes

    public static Props props(ActorRef actorRouter) {
        return Props.create(TaskRegistryActor.class, actorRouter);
    }

    private final List<Task> tasks = new ArrayList<>();
    private final ActorRef actorRouter;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TaskRegistryMessages.GetTasks.class, getTasks -> getSender().tell(new Tasks(tasks), getSelf()))
                .match(TaskRegistryMessages.CreateTask.class, createTask -> {
                    tasks.add(createTask.getTask());
                    actorRouter.tell(new TaskMessage(createTask.getTask().getId()), ActorRef.noSender());
                    getSender().tell(new TaskRegistryMessages.ActionPerformed(
                            String.format("Task %s created.", createTask.getTask().getId())), getSelf());
                })
                .match(TaskRegistryMessages.GetTask.class, getTask -> {
                    getSender().tell(tasks.stream()
                            .filter(task -> task.getId() == getTask.getId())
                            .findFirst(), getSelf());
                })
                .match(TaskRegistryMessages.DeleteTask.class, deleteTask -> {
                    tasks.removeIf(task -> task.getId() == deleteTask.getId());
                    getSender().tell(new TaskRegistryMessages.ActionPerformed(String.format("User %s deleted.", deleteTask.getId())),
                            getSelf());

                }).matchAny(o -> log.info("received unknown message"))
                .build();
    }
}