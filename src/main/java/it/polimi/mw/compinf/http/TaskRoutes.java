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
import com.esotericsoftware.minlog.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;
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

    private CompletionStage<TaskRegistryMessage.ActionPerformed> createCompressionMessage(CompressionTask task) {
        return Patterns.ask(taskRegistryActor, new TaskRegistryMessage.CreateCompressionMessage(task), askTimeout)
                .thenApply(TaskRegistryMessage.ActionPerformed.class::cast);
    }

    private CompletionStage<TaskRegistryMessage.GetSSE> getSSESource(UUID uuid) {
        return Patterns.ask(taskRegistryActor, new TaskRegistryMessage.CreateSSE(uuid), askTimeout)
                .thenApply(TaskRegistryMessage.GetSSE.class::cast);
    }

    public Route taskRoutes() {
        return pathPrefix("tasks", () ->
                concat(
                        compressionTaskRoutes()//,
                        //conversionTaskRoutes(),
                        //downloadTaskRoutes()
                )
        );
    }

    private Route compressionTaskRoutes() {
        return pathPrefix("compression", () ->
                concat(
                        //#compression-task-create
                        post(() ->
                                entity(
                                        Jackson.unmarshaller(CompressionTask.class),
                                        task -> onSuccess(createCompressionMessage(task), performed -> {
                                            log.info("Compression task result: {}", performed.getDescription());
                                            return complete(StatusCodes.ACCEPTED, performed, Jackson.marshaller());
                                        })
                                )
                        ),
                        get(() ->
                                pathSuffix(PathMatchers.uuidSegment(), uuid ->
                                    onSuccess(getSSESource(uuid), sse -> {
                                        log.info("Updating SSE events: {}", sse.getSource().toString());
                                        return completeOK(sse.getSource(), EventStreamMarshalling.toEventStream());
                                    })
                                )
                        )
                ));
    }
}