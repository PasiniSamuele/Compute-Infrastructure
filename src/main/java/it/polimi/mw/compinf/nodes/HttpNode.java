package it.polimi.mw.compinf.nodes;

import akka.actor.*;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.management.javadsl.AkkaManagement;
import it.polimi.mw.compinf.http.TaskRegistryActor;
import it.polimi.mw.compinf.http.TaskRoutes;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public class HttpNode extends Node {
    public HttpNode(String port, String seed, String kafka) {
        super("http", port, seed, kafka);
    }

    @Override
    void startNode(String role, String port, String seed, String kafka) {
        // Boot up server using the route as defined below
        ActorSystem actorSystem = ActorSystem.create("cluster", setupClusterNodeConfig(role, port, seed));

        AkkaManagement.get(actorSystem).start();

        ActorRef taskRegistryActor = actorSystem.actorOf(TaskRegistryActor.props(kafka), "taskRegistryActor");

        TaskRoutes taskRoutes = new TaskRoutes(actorSystem, taskRegistryActor);
        CompletionStage<ServerBinding> futureBinding = Http.get(actorSystem).newServerAt("0.0.0.0", 40000).bind(taskRoutes.taskRoutes());

        futureBinding.whenComplete((binding, exception) -> {
            if (binding != null) {
                InetSocketAddress address = binding.localAddress();
                actorSystem.log().info("Server online at http://{}:{}/", address.getHostString(), address.getPort());
            } else {
                actorSystem.log().error("Failed to bind HTTP endpoint, terminating system", exception);
                actorSystem.terminate();
            }
        });
    }
}
