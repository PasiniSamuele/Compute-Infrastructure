package it.polimi.mw.compinf.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import it.polimi.mw.compinf.tasks.CompressionTask;
import it.polimi.mw.compinf.tasks.Task;
import it.polimi.mw.compinf.tasks.TaskResult;

import java.util.Optional;
import java.util.UUID;

public class WorkerActor extends AbstractLoggingActor {
	ActorSelection storeKeeper = getContext().actorSelection("akka://cluster@127.0.0.1:7777/user/storeKeeper");

	public static Props props() {
		return Props.create(WorkerActor.class);
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(CompressionTask.class, this::onCompressionTask)
				.build();
	}

	private void onCompressionTask(CompressionTask message) throws Exception {
		UUID uuid = message.getUUID();
		log().info("Received {}", uuid);

		// TODO Dummy compression

		Thread.sleep(10000);

		onFinishedTask(uuid, "test".getBytes(), message.getDirectoryName());
	}

	private void onFinishedTask(UUID uuid, byte[] file, String directoryName) {
		TaskResult taskResult = new TaskResult(uuid, file, directoryName);
		storeKeeper.tell(taskResult, self());
	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) {
		if (message.isPresent()) {
			try {
				Task taskMessage = (Task) message.get();

				log().warning("Restarting task {} due to failure", taskMessage.getDirectoryName());

				// Resending the message with a higher priority in order to process it first
				getContext().getSelf().tell(taskMessage.increasePriority(), getContext().getSender());
			} catch (ClassCastException e) {
				log().error("Invalid task message");
			}
		}
	}
}
