package it.polimi.mw.compinf;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import it.polimi.mw.compinf.tasks.CompressionTask;
import it.polimi.mw.compinf.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static it.polimi.mw.compinf.http.TaskRegistryMessage.*;

public class Worker extends AbstractActor {
	LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(CompressionTask.class, this::onCompressionTask)
				.build();
	}

	private void onCompressionTask(CompressionTask message) throws Exception {
		UUID uuid = message.getUUID();
		log.info("Received {}", uuid);

		int randInt = (int) (Math.random() * 4 + 1);

		// Simulating random task failure
		if (randInt > 3) {
			log.error("Exception {}", uuid);
			throw new Exception();
		}

		try {
			// FIXME
			//Thread.sleep(randInt * 1000L);
			Thread.sleep(10 * 1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String dirName = message.getDirectoryName();
		writeFile(dirName, uuid.toString());

		getSender().tell(new TaskExecutedMessage(uuid), getSelf());

		log.info("Finished {}", uuid);

	}

	private void writeFile(String directoryName, String fileName) {
//		String uploadLocation = getServletContext().getInitParameter("upload.location");
//		Path fullPath = Path.of(uploadLocation + "/" + filename);
//
//		if (!Files.exists(Path.of(uploadLocation)) ) {
//			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not find the image folder. Please fix the specified folder in the web.xml file.");
//			return;
//		}
//		if (filename.isEmpty() || !Files.exists(fullPath)) {
//			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not find the requested image.");
//			return;
//		}

		// Creates directory if it does not exist and file
		try {
			Files.createDirectories(Path.of(directoryName));
			Files.write(Path.of(directoryName + File.separator + fileName), "Piero Paolo".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
