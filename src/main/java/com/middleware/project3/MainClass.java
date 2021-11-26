package com.middleware.project3;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.BalancingPool;

public class MainClass {

	public static void main(String[] args) {
		ActorSystem sys = ActorSystem.create("System");
		ActorRef router = sys.actorOf(new BalancingPool(5).props(Props.create(Actor.class)), "Router");

		for (int i = 0; i < 12; i++) {
			router.tell(new Message(i), ActorRef.noSender());
		}
	}
}
