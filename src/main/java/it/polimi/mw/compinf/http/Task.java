package it.polimi.mw.compinf.http;

import it.polimi.mw.compinf.message.TaskMessage;

public class Task {
    private final String uuid;
    private int priority;

    public Task(int priority) {
        this.uuid = java.util.UUID.randomUUID().toString();
        this.priority = priority;
    }

    public Task() {
        this.uuid = java.util.UUID.randomUUID().toString();
        this.priority = 1;
    }


    public String getUUID() {
        return uuid;
    }

    public Task increasePriority() {
        if (priority > 0) {
            priority--;
        }
        return this;
    }
}
