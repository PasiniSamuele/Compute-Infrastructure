package it.polimi.mw.compinf.nodes;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import akka.routing.FromConfig;
import it.polimi.mw.compinf.actors.WorkerActor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class WorkerNode extends Node {
    private final static int SUPERVISOR_RETRIES = 10;
    private final static int SUPERVISOR_PERIOD = 1;

    public WorkerNode(String port, String seed, String kafka) {
        super("worker", port, seed, kafka);
    }

    @Override
    void startNode(String role, String port, String seed, String kafka) {
        ActorSystem actorSystem = ActorSystem.create("cluster", setupClusterNodeConfig(role, port, seed));

        // Creating supervision strategy for the local router
        Duration duration = Duration.create(SUPERVISOR_PERIOD, TimeUnit.MINUTES);
        SupervisorStrategy strategy = new OneForOneStrategy(SUPERVISOR_RETRIES, duration, false,
                DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart()).build());

        // Router to send messages to StoreKeeper node.
        ActorRef storeKeeperRouter = actorSystem.actorOf(FromConfig.getInstance().props(), "storeKeeperRouter");

        // Creating the local router (this is a balancing pool)
        actorSystem.actorOf(
                FromConfig
                        .getInstance()
                        .withSupervisorStrategy(strategy)
                        .props(WorkerActor.props(kafka, storeKeeperRouter)),
                "workerPoolRouter");

        actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());
    }
}
