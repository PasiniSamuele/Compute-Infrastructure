package com.middleware.project3;

import akka.actor.AbstractActor;

public class Actor extends AbstractActor {
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Message.class, this::onMessage).build();
	}
	
	private void onMessage(Message message) {
		System.out.println("Received " + message.getId() + ", sleeping...");
		
		try {
			Thread.sleep(((int) (Math.random() * 4 + 1)) * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Finished " + message.getId());
	}
}
