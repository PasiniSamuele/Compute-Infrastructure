package it.polimi.mw.compinf.actors;

import akka.actor.AbstractLoggingActor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StoreKeeperActor extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        return null;
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
}
