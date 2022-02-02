package it.polimi.mw.compinf;

import it.polimi.mw.compinf.nodes.HttpNode;
import it.polimi.mw.compinf.nodes.StoreKeeperNode;
import it.polimi.mw.compinf.nodes.WorkerNode;

public class MainApp {
    public static void main(String[] args) {
        new HttpNode("25565");

        new StoreKeeperNode("7777");

        //new WorkerNode("30001");
        //new WorkerNode("30002");
    }
}
