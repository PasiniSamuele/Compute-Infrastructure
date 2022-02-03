package it.polimi.mw.compinf.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConversionTask extends Task {
    private final String targetFormat;

    @JsonCreator
    public ConversionTask(
            @JsonProperty("directoryName") String directoryName,
            @JsonProperty("targetFormat") String targetFormat) {
        super(directoryName);
        this.targetFormat = targetFormat;
    }

    public String getTargetFormat() {
        return targetFormat;
    }
}