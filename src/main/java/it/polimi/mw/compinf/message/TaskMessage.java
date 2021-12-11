package it.polimi.mw.compinf.message;

public class TaskMessage implements Message {
	private final int id;
	private int priority;
	
	public TaskMessage(int id) {
		this(id, 1);
	}
	
	public TaskMessage(int id, int priority) {
		this.id = id;
		this.priority = priority;
	}
	
	public int getId() {
		return id;
	}

	public int getPriority() {
		return priority;
	}

	public TaskMessage increasePriority() {
		if (priority > 0) {
			priority--;
		}
		return this;
	}
}
