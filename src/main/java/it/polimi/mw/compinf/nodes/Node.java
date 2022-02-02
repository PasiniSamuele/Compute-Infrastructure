package it.polimi.mw.compinf.nodes;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public abstract class Node {
    public Node(String port, String role) {
        startNode(port, role);
    }

    abstract void startNode(String port, String role);

    static Config setupClusterNodeConfig(String port, String role) {
        return ConfigFactory.parseString(
                        String.format("akka.remote.netty.tcp.port=%s%n", port)
                                + String.format("akka.remote.artery.canonical.port=%s%n", port)
                                + String.format("akka.cluster.roles=[\"%s\"]%n", role))
                .withFallback(ConfigFactory.load());
    }
}
