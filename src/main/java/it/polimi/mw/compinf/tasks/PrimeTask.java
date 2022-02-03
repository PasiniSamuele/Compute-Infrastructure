package it.polimi.mw.compinf.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PrimeTask extends Task {
    private final int upperBound;

    @JsonCreator
    public PrimeTask(
            @JsonProperty("directoryName") String directoryName,
            @JsonProperty("upperBound") int upperBound) {
        super(directoryName);
        this.upperBound = upperBound;
    }

    public int getUpperBound() {
        return upperBound;
    }
}
