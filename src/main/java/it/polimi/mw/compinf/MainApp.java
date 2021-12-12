package it.polimi.mw.compinf;

import akka.actor.*;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.japi.pf.DeciderBuilder;
import akka.routing.FromConfig;
import it.polimi.mw.compinf.http.TaskRegistryActor;
import it.polimi.mw.compinf.http.TaskRoutes;
import scala.concurrent.duration.Duration;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class MainApp {
    public static int SUPERVISOR_RETRIES = 10;
    public static int SUPERVISOR_PERIOD = 1;

    private static ActorRef startExecutionSystem() {
        ActorSystem system = ActorSystem.create("executionSystem");

        Duration duration = Duration.create(SUPERVISOR_PERIOD, TimeUnit.MINUTES);

        SupervisorStrategy strategy = new OneForOneStrategy(SUPERVISOR_RETRIES, duration, false,
                DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart()).build());

        return system.actorOf(
                FromConfig.getInstance().withSupervisorStrategy(strategy).props(Props.create(Actor.class)), "actorRouter");
    }

    private static void startHttpServer(ActorRef actorRouter) {
        // Boot up server using the route as defined below
        ActorSystem system = akka.actor.ActorSystem.create("httpServer");
        ActorRef taskRegistryActor = system.actorOf(TaskRegistryActor.props(actorRouter), "taskRegistryActor");

        TaskRoutes taskRoutes = new TaskRoutes(system, taskRegistryActor);
        CompletionStage<ServerBinding> futureBinding =
                Http.get(system).newServerAt("localhost", 8080).bind(taskRoutes.taskRoutes());

        futureBinding.whenComplete((binding, exception) -> {
            if (binding != null) {
                InetSocketAddress address = binding.localAddress();
                system.log().info("Server online at http://{}:{}/",
                        address.getHostString(),
                        address.getPort());
            } else {
                system.log().error("Failed to bind HTTP endpoint, terminating system", exception);
                system.terminate();
            }
        });
    }

    public static void main(String[] args) {
        ActorRef actorRouter = startExecutionSystem();
        startHttpServer(actorRouter);
    }
}
