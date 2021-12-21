package it.polimi.mw.compinf.http;

import java.util.UUID;


public class Task {
    private final UUID uuid;
    private int priority;

    public Task(int priority) {
        this.uuid = UUID.randomUUID();
        this.priority = priority;
    }

    public Task() {
        this(1);
    }

    public UUID getUUID() {
        return uuid;
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
