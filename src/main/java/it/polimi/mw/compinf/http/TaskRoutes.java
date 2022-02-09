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
import it.polimi.mw.compinf.tasks.CompressionTask;
import it.polimi.mw.compinf.tasks.ConversionTask;
import it.polimi.mw.compinf.tasks.PrimeTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * Routes can be defined in separated classes like shown in here
 */
public class TaskRoutes extends AllDirectives {
    private final ActorRef taskRegistryActor;
    private final static Logger log = LoggerFactory.getLogger(TaskRoutes.class);
    private final Duration askTimeout;

    public TaskRoutes(ActorSystem system, ActorRef taskRegistryActor) {
        this.taskRegistryActor = taskRegistryActor;
        askTimeout = system.settings().config().getDuration("akka.http.routes.ask-timeout");
    }

    private CompletionStage<TaskRegistryMessage.TaskCreationMessage> createCompressionMessage(CompressionTask task) {
        return Patterns.ask(taskRegistryActor, new TaskRegistryMessage.CreateCompressionMessage(task), askTimeout)
                .thenApply(TaskRegistryMessage.TaskCreationMessage.class::cast);
    }

    private CompletionStage<TaskRegistryMessage.TaskCreationMessage> createConversionMessage(ConversionTask task) {
        return Patterns.ask(taskRegistryActor, new TaskRegistryMessage.CreateConversionMessage(task), askTimeout)
                .thenApply(TaskRegistryMessage.TaskCreationMessage.class::cast);
    }

    private CompletionStage<TaskRegistryMessage.TaskCreationMessage> createPrimeMessage(PrimeTask task) {
        return Patterns.ask(taskRegistryActor, new TaskRegistryMessage.CreatePrimeMessage(task), askTimeout)
                .thenApply(TaskRegistryMessage.TaskCreationMessage.class::cast);
    }

    private CompletionStage<TaskRegistryMessage.GetSSEMessage> getSSESource(UUID uuid) {
        return Patterns.ask(taskRegistryActor, new TaskRegistryMessage.CreateSSEMessage(uuid), askTimeout)
                .thenApply(TaskRegistryMessage.GetSSEMessage.class::cast)
                .exceptionally(e -> {
                    log.error("Invalid SSE request with UUID: {}", e.getMessage());
                    return null;
                });
    }

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
                        onSuccess(getSSESource(uuid), sse -> {
                            if (sse == null) {
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
                post(() ->
                        entity(
                                Jackson.unmarshaller(CompressionTask.class),
                                task -> onComplete(createCompressionMessage(task),
                                        result -> {
                                            if (result.isFailure()) {
                                                log.warn("Compression task refused due to cluster unavailability");
                                                return complete(StatusCodes.SERVICE_UNAVAILABLE);
                                            } else {
                                                TaskRegistryMessage.TaskCreationMessage msg = result.get();
                                                log.info("Compression task accepted with UUID: {}", msg.getMessage());
                                                return complete(StatusCodes.ACCEPTED, msg, Jackson.marshaller());
                                            }
                                        }
                                )
                        )
                )
        ));
    }

    private Route conversionTaskRoutes() {
        return pathPrefix("conversion", () -> concat(
                post(() ->
                        entity(
                                Jackson.unmarshaller(ConversionTask.class),
                                task -> onComplete(createConversionMessage(task),
                                        result -> {
                                            if (result.isFailure()) {
                                                log.warn("Conversion task refused due to cluster unavailability");
                                                return complete(StatusCodes.SERVICE_UNAVAILABLE);
                                            } else {
                                                TaskRegistryMessage.TaskCreationMessage msg = result.get();
                                                log.info("Conversion task accepted with UUID: {}", msg.getMessage());
                                                return complete(StatusCodes.ACCEPTED, msg, Jackson.marshaller());
                                            }
                                        }
                                )
                        )
                )
        ));
    }

    private Route primeTaskRoutes() {
        return pathPrefix("prime", () -> concat(
                post(() ->
                        entity(
                                Jackson.unmarshaller(PrimeTask.class),
                                task -> onComplete(createPrimeMessage(task),
                                        result -> {
                                            if (result.isFailure()) {
                                                log.warn("Prime task refused due to cluster unavailability");
                                                return complete(StatusCodes.SERVICE_UNAVAILABLE);
                                            } else {
                                                TaskRegistryMessage.TaskCreationMessage msg = result.get();
                                                log.info("Prime task accepted with UUID: {}", msg.getMessage());
                                                return complete(StatusCodes.ACCEPTED, msg, Jackson.marshaller());
                                            }
                                        }
                                )
                        )
                )
        ));
    }
}