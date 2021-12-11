package it.polimi.mw.compinf.http;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import it.polimi.mw.compinf.http.TaskRegistry.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.*;

public class TaskRoutes {
    private final static Logger log = LoggerFactory.getLogger(TaskRoutes.class);
    private final ActorRef<TaskRegistry.Command> taskRegistryActor;
    private final Duration askTimeout;
    private final Scheduler scheduler;

    public TaskRoutes(ActorSystem<?> system, ActorRef<TaskRegistry.Command> taskRegistryActor) {
        this.taskRegistryActor = taskRegistryActor;
        scheduler = system.scheduler();
        askTimeout = system.settings().config().getDuration("comp-inf-app.routes.ask-timeout");
    }

    private CompletionStage<TaskRegistry.GetTaskResponse> getTask(String name) {
        return AskPattern.ask(taskRegistryActor, ref -> new TaskRegistry.GetTask(name, ref), askTimeout, scheduler);
    }

    private CompletionStage<TaskRegistry.ActionPerformed> deleteTask(String name) {
        return AskPattern.ask(taskRegistryActor, ref -> new TaskRegistry.DeleteTask(name, ref), askTimeout, scheduler);
    }

    private CompletionStage<TaskRegistry.Tasks> getTasks() {
        return AskPattern.ask(taskRegistryActor, TaskRegistry.GetTasks::new, askTimeout, scheduler);
    }

    private CompletionStage<TaskRegistry.ActionPerformed> createTask(Task task) {
        return AskPattern.ask(taskRegistryActor, ref -> new TaskRegistry.CreateTask(task, ref), askTimeout, scheduler);
    }

    /**
    * This method creates one route (of possibly many more that will be part of your Web App)
    */
    //#all-routes
    public Route taskRoutes() {
        return pathPrefix("tasks", () -> concat(
            //#tasks-get
            pathEnd(() ->
                concat(
                    get(() ->
                        onSuccess(getTasks(),
                            tasks -> complete(StatusCodes.OK, tasks, Jackson.marshaller())
                        )
                    )
                )
            )
        ));
    }
}
