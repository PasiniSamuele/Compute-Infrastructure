package it.polimi.mw.compinf.nodes;

import akka.actor.*;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.polimi.mw.compinf.http.TaskRegistryActor;
import it.polimi.mw.compinf.http.TaskRoutes;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public class HttpNode {
    public HttpNode(String port) {
        // Boot up server using the route as defined below
        ActorSystem system = ActorSystem.create("cluster", setupClusterNodeConfig(port));

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

    private static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                        String.format("akka.remote.netty.tcp.port=%s%n", port) +
                                String.format("akka.remote.artery.canonical.port=%s%n", port))
                .withFallback(ConfigFactory.load());
    }
}
