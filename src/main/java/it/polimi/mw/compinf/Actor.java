package it.polimi.mw.compinf;

import java.util.NoSuchElementException;
import java.util.Optional;

import akka.actor.AbstractActor;
import it.polimi.mw.compinf.message.AnotherTaskMessage;
import it.polimi.mw.compinf.message.TaskMessage;

public class Actor extends AbstractActor {
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(TaskMessage.class, this::onMessage)
				.match(AnotherTaskMessage.class, this::onMessageNew).build();
	}

	private void onMessage(TaskMessage message) throws Exception {
		System.out.println("Received " + message.getId());

		int randInt = (int) (Math.random() * 4 + 1);

		// Simulating random task failure
		if (randInt > 3) {
			System.out.println("Exception " + message.getId());
			throw new Exception();
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
		
		try {
			TaskMessage taskMessage = (TaskMessage) message.get();
			// getContext().getSender().tell(taskMessage.increasePriority(), getContext().getSelf());
			getContext().getSelf().tell(taskMessage.increasePriority(), getContext().getSender());
		} catch(NoSuchElementException e) {
			System.out.println("Restart got null message!");
		}
		
	}

	@Override
	public void postRestart(Throwable reason) {
		System.out.println("...now restarted!");
	}
}
