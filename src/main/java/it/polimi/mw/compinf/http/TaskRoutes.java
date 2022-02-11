package it.polimi.mw.compinf.http;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.sse.EventStreamMarshalling;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import it.polimi.mw.compinf.exceptions.ClusterUnavailableException;
import it.polimi.mw.compinf.exceptions.InvalidUUIDException;
import it.polimi.mw.compinf.tasks.CompressionTask;
import it.polimi.mw.compinf.tasks.ConversionTask;
import it.polimi.mw.compinf.tasks.PrimeTask;
import it.polimi.mw.compinf.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * This class describes REST API routes available through HTTP.
 */
public class TaskRoutes extends AllDirectives {
    private final ActorRef taskRegistryActor;
    private final static Logger log = LoggerFactory.getLogger(TaskRoutes.class);
    private final Duration askTimeout;

    public TaskRoutes(ActorSystem system, ActorRef taskRegistryActor) {
        this.taskRegistryActor = taskRegistryActor;
        askTimeout = system.settings().config().getDuration("akka.http.routes.ask-timeout");
    }

    /**
     * Entry point for all available routes.
     * @return the routes available.
     */
    public Route taskRoutes() {
        return pathPrefix("tasks", () ->
                concat(
                        statusTaskRoutes(),
                        compressionTaskRoutes(),
                        conversionTaskRoutes(),
                        primeTaskRoutes()
                )
        );
    }

    private Route statusTaskRoutes() {
        return get(() ->
                pathSuffix(PathMatchers.uuidSegment(), uuid ->
                        onSuccess(getSSESource(uuid),
                                sse -> {
                                    if (sse == null) {
                                        log.error("Task status query refused due to cluster unavailability");
                                        return complete(StatusCodes.SERVICE_UNAVAILABLE);
                                    }

                                    if (sse.getSource() == null) {
                                        log.error("Invalid SSE request with UUID: {}", uuid);
                                        return complete(StatusCodes.BAD_REQUEST, "Invalid SSE request with UUID " + uuid);
                                    }

                                    log.info("Updating SSE events: {}", sse.getSource().toString());
                                    return completeOK(sse.getSource(), EventStreamMarshalling.toEventStream());
                                })
                )
        );
    }

    private Route compressionTaskRoutes() {
        return pathPrefix("compression", () -> concat(
                // Support HTTP POST method
                post(() ->
                        entity(
                                // Unmarshall JSON parameters received with the request
                                Jackson.unmarshaller(CompressionTask.class),
                                
                                // HTTP response
                                this::handleRoute
                        )
                )
        ));
    }

    private Route conversionTaskRoutes() {
        return pathPrefix("conversion", () -> concat(
                post(() ->
                        entity(
                                Jackson.unmarshaller(ConversionTask.class),
                                this::handleRoute
                        )
                )
        ));
    }

    private Route primeTaskRoutes() {
        return pathPrefix("prime", () -> concat(
                post(() ->
                        entity(
                                Jackson.unmarshaller(PrimeTask.class),
                                this::handleRoute
                        )
                )
        ));
    }

    private Route handleRoute(Task task) {
        return onSuccess(createTaskMessage(task),
                msg -> {
                    if (msg.getUUID() == null) {
                        //log.error("{} task refused due to cluster unavailability", task.getName());
                        log.error("task refused due to cluster unavailability");
                        return complete(StatusCodes.SERVICE_UNAVAILABLE);
                    } else {
                        //log.info("{} task accepted with UUID: {}", task.getName(), msg.getMessage());
                        log.info("task accepted with UUID: {}", msg.getMessage());
                        return complete(StatusCodes.ACCEPTED, msg, Jackson.marshaller());
                    }
                }
        );
    }

    private CompletionStage<TaskRegistryMessage.TaskCreationMessage> createTaskMessage(Task task) {
        return Patterns.ask(taskRegistryActor, new TaskRegistryMessage.CreateTaskMessage(task), askTimeout)
                .thenApply(TaskRegistryMessage.TaskCreationMessage.class::cast)
                .exceptionally(e -> new TaskRegistryMessage.TaskCreationMessage(null, null));
    }

    private CompletionStage<TaskRegistryMessage.GetSSEMessage> getSSESource(UUID uuid) {
        return Patterns.ask(taskRegistryActor, new TaskRegistryMessage.CreateSSEMessage(uuid), askTimeout)
                .thenApply(TaskRegistryMessage.GetSSEMessage.class::cast)
                .exceptionally(e -> {
                    if (e instanceof InvalidUUIDException) {
                        return new TaskRegistryMessage.GetSSEMessage(null);
                    } else {
                        return null;
                    }
                });
    }
}