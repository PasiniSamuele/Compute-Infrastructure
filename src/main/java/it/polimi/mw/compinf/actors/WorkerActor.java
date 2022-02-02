package it.polimi.mw.compinf.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import it.polimi.mw.compinf.http.TaskRegistryMessage;
import it.polimi.mw.compinf.tasks.CompressionTask;
import it.polimi.mw.compinf.tasks.Task;

import java.util.Optional;
import java.util.UUID;

public class WorkerActor extends AbstractLoggingActor {
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

		onFinishedTask("test".getBytes(), message.getDirectoryName());

		log().info("Finished {}", uuid);
		getSender().tell(new TaskRegistryMessage.TaskExecutedMessage(uuid), getSelf());
	}

	private void onFinishedTask(byte[] file, String directoryName) {
		// TODO Send file to StoreKeeper node
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
