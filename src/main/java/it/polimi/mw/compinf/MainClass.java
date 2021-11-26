package it.polimi.mw.compinf;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.routing.BalancingPool;
import akka.actor.OneForOneStrategy;
import akka.japi.pf.DeciderBuilder;
import java.time.Duration;

public class MainClass {
	
	public static int POOL_ACTORS = 2;
	
	public static int SUPERVISOR_RETRIES = 10;
	public static int SUPERVISOR_PERIOD = 1;
	
	public static void main(String[] args) {
		ActorSystem sys = ActorSystem.create("System");

		// TODO Disable logging of exception stack trace
		SupervisorStrategy strategy = new OneForOneStrategy(SUPERVISOR_RETRIES,
				Duration.ofMinutes(SUPERVISOR_PERIOD),
				DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart()).build());
		
		ActorRef router = sys.actorOf(new BalancingPool(POOL_ACTORS, strategy, "pool-dispatcher").props(Props.create(Actor.class)), "Router");

		for (int i = 0; i < 12; i++) {
			router.tell(new TaskMessage(i), ActorRef.noSender());
		}
	}
}
