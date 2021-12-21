package it.polimi.mw.compinf.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class CompressionTask extends Task {
    private final double compressionRatio;

    @JsonCreator
    public CompressionTask(@JsonProperty("compressionRatio") double compressionRatio) {
        super();
        this.compressionRatio = compressionRatio;
    }

    public double getCompressionRatio() {
        return compressionRatio;
    }
}
