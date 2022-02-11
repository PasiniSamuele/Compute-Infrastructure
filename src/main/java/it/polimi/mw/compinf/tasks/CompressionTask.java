package it.polimi.mw.compinf.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CompressionTask extends Task {
    private final double compressionRatio;

    @JsonCreator
    public CompressionTask(
            @JsonProperty("directoryName") String directoryName,
            @JsonProperty("compressionRatio") double compressionRatio,
            @JsonProperty("forceFailure") int forceFailure) {
        super(directoryName, forceFailure);
        this.compressionRatio = compressionRatio;
        this.name = "Compression";
    }

    public double getCompressionRatio() {
        return compressionRatio;
    }
}
