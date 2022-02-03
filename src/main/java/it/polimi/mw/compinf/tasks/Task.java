package it.polimi.mw.compinf.tasks;

import it.polimi.mw.compinf.util.CborSerializable;

import java.util.UUID;

public abstract class Task implements CborSerializable {
    private final UUID uuid;
    private final String directoryName;
    private int priority;

    public Task(String directoryName, int priority) {
        this.uuid = UUID.randomUUID();
        this.directoryName = directoryName;
        this.priority = priority;
    }

    public Task(String directoryName) {
        this(directoryName, 1);
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public int getPriority() {
        return priority;
    }

    public Task increasePriority() {
        if (priority > 0) {
            priority--;
        }
        return this;
    }
}
