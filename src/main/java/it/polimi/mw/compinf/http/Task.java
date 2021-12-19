package it.polimi.mw.compinf.http;

import static java.util.UUID.randomUUID;

public class Task {
    private final String uuid;
    private int priority;

    public Task(int priority) {
        this.uuid = randomUUID().toString();
        this.priority = priority;
    }

    public Task() {
        this(1);
    }

    public String getUUID() {
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
