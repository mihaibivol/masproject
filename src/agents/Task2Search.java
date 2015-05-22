package agents;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;

import agents.World.Ship;

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
				MessageTemplate template = World.ConversationType.AGENT_STATE.getTemplate();
				ACLMessage sm = World.ConversationType.AGENT_STATE.createNewMessage();
				sm.addReceiver(new AID("world", AID.ISLOCALNAME));
				sm.setReplyWith("state" + System.currentTimeMillis());
				
				send(sm);
				ACLMessage reply = blockingReceive(template);
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
				
				ACLMessage msg = World.ConversationType.GOLD_DISCOVERY.createNewMessage();
				
				
				try {
					msg.setContentObject(translatePointsToShip(state.shipVector, state.goldVector));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				World.broadcast("CarrierAgents", myAgent, msg);
				
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
	
	ArrayList<Point> translatePointsToShip(Point shipVector, ArrayList<Point> goldVector) {
		ArrayList<Point> points = new ArrayList<Point>();
		for (Point p : goldVector) {
			points.add(new Point(p.x - shipVector.x, p.y - shipVector.y));
		}
		
		return points;
	}
}

