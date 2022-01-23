package testCluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ProducerNode {
    private final ActorRef producer;

    public ProducerNode(String port) {
        ActorSystem actorSystem = ActorSystem.create("cluster", setupClusterNodeConfig(port));
        producer = actorSystem.actorOf(Producer.props(), "producer");

        actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());
    }

    private static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                        String.format("akka.remote.netty.tcp.port=%s%n", port) +
                                String.format("akka.remote.artery.canonical.port=%s%n", port))
                .withFallback(ConfigFactory.load());
    }

    public void sendMessages() {
        for (int i = 0; i < 10; i++) {
            producer.tell(i, ActorRef.noSender());
        }
    }
}
