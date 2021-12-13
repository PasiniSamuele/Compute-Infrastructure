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
import java.util.Collections;
import java.util.List;

import static it.polimi.mw.compinf.http.TaskRegistryMessages.*;

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
                .match(GetTasks.class, this::onGetTasks)
                .match(CreateTask.class, this::onCreateTask)
                .match(GetTask.class, this::onGetTask)
                .match(DeleteTask.class, this::onDeleteTask)
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private void onGetTasks(GetTasks getTasks) {
        // We must be careful not to send out users since it is mutable
        // so for this response we need to make a defensive copy
        getSender().tell(new Tasks(List.copyOf(tasks)), getSelf());
    }

    private void onCreateTask(CreateTask createTask) {
        tasks.add(createTask.getTask());
        actorRouter.tell(new TaskMessage(createTask.getTask().getId()), ActorRef.noSender());
        getSender().tell(new ActionPerformed(
                String.format("Task %s created.", createTask.getTask().getId())), getSelf());
    }

    private void onGetTask(GetTask getTask) {
        getSender().tell(tasks.stream()
                .filter(task -> task.getId() == getTask.getId())
                .findFirst(), getSelf());
    }

    private void onDeleteTask(DeleteTask deleteTask) {
        tasks.removeIf(task -> task.getId() == deleteTask.getId());
        getSender().tell(new ActionPerformed(String.format("Task %s deleted.", deleteTask.getId())),
                getSelf());

    }
}