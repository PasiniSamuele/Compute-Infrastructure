package it.polimi.mw.compinf;

import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import akka.dispatch.PriorityGenerator;
import akka.dispatch.UnboundedStablePriorityMailbox;

class PriorityMailbox extends UnboundedStablePriorityMailbox {

	public PriorityMailbox(ActorSystem.Settings settings, Config config) {
		super(new PriorityGenerator() {
			@Override
			public int gen(Object message) {
				TaskMessage taskMessage = (TaskMessage) message;
				throw new RuntimeException();
				//return taskMessage.getPriority();
			}
		});
	}
}
