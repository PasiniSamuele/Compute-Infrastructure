package it.polimi.mw.compinf.http;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
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

    private CompletionStage<TaskRegistryMessage.ActionPerformed> createCompressionTask(CompressionTask task) {
        return Patterns.ask(taskRegistryActor, new TaskRegistryMessage.CreateCompressionMessage(task), askTimeout)
                .thenApply(TaskRegistryMessage.ActionPerformed.class::cast);
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
        return pathSuffix("compression", () ->
                concat(
                        //#compression-task-create
                        post(() ->
                                entity(
                                        Jackson.unmarshaller(CompressionTask.class),
                                        task -> onSuccess(createCompressionTask(task), performed -> {
                                            log.info("Create result: {}", performed.getDescription());
                                            return complete(StatusCodes.ACCEPTED, performed, Jackson.marshaller());
                                        })
                                )
                        )
                ));
    }

/*    private Route conversionTaskRoutes() {
        return pathSuffix("conversion", () ->
                concat(
                        //#conversion-task-create
                        post(() ->
                                entity(
                                        Jackson.unmarshaller(ConversionTask.class),
                                        task -> onSuccess(createConversionTask(task), performed -> {
                                            log.info("Create result: {}", performed.getDescription());
                                            return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
                                        })
                                )
                        )
                ));
    }*/

/*    private Route downloadTaskRoutes() {
        return pathSuffix("download", () ->
                concat(
                        //#download-task-create
                        post(() ->
                                entity(
                                        Jackson.unmarshaller(DownloadTask.class),
                                        task -> onSuccess(createDownloadTask(task), performed -> {
                                            log.info("Create result: {}", performed.getDescription());
                                            return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
                                        })
                                )
                        )
                ));
    }*/
}