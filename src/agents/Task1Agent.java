package agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Task1Agent extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrayList<Point> possibleDest = new ArrayList<Point>();
	
	@Override
	protected void setup() {
		System.out.println("Agent started");
		// Subscribe to the world
		ACLMessage m = new ACLMessage(ACLMessage.SUBSCRIBE);
		m.setContent("Task1");
		m.addReceiver(new AID("world", AID.ISLOCALNAME));
		send(m);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
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
			private Random rng = new Random();
			private Point defaultDest = randomChoice(possibleDest);
			
			private ArrayList<String> getPossibleDirs(Point dest) {
				ArrayList<String> res = new ArrayList<String>();
				if (dest.getX() > 0) res.add("Right");
				if (dest.getX() < 0) res.add("Left");
				if (dest.getY() > 0) res.add("Down");
				if (dest.getY() < 0) res.add("Up");
				return res;
			}
			
			private ArrayList<String> getComplement(ArrayList<String> dirs) {
				ArrayList<String> res = new ArrayList<String>();
				res.add("Left");
				res.add("Right");
				res.add("Up");
				res.add("Down");
				if (dirs == null) return res;
				res.removeAll(dirs);
				return res;
			}
			
			private <T> T randomChoice(ArrayList<T> s) {
				int idx = rng.nextInt(s.size());
				return s.get(idx);
			}
			
			private Point getClosest(ArrayList<Point> points) {
				Point origin = new Point(0, 0);
				Point res = null;
				double minDist = Double.MAX_VALUE;
				for (Point p : points) {
					double dist = p.distance(origin);
					if (dist < minDist) {
						minDist = dist;
						res = p;
					}
				}
				return res;
			}
			
			private Point destFromDir(String dir) {
				if (dir.equals("Right")) return new Point(1, 0);
				if (dir.equals("Left")) return new Point(-1, 0);
				if (dir.equals("Up")) return new Point(0, -1);
				if (dir.equals("Down")) return new Point(0, 1);
				return new Point(0, 0);
			}
			
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
				
				shuffleDest();
				String dir = randomChoice(getPossibleDirs(defaultDest));
				if (state.full) {
					dir = randomChoice(getPossibleDirs(state.shipVector));
				} else if (!state.goldVector.isEmpty()) {
					Point dest = getClosest(state.goldVector);
					dir = randomChoice(getPossibleDirs(dest));
				} else if (state.done && !state.empty) {
					dir = randomChoice(getPossibleDirs(state.shipVector));
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
}
