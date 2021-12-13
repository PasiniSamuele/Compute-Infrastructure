package it.polimi.mw.compinf.http;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Scheduler;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import it.polimi.mw.compinf.http.TaskRegistryActor.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

/**
 * Routes can be defined in separated classes like shown in here
 */
//#task-routes-class
public class TaskRoutes extends AllDirectives {
    private final ActorRef taskRegistryActor;
    private final static Logger log = LoggerFactory.getLogger(TaskRoutes.class);
    private final Duration askTimeout;
    // FIXME: do we need scheduler here in Akka HTTP classic?
    //        In Akka HTTP typed it is used by AskPattern.ask().
    //private final Scheduler scheduler;

    public TaskRoutes(ActorSystem system, ActorRef taskRegistryActor) {
        this.taskRegistryActor = taskRegistryActor;
        //scheduler = system.scheduler();
        askTimeout = system.settings().config().getDuration("comp-inf-app.routes.ask-timeout");
    }

    private CompletionStage<TaskRegistryActor.Tasks> getTasks() {
        return Patterns.ask(taskRegistryActor, new TaskRegistryMessages.GetTasks(), askTimeout)
                .thenApply(TaskRegistryActor.Tasks.class::cast);
    }

    /*private CompletionStage<TaskRegistry.GetTaskResponse> getTask(int id) {
        return AskPattern.ask(taskRegistryActor, ref -> new TaskRegistry.GetTask(id, ref), askTimeout, scheduler);
    }

    private CompletionStage<TaskRegistry.ActionPerformed> deleteTask(int id) {
        return AskPattern.ask(taskRegistryActor, ref -> new TaskRegistry.DeleteTask(id, ref), askTimeout, scheduler);
    }

    private CompletionStage<TaskRegistry.Tasks> getTasks() {
        return AskPattern.ask(taskRegistryActor, TaskRegistry.GetTasks::new, askTimeout, scheduler);
    }

    private CompletionStage<TaskRegistry.ActionPerformed> createTask(Task task) {
        return AskPattern.ask(taskRegistryActor, ref -> new TaskRegistry.CreateTask(task, ref), askTimeout, scheduler);
    }*/

    /**
     * This method creates one route (of possibly many more that will be part of your Web App)
     */
    //#all-routes
    //#users-get-delete
    public Route taskRoutes() {
        return pathPrefix("tasks", () ->
                concat(
                        getOrPostUsers()
                        /*path(PathMatchers.segment(), id -> route(
                                        getTask(id),
                                        deleteTask(id)
                                )
                        )*/
                )
        );
    }

    //#tasks-get-create
    private Route getOrPostUsers() {
        return pathEnd(() ->
                concat(
                        //#tasks-get
                        get(() ->
                                onSuccess(getTasks(),
                                        tasks -> complete(StatusCodes.OK, tasks, Jackson.marshaller())
                                )
                        ),
                        /*get(() -> {
                            CompletionStage<TaskRegistryActor.Tasks> futureUsers = Patterns
                                    .ask(taskRegistryActor, new TaskRegistryMessages.GetTasks(), timeout)
                                    .thenApply(TaskRegistryActor.Tasks.class::cast);
                            return onSuccess(() -> futureUsers,
                                    users -> complete(StatusCodes.OK, users, Jackson.marshaller()));
                        }),*/
                        //#task-create
                        post(() ->
                                entity(
                                        Jackson.unmarshaller(Task.class),
                                        task -> {
                                            CompletionStage<TaskRegistryMessages.ActionPerformed> taskCreated = Patterns
                                                    .ask(taskRegistryActor, new TaskRegistryMessages.CreateTask(task), askTimeout)
                                                    .thenApply(TaskRegistryMessages.ActionPerformed.class::cast);
                                            return onSuccess(() -> taskCreated,
                                                    performed -> {
                                                        log.info("Created task [{}]: {}", task.getId(), performed.getDescription());
                                                        return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
                                                    });
                                        }))
                )
        );
    }//#tasks-get-post
}
//#all-routes

//#users-get-delete

//#users-get-delete
    /*private Route getUser(String name) {
        return get(() -> {
            // #retrieve-user-info
            CompletionStage<Optional<Task>> maybeUser = Patterns
                    .ask(userRegistryActor, new UserRegistryMessages.GetUser(name), timeout)
                    .thenApply(Optional.class::cast);

            return onSuccess(() -> maybeUser,
                    performed -> {
                        if (performed.isPresent())
                            return complete(StatusCodes.OK, performed.get(), Jackson.marshaller());
                        else
                            return complete(StatusCodes.NOT_FOUND);
                    }
            );
            //#retrieve-user-info
        });
    }*/

    /*private Route deleteUser(String name) {
        return
                //#users-delete-logic
                delete(() -> {
                    CompletionStage<ActionPerformed> userDeleted = Patterns
                            .ask(userRegistryActor, new UserRegistryMessages.DeleteUser(name), timeout)
                            .thenApply(ActionPerformed.class::cast);

                    return onSuccess(() -> userDeleted,
                            performed -> {
                                log.info("Deleted user [{}]: {}", name, performed.getDescription());
                                return complete(StatusCodes.OK, performed, Jackson.marshaller());
                            }
                    );
                });
        //#users-delete-logic
    }*/
//#users-get-delete