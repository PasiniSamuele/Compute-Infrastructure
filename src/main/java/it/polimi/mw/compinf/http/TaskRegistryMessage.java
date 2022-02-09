package it.polimi.mw.compinf.http;

import akka.NotUsed;
import akka.http.javadsl.model.sse.ServerSentEvent;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.annotation.JsonCreator;
import it.polimi.mw.compinf.tasks.PrimeTask;
import it.polimi.mw.compinf.util.CborSerializable;
import it.polimi.mw.compinf.tasks.CompressionTask;
import it.polimi.mw.compinf.tasks.ConversionTask;

import java.util.UUID;

public interface TaskRegistryMessage {
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

    class TaskFailedMessage implements CborSerializable {

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

    class CreatePrimeMessage implements CborSerializable {
        private final PrimeTask primeTask;

        public CreatePrimeMessage(PrimeTask primeTask) {
            this.primeTask = primeTask;
        }

        public PrimeTask getPrimeTask() {
            return primeTask;
        }
    }

    class CreateConversionMessage implements CborSerializable {
        private final ConversionTask conversionTask;

        public CreateConversionMessage(ConversionTask conversionTask) {
            this.conversionTask = conversionTask;
        }

        public ConversionTask getConversionTask() {
            return conversionTask;
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