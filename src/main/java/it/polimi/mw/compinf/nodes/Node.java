package it.polimi.mw.compinf.nodes;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public abstract class Node {
    String port;

    public Node(String port) {
        this.port = port;
        startNode();
    }

    abstract void startNode();

    static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                        String.format("akka.remote.netty.tcp.port=%s%n", port) +
                                String.format("akka.remote.artery.canonical.port=%s%n", port))
                .withFallback(ConfigFactory.load());
    }
}
