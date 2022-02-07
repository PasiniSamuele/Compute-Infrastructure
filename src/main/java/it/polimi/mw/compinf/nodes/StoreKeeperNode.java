package it.polimi.mw.compinf.nodes;

import akka.actor.ActorSystem;
import it.polimi.mw.compinf.actors.StoreKeeperActor;

public class StoreKeeperNode extends Node {
    public StoreKeeperNode(String port, String seed, String kafka) {
        super("storeKeeper", port, seed, kafka);
    }

    @Override
    void startNode(String role, String port, String seed, String kafka) {
        ActorSystem actorSystem = ActorSystem.create("cluster", setupClusterNodeConfig(role, port, seed));
        actorSystem.actorOf(StoreKeeperActor.props(kafka), "storeKeeper");
    }
}
