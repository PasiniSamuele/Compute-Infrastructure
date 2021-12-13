package it.polimi.mw.compinf.message;

public class TaskMessage implements Message {
	private final String uuid;
	private int priority;
	
	public TaskMessage(String uuid) {
		this(uuid, 1);
	}
	
	public TaskMessage(String uuid, int priority) {
		this.uuid = uuid;

	}
	
	public String getUUID() {
		return uuid;
	}

	public int getPriority() {
		return priority;
	}


}
