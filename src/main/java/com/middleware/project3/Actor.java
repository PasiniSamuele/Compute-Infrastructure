package com.middleware.project3;

import java.util.Optional;

import akka.actor.AbstractActor;

public class Actor extends AbstractActor {
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Message.class, this::onMessage).build();
	}
	
	private void onMessage(Message message) throws Exception {
		System.out.println("Received " + message.getId());
		
		int randInt = (int) (Math.random() * 4 + 1);
		
		if (randInt > 3) {
			System.out.println("Exception " + message.getId());
			throw new Exception();
		}
		
		try {
			Thread.sleep(randInt * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Finished " + message.getId());
	}
	
	@Override
	public void preRestart(Throwable reason, Optional<Object> message) {
		System.out.println("Restarting...");
		
		// Pay attention to infinite loops!
		// https://stackoverflow.com/questions/13542921/akka-resending-the-breaking-message
		// FIXME Patch orElse == null
		getContext().getSelf().tell(message.orElse(null), getContext().getSender());
	}
	
	@Override
	public void postRestart(Throwable reason) {
		System.out.println("...now restarted!");	
	}
}
