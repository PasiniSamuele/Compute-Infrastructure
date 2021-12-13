package it.polimi.mw.compinf.http;

import java.io.Serializable;

public interface TaskRegistryMessage {

    class ActionPerformed implements Serializable {
        private final String description;

        public ActionPerformed(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    class CreateCompressionMessage implements Serializable {
        private final CompressionTask compressionTask;

        public CreateCompressionMessage(CompressionTask compressionTask) {
            this.compressionTask = compressionTask;
        }

        public CompressionTask getCompressionTask() {
            return compressionTask;
        }
    }

/*    class CreateConversionTask implements Serializable {
        private final Task task;

        public CreateConversionTask(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }
    }

    class CreateDownloadTask implements Serializable {
        private final Task task;

        public CreateDownloadTask(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }
    }*/
}