package it.polimi.mw.compinf;

import it.polimi.mw.compinf.nodes.HttpNode;
import it.polimi.mw.compinf.nodes.StoreKeeperNode;
import it.polimi.mw.compinf.nodes.WorkerNode;

import java.util.Locale;
import java.util.regex.Pattern;

public class MainApp {
    private enum Role {
        HTTP, WORKER, STOREKEEPER
    }

    private static boolean isValidAddress(String address) {
        Pattern pattern = Pattern.compile("^"
                + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}" // Domain name
                + "|"
                + "localhost" // localhost
                + "|"
                + "(([0-9]{1,3}\\.){3})[0-9]{1,3})" // Ip
                + ":"
                + "[0-9]{1,5}$"); // Port

        return pattern.matcher(address).matches();
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Invalid parameters!");
            return;
        }

        Role role;
        try {
            role = Role.valueOf(args[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid node role!");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid node port!");
            return;
        }

        String seedNode;
        if (!isValidAddress(args[2])) {
            System.out.println("Invalid seed node address!");
            return;
        } else {
            seedNode = args[2];
        }

        String kafkaAddr;
        if (!isValidAddress(args[3])) {
            System.out.println("Invalid kafka address!");
            return;
        } else {
            kafkaAddr = args[3];
        }

        switch (role) {
            case HTTP:
                new HttpNode(Integer.toString(port), seedNode, kafkaAddr);
                break;
            case STOREKEEPER:
                new StoreKeeperNode(Integer.toString(port), seedNode, kafkaAddr);
                break;
            case WORKER:
                new WorkerNode(Integer.toString(port), seedNode, kafkaAddr);
                break;
        }
    }
}
