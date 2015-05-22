package agents;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import agents.World.Ship;

public class Task2Search extends Task1Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
		
		addBehaviour(new TickerBehaviour(this, 50) {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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
				
				if (!state.goldVector.isEmpty()) {	
					ACLMessage msg = World.ConversationType.GOLD_DISCOVERY.createNewMessage();
					
					
					try {
						msg.setContentObject(translatePointsToShip(state.shipVector, new HashSet<Point>(state.goldVector)));
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					World.broadcast("CarrierAgents", myAgent, msg);
				}
				
				String dir;
				if (state.done) {
					return;
				}

				if (state.shipVector.distance(new Point(0, 0)) > 5) {
					Point dest = (Point)state.shipVector.clone();
					dest.move(-dest.x, -dest.y);
					AffineTransform aft = new AffineTransform();
					aft.rotate(1.3);
					aft.transform(dest, dest);
					dir = randomChoice(getPossibleDirs(dest));
				} else {
					dir = randomChoice(getComplement(null));
				}
					
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
	
	HashSet<Point> translatePointsToShip(Point shipVector, HashSet<Point> goldVector) {
		HashSet<Point> points = new HashSet<Point>();
		for (Point p : goldVector) {
			points.add(new Point(p.x - shipVector.x, p.y - shipVector.y));
		}
		
		return points;
	}
}

