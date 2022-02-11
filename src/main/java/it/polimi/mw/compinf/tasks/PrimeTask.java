package it.polimi.mw.compinf.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PrimeTask extends Task {
    private final int upperBound;

    @JsonCreator
    public PrimeTask(
            @JsonProperty("directoryName") String directoryName,
            @JsonProperty("upperBound") int upperBound,
            @JsonProperty("forceFailure") int forceFailure) {
        super(directoryName, forceFailure);
        this.upperBound = upperBound;
        this.name = "Prime";
    }

    public int getUpperBound() {
        return upperBound;
    }
}
