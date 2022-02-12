package it.polimi.mw.compinf.nodes;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.management.javadsl.AkkaManagement;
import akka.routing.FromConfig;
import it.polimi.mw.compinf.http.HttpActor;
import it.polimi.mw.compinf.http.HttpRoutes;

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

        // Router to send messages to Worker nodes.
        ActorRef workerRouter = actorSystem.actorOf(FromConfig.getInstance().props(), "workerRouter");

        // Router to send messages to StoreKeeper node.
        ActorRef storeKeeperRouter = actorSystem.actorOf(FromConfig.getInstance().props(), "storeKeeperRouter");

        ActorRef httpActor = actorSystem.actorOf(HttpActor.props(kafka, workerRouter, storeKeeperRouter), "httpActor");

        HttpRoutes httpRoutes = new HttpRoutes(actorSystem, httpActor);
        CompletionStage<ServerBinding> futureBinding = Http.get(actorSystem).newServerAt("0.0.0.0", 40000).bind(httpRoutes.taskRoutes());

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
