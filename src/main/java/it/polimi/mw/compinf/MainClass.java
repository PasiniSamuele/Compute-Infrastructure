package it.polimi.mw.compinf;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.routing.FromConfig;
import akka.actor.OneForOneStrategy;
import akka.japi.pf.DeciderBuilder;
import java.time.Duration;

public class MainClass {
	public static int SUPERVISOR_RETRIES = 10;
	public static int SUPERVISOR_PERIOD = 1;

	public static void main(String[] args) {
		ActorSystem sys = ActorSystem.create("system");

		// TODO Disable logging of exception stack trace
		SupervisorStrategy strategy = new OneForOneStrategy(SUPERVISOR_RETRIES, Duration.ofMinutes(SUPERVISOR_PERIOD),
				DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart()).build());

		ActorRef router = sys.actorOf(
				FromConfig.getInstance().withSupervisorStrategy(strategy).props(Props.create(Actor.class)), "router");
		
		for (int i = 0; i < 20; i++) {
			router.tell(new TaskMessage(i), ActorRef.noSender());
		}

//		Message m1 = new TaskMessage(1);
//		Message m2 = new AnotherTaskMessage();
//		
//		router.tell(m1, ActorRef.noSender());
//		router.tell(m2, ActorRef.noSender());
	}
}
