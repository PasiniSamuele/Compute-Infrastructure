package it.polimi.mw.compinf.http;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import it.polimi.mw.compinf.http.TaskRegistryActor.Task;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

/**
 * Routes can be defined in separated classes like shown in here
 */
//#user-routes-class
public class TaskRoutes extends AllDirectives {
    //#user-routes-class
    final private ActorRef taskRegistryActor;
    final private LoggingAdapter log;


    public TaskRoutes(ActorSystem system, ActorRef taskRegistryActor) {
        this.taskRegistryActor = taskRegistryActor;
        log = Logging.getLogger(system, this);
    }

    // Required by the `ask` (?) method below
    Duration timeout = Duration.ofSeconds(5l); // usually we'd obtain the timeout from the system's configuration

    /**
     * This method creates one route (of possibly many more that will be part of your Web App)
     */
    //#all-routes
    //#users-get-delete
    public Route taskRoutes() {
        return route(pathPrefix("tasks", () ->
                route(
                        getOrPostUsers()
                        /*path(PathMatchers.segment(), id -> route(
                                        getTask(id),
                                        deleteTask(id)
                                )
                        )*/
                )
        ));
    }

    //#tasks-get-post
    private Route getOrPostUsers() {
        return pathEnd(() ->
                route(
                        get(() -> {
                            CompletionStage<TaskRegistryActor.Tasks> futureUsers = Patterns
                                    .ask(taskRegistryActor, new TaskRegistryMessages.GetTasks(), timeout)
                                    .thenApply(TaskRegistryActor.Tasks.class::cast);
                            return onSuccess(() -> futureUsers,
                                    users -> complete(StatusCodes.OK, users, Jackson.marshaller()));
                        }),
                        post(() ->
                                entity(
                                        Jackson.unmarshaller(Task.class),
                                        task -> {
                                            CompletionStage<TaskRegistryMessages.ActionPerformed> taskCreated = Patterns
                                                    .ask(taskRegistryActor, new TaskRegistryMessages.CreateTask(task), timeout)
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



/**
 * This method creates one route (of possibly many more that will be part of your Web App)
 * <p>
 * //#all-routes
 * public Route taskRoutes() {
 * return pathPrefix("tasks", () -> concat(
 * pathEnd(() ->
 * concat(
 * //#tasks-get
 * get(() ->
 * onSuccess(getTasks(),
 * tasks -> complete(StatusCodes.OK, tasks, Jackson.marshaller())
 * )
 * ),
 * //#task-create
 * post(() ->
 * entity(
 * Jackson.unmarshaller(Task.class),
 * task -> onSuccess(createTask(task), performed -> {
 * log.info("Create result: {}", performed.description);
 * return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
 * })
 * )
 * )
 * )
 * )
 * ));
 * }
 * }
 * <p>
 * //#all-routes
 * public Route taskRoutes() {
 * return pathPrefix("tasks", () -> concat(
 * pathEnd(() ->
 * concat(
 * //#tasks-get
 * get(() ->
 * onSuccess(getTasks(),
 * tasks -> complete(StatusCodes.OK, tasks, Jackson.marshaller())
 * )
 * ),
 * //#task-create
 * post(() ->
 * entity(
 * Jackson.unmarshaller(Task.class),
 * task -> onSuccess(createTask(task), performed -> {
 * log.info("Create result: {}", performed.description);
 * return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
 * })
 * )
 * )
 * )
 * )
 * ));
 * }
 * }
 * <p>
 * //#all-routes
 * public Route taskRoutes() {
 * return pathPrefix("tasks", () -> concat(
 * pathEnd(() ->
 * concat(
 * //#tasks-get
 * get(() ->
 * onSuccess(getTasks(),
 * tasks -> complete(StatusCodes.OK, tasks, Jackson.marshaller())
 * )
 * ),
 * //#task-create
 * post(() ->
 * entity(
 * Jackson.unmarshaller(Task.class),
 * task -> onSuccess(createTask(task), performed -> {
 * log.info("Create result: {}", performed.description);
 * return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
 * })
 * )
 * )
 * )
 * )
 * ));
 * }
 * }
 */
/**
 //#all-routes
 public Route taskRoutes() {
 return pathPrefix("tasks", () -> concat(
 pathEnd(() ->
 concat(
 //#tasks-get
 get(() ->
 onSuccess(getTasks(),
 tasks -> complete(StatusCodes.OK, tasks, Jackson.marshaller())
 )
 ),
 //#task-create
 post(() ->
 entity(
 Jackson.unmarshaller(Task.class),
 task -> onSuccess(createTask(task), performed -> {
 log.info("Create result: {}", performed.description);
 return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
 })
 )
 )
 )
 )
 ));
 }
 }
 */