package it.polimi.mw.compinf;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.polimi.mw.compinf.http.CompressionTask;
import it.polimi.mw.compinf.http.Task;
import it.polimi.mw.compinf.http.TaskRegistryMessage;

import java.util.NoSuchElementException;
import java.util.Optional;

import static it.polimi.mw.compinf.http.TaskRegistryMessage.*;

public class Actor extends AbstractActor {
	LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(CompressionTask.class, this::onCompressionTask)
				.build();
	}

	private void onCompressionTask(CompressionTask message) throws Exception {
		log.info("Received {}", message.getUUID());

		int randInt = (int) (Math.random() * 4 + 1);

		// Simulating random task failure
		if (randInt > 3) {
			log.error("Exception {}", message.getUUID());
			throw new Exception();
		}

		try {
			// FIXME
			//Thread.sleep(randInt * 1000L);
			Thread.sleep(10 * 1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		getSender().tell(new TaskExecuted(message.getUUID()), getSelf());

		log.info("Finished {}", message.getUUID());

	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) {
		log.info("Restarting...");
		
		try {
			Task taskMessage = (Task) message.get();
			// getContext().getSender().tell(taskMessage.increasePriority(), getContext().getSelf());
			getContext().getSelf().tell(taskMessage.increasePriority(), getContext().getSender());
		} catch(NoSuchElementException e) {
			log.error("Restart got null message!");
		}
		
	}

	@Override
	public void postRestart(Throwable reason) {
		log.info("...now restarted!");
	}
}
