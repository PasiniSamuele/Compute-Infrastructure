package it.polimi.mw.compinf.nodes;

import akka.actor.ActorSystem;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import akka.routing.FromConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.polimi.mw.compinf.actors.WorkerActor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class WorkerNode {
    private final static int SUPERVISOR_RETRIES = 10;
    private final static int SUPERVISOR_PERIOD = 1;

    public WorkerNode(String port) {
        ActorSystem actorSystem = ActorSystem.create("cluster", setupClusterNodeConfig(port));

        // Creating supervision strategy for the local router
        Duration duration = Duration.create(SUPERVISOR_PERIOD, TimeUnit.MINUTES);
        SupervisorStrategy strategy = new OneForOneStrategy(SUPERVISOR_RETRIES, duration, false,
                DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart()).build());

        // Creating the local router (this is a balancing pool)
        actorSystem.actorOf(
                FromConfig
                        .getInstance()
                        .withSupervisorStrategy(strategy)
                        .props(WorkerActor.props()),
                "workerRouter");

        actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());
    }

    private static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                        String.format("akka.remote.netty.tcp.port=%s%n", port) +
                                String.format("akka.remote.artery.canonical.port=%s%n", port))
                .withFallback(ConfigFactory.load());
    }
}
