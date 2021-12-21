package it.polimi.mw.compinf.http;

import akka.NotUsed;
import akka.http.javadsl.model.sse.ServerSentEvent;
import akka.stream.javadsl.Source;

import java.io.Serializable;
import java.util.UUID;

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

    class CreateSSE implements Serializable {
        private final UUID uuid;

        public CreateSSE(UUID uuid) {
            this.uuid = uuid;
        }

        public UUID getUUID() {
            return uuid;
        }
    }

    class GetSSE implements Serializable {
        private Source<ServerSentEvent, NotUsed> source;

        public GetSSE(Source<ServerSentEvent, NotUsed> source) {
            this.source = source;
        }

        public Source<ServerSentEvent, NotUsed> getSource() {
            return source;
        }
    }



    class TaskExecuted implements Serializable {
        private final UUID uuid;

        public TaskExecuted(UUID uuid) {
            this.uuid = uuid;
        }

        public UUID getUUID() {
            return uuid;
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