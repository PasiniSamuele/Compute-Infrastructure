package it.polimi.mw.compinf;

import it.polimi.mw.compinf.nodes.HttpNode;
import it.polimi.mw.compinf.nodes.WorkerNode;

public class MainApp {
    public static void main(String[] args) {
        new HttpNode("25565");
        new WorkerNode("25566");
        new WorkerNode("25567");
    }
}
