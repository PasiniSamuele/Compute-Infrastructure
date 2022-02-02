package it.polimi.mw.compinf.nodes;

import akka.actor.*;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import it.polimi.mw.compinf.http.TaskRegistryActor;
import it.polimi.mw.compinf.http.TaskRoutes;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public class HttpNode extends Node {
    public HttpNode(String port) {
        super(port, "http");
    }

    @Override
    void startNode(String port, String role) {
        // Boot up server using the route as defined below
        ActorSystem system = ActorSystem.create("cluster", setupClusterNodeConfig(port, role));

        ActorRef taskRegistryActor = system.actorOf(TaskRegistryActor.props(), "taskRegistryActor");

        TaskRoutes taskRoutes = new TaskRoutes(system, taskRegistryActor);
        CompletionStage<ServerBinding> futureBinding = Http.get(system).newServerAt("localhost", 8080).bind(taskRoutes.taskRoutes());

        futureBinding.whenComplete((binding, exception) -> {
            if (binding != null) {
                InetSocketAddress address = binding.localAddress();
                system.log().info("Server online at http://{}:{}/", address.getHostString(), address.getPort());
            } else {
                system.log().error("Failed to bind HTTP endpoint, terminating system", exception);
                system.terminate();
            }
        });
    }
}
