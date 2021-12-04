package it.polimi.mw.compinf;

public class TaskMessage extends Message {
	private int id;
	private int priority;
	
	public TaskMessage(int id) {
		this.id = id;
		this.priority = 10;
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

	public void setPriority(int priority) {
		this.priority = priority;
	}
}
