package it.polimi.mw.compinf.tasks;

import it.polimi.mw.compinf.util.CborSerializable;

import java.util.UUID;

public class TaskResult implements CborSerializable {
    private final UUID uuid;
    private final String directoryName;
    private final byte[] file;

    public TaskResult(UUID uuid, byte[] file, String directoryName) {
        this.uuid = uuid;
        this.directoryName = directoryName;
        this.file = file;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public byte[] getFile() {
        return file;
    }
}
