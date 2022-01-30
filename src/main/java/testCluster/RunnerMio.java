package testCluster;

public class RunnerMio {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Starting Cluster ---");

        ProducerNode p1 = new ProducerNode("25565");

        WorkerNode w1 = new WorkerNode("25566");
        WorkerNode w2 = new WorkerNode("25567");

        Thread.sleep(5000);

        System.out.println("--- Cluster Started ---");
        System.out.println("--- Sending messages ---");

        p1.sendMessages();
    }
}
