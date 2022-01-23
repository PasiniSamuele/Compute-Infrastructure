package testCluster;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class WorkerNode {
    public WorkerNode(String port) {
        ActorSystem actorSystem = ActorSystem.create("cluster", setupClusterNodeConfig(port));
        actorSystem.actorOf(Worker.props(), "worker");
        actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());
    }

    private static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                        String.format("akka.remote.netty.tcp.port=%s%n", port) +
                                String.format("akka.remote.artery.canonical.port=%s%n", port))
                .withFallback(ConfigFactory.load());
    }
}
