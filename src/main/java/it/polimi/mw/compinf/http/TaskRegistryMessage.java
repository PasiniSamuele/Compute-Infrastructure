package it.polimi.mw.compinf.http;

import akka.NotUsed;
import akka.http.javadsl.model.sse.ServerSentEvent;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.annotation.JsonCreator;
import it.polimi.mw.compinf.tasks.CborSerializable;
import it.polimi.mw.compinf.tasks.CompressionTask;

import java.io.Serializable;
import java.util.UUID;

public interface TaskRegistryMessage {
    class GenericMessage implements CborSerializable {
        private final String message;

        public GenericMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    class CreateCompressionMessage implements CborSerializable {
        private final CompressionTask compressionTask;

        public CreateCompressionMessage(CompressionTask compressionTask) {
            this.compressionTask = compressionTask;
        }

        public CompressionTask getCompressionTask() {
            return compressionTask;
        }
    }

    class CreateSSEMessage implements CborSerializable {
        private final UUID uuid;

        public CreateSSEMessage(UUID uuid) {
            this.uuid = uuid;
        }

        public UUID getUUID() {
            return uuid;
        }
    }

    class GetSSEMessage implements CborSerializable {
        private final Source<ServerSentEvent, NotUsed> source;

        public GetSSEMessage(Source<ServerSentEvent, NotUsed> source) {
            this.source = source;
        }

        public Source<ServerSentEvent, NotUsed> getSource() {
            return source;
        }
    }

    class TaskExecutedMessage implements CborSerializable {
        private final UUID uuid;

        // Note: annotation is needed since the constructor has a single parameter.
        // For more info, check here:
        // https://doc.akka.io/docs/akka/current/serialization-jackson.html#constructor-with-single-parameter
        @JsonCreator
        public TaskExecutedMessage(UUID uuid) {
            this.uuid = uuid;
        }

        public UUID getUUID() {
            return uuid;
        }
    }
}