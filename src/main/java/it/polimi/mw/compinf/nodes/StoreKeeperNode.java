package it.polimi.mw.compinf.nodes;

import akka.actor.ActorSystem;
import it.polimi.mw.compinf.actors.StoreKeeperActor;

public class StoreKeeperNode extends Node {
    public StoreKeeperNode(String port) {
        super(port);
    }

    @Override
    void startNode() {
        ActorSystem actorSystem = ActorSystem.create("cluster", setupClusterNodeConfig(port));
        actorSystem.actorOf(StoreKeeperActor.props(), "storeKeeper");
    }
}
