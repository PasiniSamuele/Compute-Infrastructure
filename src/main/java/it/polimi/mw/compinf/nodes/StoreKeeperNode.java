package it.polimi.mw.compinf.nodes;

import akka.actor.ActorSystem;
import it.polimi.mw.compinf.actors.StoreKeeperActor;

public class StoreKeeperNode extends Node {
    public StoreKeeperNode(String port) {
        super(port, "storeKeeper");
    }

    @Override
    void startNode(String port, String role) {
        ActorSystem actorSystem = ActorSystem.create("cluster", setupClusterNodeConfig(port, role));
        actorSystem.actorOf(StoreKeeperActor.props(), "storeKeeper");
    }
}
