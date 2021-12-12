package it.polimi.mw.compinf.http;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.mw.compinf.message.TaskMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

//#task-registry-actor
public class TaskRegistry extends AbstractBehavior<TaskRegistry.Command>  {

    // actor protocol
    public interface Command {}

    public final static class GetTasks implements Command {
        public final ActorRef<Tasks> replyTo;
        public GetTasks(ActorRef<Tasks> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public final static class CreateTask implements Command {
        public final Task task;
        public final ActorRef<ActionPerformed> replyTo;
        public CreateTask(Task task, ActorRef<ActionPerformed> replyTo) {
            this.task = task;
            this.replyTo = replyTo;
        }
    }

    public final static class GetTaskResponse {
        public final Optional<Task> maybeTask;
        public GetTaskResponse(Optional<Task> maybeTask) {
            this.maybeTask = maybeTask;
        }
    }

    public final static class GetTask implements Command {
        public final int id;
        public final ActorRef<GetTaskResponse> replyTo;
        public GetTask(int id, ActorRef<GetTaskResponse> replyTo) {
            this.id = id;
            this.replyTo = replyTo;
        }
    }


    public final static class DeleteTask implements Command {
        public final int id;
        public final ActorRef<ActionPerformed> replyTo;
        public DeleteTask(int id, ActorRef<ActionPerformed> replyTo) {
            this.id = id;
            this.replyTo = replyTo;
        }
    }


    public final static class ActionPerformed implements Command {
        public final String description;
        public ActionPerformed(String description) {
            this.description = description;
        }
    }

    //#task-case-classes
    public final static class Task {
        public final int id;

        @JsonCreator
        public Task(@JsonProperty("id") int id) {
            this.id = id;
        }
    }

    public final static class Tasks {
        public final List<Task> tasks;
        public Tasks(List<Task> tasks) {
            this.tasks = tasks;
        }
    }
    //#task-case-classes

    private final akka.actor.ActorRef actorRouter;
    private final List<Task> tasks = new ArrayList<>();

    private TaskRegistry(ActorContext<Command> context, akka.actor.ActorRef actorRouter) {
        super(context);
        this.actorRouter = actorRouter;
    }

    public static Behavior<Command> create(akka.actor.ActorRef actorRouter) {
        return Behaviors.setup(context -> new TaskRegistry(context, actorRouter));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetTasks.class, this::onGetTasks)
                .onMessage(CreateTask.class, this::onCreateTask)
                .onMessage(GetTask.class, this::onGetTask)
                .onMessage(DeleteTask.class, this::onDeleteTask)
                .build();
    }

    private Behavior<Command> onGetTasks(GetTasks command) {
        // We must be careful not to send out users since it is mutable
        // so for this response we need to make a defensive copy
        command.replyTo.tell(new Tasks(Collections.unmodifiableList(new ArrayList<>(tasks))));
        return this;
    }

    private Behavior<Command> onCreateTask(CreateTask command) {
        tasks.add(command.task);
        command.replyTo.tell(new ActionPerformed(String.format("Task %s created.", command.task.id)));
        actorRouter.tell(new TaskMessage(command.task.id), akka.actor.ActorRef.noSender());
        return this;
    }

    private Behavior<Command> onGetTask(GetTask command) {
        Optional<Task> maybeUser = tasks.stream()
                .filter(task -> task.id == command.id)
                .findFirst();
        command.replyTo.tell(new GetTaskResponse(maybeUser));
        return this;
    }

    private Behavior<Command> onDeleteTask(DeleteTask command) {
        tasks.removeIf(task -> task.id == command.id);
        command.replyTo.tell(new ActionPerformed(String.format("Task %s deleted.", command.id)));
        return this;
    }

}
//#task-registry-actor