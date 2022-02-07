package it.polimi.mw.compinf.nodes;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public abstract class Node {
    public Node(String role, String port, String seed, String kafka) {
        startNode(role, port, seed, kafka);
    }

    abstract void startNode(String role, String port, String seed, String kafka); // INVERT

    static Config setupClusterNodeConfig(String role, String port, String seed) {
        return ConfigFactory.parseString(
                        String.format("akka.remote.netty.tcp.port=%s%n", port)
                                + String.format("akka.remote.artery.canonical.port=%s%n", port)
                                + String.format("akka.cluster.seed-nodes=[\"akka://cluster@%s\"]%n", seed)
                                + String.format("akka.cluster.roles=[\"%s\"]%n", role))
                .withFallback(ConfigFactory.load());
    }
}
