package it.polimi.mw.compinf.nodes;

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

    public WorkerNode(String port) {
        super(port);
    }

    @Override
    void startNode() {
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
}
