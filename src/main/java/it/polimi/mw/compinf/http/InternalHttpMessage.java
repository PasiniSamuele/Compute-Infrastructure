package it.polimi.mw.compinf.http;

import akka.NotUsed;
import akka.http.javadsl.model.sse.ServerSentEvent;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.annotation.JsonCreator;
import it.polimi.mw.compinf.tasks.Task;
import it.polimi.mw.compinf.util.CborSerializable;

import java.util.UUID;

public interface InternalHttpMessage {
    class TaskCreationMessage implements CborSerializable {
        private final String message;
        private final UUID uuid;

        public TaskCreationMessage(String message, UUID uuid) {
            this.message = message;
            this.uuid = uuid;
        }

        public String getMessage() {
            return message;
        }

        public UUID getUUID() {
            return uuid;
        }
    }

    class CreateTaskMessage implements CborSerializable {
        private final Task task;

        public CreateTaskMessage(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
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