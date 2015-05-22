package agents;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.awt.Point;
import java.util.ArrayList;

public class Task2Search extends Task1Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<Point> possibleDest;
	@Override
	protected void setup() {
		System.out.println("Agent started");
		// Subscribe to the world
		ACLMessage m = new ACLMessage(ACLMessage.SUBSCRIBE);
		m.setContent("Task2Search");
		m.addReceiver(new AID("world", AID.ISLOCALNAME));
		send(m);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		/* This can remain for search agents
		 * they are drawn to points in the lower right corner frontier
		 * if they don't have anything else to do
		 */
		int frontier = 3000;
		for (int i = -frontier; i < frontier; i+= 10) {
			possibleDest.add(new Point(i, frontier));
			possibleDest.add(new Point(frontier, i));
		}
		
		addBehaviour(new TickerBehaviour(this, 50) {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			
			private Point defaultDest = randomChoice(possibleDest);
			

			
			/* This is sort of a agent's builtin random number generator */
			private void shuffleDest() {
				if (rng.nextDouble() < .05) {
					defaultDest = randomChoice(possibleDest);
				}
			}
			

			@Override
			protected void onTick() {
				ACLMessage sm = new ACLMessage(ACLMessage.REQUEST);
				sm.addReceiver(new AID("world", AID.ISLOCALNAME));
				send(sm);
				ACLMessage reply = blockingReceive();
				AgentState state = null;
				
				try {
					state = (AgentState)reply.getContentObject();
				} catch (UnreadableException e) { 
					return;
				}
				
				/* Just move randomly without being drawn to anything else */
				shuffleDest();
				String dir = randomChoice(getPossibleDirs(defaultDest));
					
				/* TODO send messages with the gold position */
				/*
				 * Gold position can be translated given the ship vector and the gold vector
				 */
				if (state.obstacleVector.contains(destFromDir(dir)))
					dir = randomChoice(getComplement(null));
				if (state.agentsVector.contains(destFromDir(dir)))
					dir = randomChoice(getComplement(null));
				
				//System.out.println(state);
				ACLMessage m = new ACLMessage(ACLMessage.CFP);
				m.addReceiver(new AID("world", AID.ISLOCALNAME));
				m.setContent(dir);
				send(m);
			}
		});
	}
}

