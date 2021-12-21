package it.polimi.mw.compinf.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CompressionTask extends Task {
    private final double compressionRatio;

    @JsonCreator
    public CompressionTask(
            @JsonProperty("directoryName") String directoryName,
            @JsonProperty("compressionRatio") double compressionRatio) {
        super(directoryName);
        this.compressionRatio = compressionRatio;
    }

    public double getCompressionRatio() {
        return compressionRatio;
    }
}
