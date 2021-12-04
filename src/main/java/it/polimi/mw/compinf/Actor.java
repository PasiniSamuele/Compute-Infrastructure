package it.polimi.mw.compinf;

import java.util.Optional;

import akka.actor.AbstractActor;
import jdk.internal.org.jline.utils.ShutdownHooks.Task;

public class Actor extends AbstractActor {
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(TaskMessage.class, this::onMessage)
				.match(AnotherTaskMessage.class, this::onMessageNew)
				.build();
	}
	
	private void onMessage(TaskMessage message) throws Exception {
		System.out.println("Received " + message.getId());
		
		int randInt = (int) (Math.random() * 4 + 1);
		
		// Simulating random task failure
		if (randInt > 3) {
			System.out.println("Exception " + message.getId());
			//throw new Exception();
		}
		
		try {
			Thread.sleep(randInt * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Finished " + message.getId());
	}
	
	private void onMessageNew(AnotherTaskMessage message) {
		System.out.println("Another");
	}
	@Override
	public void preRestart(Throwable reason, Optional<Object> message) {
		System.out.println("Restarting...");
		
		// Pay attention to infinite loops!
		// https://stackoverflow.com/questions/13542921/akka-resending-the-breaking-message
		
		// TODO What happens if I have a very long tasks queue and one of the first task dies?
		// Should we put it in a priority queue? Otherwise it will be sent to the end of the queue
		// So it will be restarted after the last inserted task
		
		// FIXME Patch orElse == null
		getContext().getSelf().tell(message.orElse(null), getContext().getSender());
	}
	
	@Override
	public void postRestart(Throwable reason) {
		System.out.println("...now restarted!");	
	}
}
