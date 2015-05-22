package agents;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class World extends Agent {
	final int GOLD = 100;
	final int OBST = 100;
	
	final int GOLD_DIST = 20;
	final int OBST_DIST = 5;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private int X;
	private int Y;
	private final int S = 7;
	private int SX;
	private int SY;
	
	abstract class Actor {
		//empty stuff
	}
	
	abstract class AddressableActor extends Actor {
		private AID aid;
		
		public AddressableActor(AID aid) {
			this.aid = aid;
		}
		
		public AID getAID() {
			return aid;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof AddressableActor) {
				return aid.equals(((AddressableActor)other).aid);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return aid.hashCode();
		}
	}
	
	class Agent extends AddressableActor {
		public Point pos;
		public int capacity;
		public int having;
		public String type;

		public Agent(AID aid) {
			super(aid);
		}
		
	}

	class Ship extends Actor {
		
	}
	
	class Obstacle extends Actor {
		
	}
	
	class Gold extends Actor {
		
	}
	
	int totalGold = 0;
	int totalMoves = 0;
	int existingGold = GOLD;
	
	private HashMap<Point, ArrayList<Actor>> map = new HashMap<Point, ArrayList<Actor>>();
	private ArrayList<Agent> agents = new ArrayList<World.Agent>();
	private Point ship;
	
	static boolean containsClass(ArrayList<Actor> things, String className) {
		for (Actor a : things) {
			if (a.getClass().getName().endsWith(className)) return true;
		}
		return false;
	}
	
	@Override
	protected void setup() {
		X = 110;
		Y = 110;
		SX = 30;
		SY = 30;
		
		Random rng = new Random();
		for (int i = -1; i <= X; i++) {
			for (int j = -1; j <= Y; j++) {
				map.put(new Point(i, j), new ArrayList<World.Actor>());
				if (i == -1 || j == -1 || i == X || j == Y) {
					map.get(new Point(i, j)).add(new Obstacle());
				}
			}
		}
		
		final GUI gui = new GUI();
		
		int numobst = OBST;
		int numgold = GOLD;
		int x = X / 2 + rng.nextInt(SX) - SX / 2;
		int y = Y / 2 + rng.nextInt(SY) - SY / 2;
		map.get(new Point(x, y)).add(new Ship());
		ship = new Point(x, y);
				
		while (numobst-- != 0) {
			x = rng.nextInt(X);
			y = rng.nextInt(Y);
			
			Point p = new Point(x, y);
			if ((x < (X / 2 + SX / 2) && x > (X / 2 - SX / 2)
				  && y < (Y / 2 + SY / 2) && y > (Y / 2 - SY / 2))
					|| !map.get(p).isEmpty()) {
				numobst++;
				continue;
			}
			
			
			map.get(p).add(new Obstacle());
		}
		
		while (numgold-- != 0) {
			x = rng.nextInt(X);
			y = rng.nextInt(Y);
			
			Point p = new Point(x, y);
			if ((x < (X / 2 + SX / 2) && x > (X / 2 - SX / 2)
				  && y < (Y / 2 + SY / 2) && y > (Y / 2 - SY / 2))
					|| !map.get(p).isEmpty()) {
				numgold++;
				continue;
			}
			
			
			map.get(p).add(new Gold());
		}
		gui.render();
		
		// Add behavior for registering agents
		addBehaviour(new Behaviour() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean done() {
				return totalGold == GOLD;
			}
			
			@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
				ACLMessage m = receive(mt);
				if (m != null) {
					Agent a = new Agent(m.getSender());
					String type = m.getContent();
					a.having = 0;
					if (type.equals("Task1")) {
						a.capacity = 3;
					} else if (type.equals("Task2Carrier")) {
						a.capacity = GOLD * 2;
					} else if (type.equals("Task2Search")) {
						a.capacity = 0;
					}
					a.type = type;
					if (agents.contains(a)) return;
					
					agents.add(a);
					Random rng = new Random();
					int x = X / 2 + rng.nextInt(SX) - SX / 2;
					int y = Y / 2 + rng.nextInt(SY) - SY / 2;
					while (!map.get(new Point(x, y)).isEmpty()) {
						x = X / 2 + rng.nextInt(SX) - SX / 2;
						y = Y / 2 + rng.nextInt(SY) - SY / 2;
					}
					Point p = new Point(x, y);
					a.pos = p;
					map.get(p).add(a);
				} else {
					block();
				}
			}
		});
		
		// Add behavior for moving agents
		addBehaviour(new Behaviour() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean done() {
				return totalGold == GOLD;
			}
			
			@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage m = receive(mt);
				if (m != null) {
					totalMoves++;
					// Get a reference to the actual agent in the agent list
					Agent a = agents.get(agents.indexOf(new Agent(m.getSender())));
					
					Point newP = (Point)a.pos.clone();
					if (m.getContent().equals("Right")) {
						newP.translate(1, 0);
					} else if (m.getContent().equals("Left")) {
						newP.translate(-1, 0);
					} else if (m.getContent().equals("Up")) {
						newP.translate(0, -1);
					} else if (m.getContent().equals("Down")) {
						newP.translate(0, 1);
					}
					
					// Check for obstacle or other agent
					ArrayList<Actor> things = map.get(newP);
					
					// Out of bounds
					if (things == null) return;
					
					// Have an obstacle or another agent
					if (containsClass(things, "Obstacle")) return;
					if (containsClass(things, "Agent")) return;
					
					
					if (containsClass(things, "Gold") && a.having < a.capacity) {
						// gold is the only thing at that moment so we can clear that vector
						things.clear();
						a.having += 1;
						existingGold--;
					}
					
					if (containsClass(things, "Ship")) {
						totalGold += a.having;
						System.out.println("Received more gold: " + totalGold + " " + totalMoves / agents.size());
						a.having = 0;
						if (totalGold == GOLD) {
							System.out.println("Average total moves: " + totalMoves / agents.size());
						}
					}
					
					map.get(a.pos).remove(a);
					a.pos = newP;
					map.get(newP).add(a);
					gui.render();
				} else {
					block();
				}
			}
		});
		
		// Add behavior for sensor requests
		addBehaviour(new Behaviour() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean done() {
				return totalGold == GOLD;
			}
			
			private Point getDelta(Point fr, Point to) {
				return new Point((int)(to.getX() - fr.getX()), (int)(to.getY() - fr.getY()));
			}
			
			@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage m = receive(mt);
				if (m != null) {
					ACLMessage reply = m.createReply();
					// Get a reference to the actual agent in the agent list
					Agent a = agents.get(agents.indexOf(new Agent(m.getSender())));
					AgentState s = new AgentState();
					if (a.capacity == a.having) s.full = true;
					if (a.having == 0) s.empty = true;
					if (existingGold == 0) s.done = true;
					
					s.shipVector = getDelta(a.pos, ship);
					
					// Add other distances
					for (Entry<Point, ArrayList<Actor>> e : map.entrySet()) {
						if (e.getKey().equals(a.pos)) continue;
						if (e.getKey().distance(a.pos) < GOLD_DIST &&
								containsClass(e.getValue(), "Gold")) {
							s.goldVector.add(getDelta(a.pos, e.getKey()));
						}
						if (e.getKey().distance(a.pos) < OBST_DIST &&
								containsClass(e.getValue(), "Agent")) {
							s.agentsVector.add(getDelta(a.pos, e.getKey()));
						}
						if (e.getKey().distance(a.pos) < OBST_DIST &&
								containsClass(e.getValue(), "Obstacle")) {
							s.obstacleVector.add(getDelta(a.pos, e.getKey()));
						}
					}
					
					
					
					try {
						reply.setContentObject(s);
					} catch (IOException e) {
						e.printStackTrace();
					}
					send(reply);
				} else {
					block();
				}
				
			}
		});
	}
	
	
	/*** GUI STUFF ***/
	private class GUIPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.black);
			g.fillRect(0, 0, X * S, Y * S);
			for (int x = 0; x < X; x++) {
				for (int y = 0; y < Y; y++) {
					ArrayList<Actor> actors = map.get(new Point(x, y));
					if (actors.isEmpty()) {
						continue;
					} else if (actors.get(0) instanceof Obstacle) {
						g.setColor(Color.red);
						g.fillRect(x * S, y * S, S, S);
					} else if (actors.get(0) instanceof Ship) {
						g.setColor(Color.magenta);
						g.fillRect(x * S, y * S, S, S);
					} else if (actors.get(0) instanceof Gold) {
						g.setColor(Color.orange);
						g.fillRect(x * S, y * S, S, S);
					} else if (actors.get(0) instanceof Agent) {
						Agent a = (Agent)actors.get(0);
						if (a.type.equals("Task1")) {
							g.setColor(Color.green);
						} else if (a.type.equals("Task2Carrier")) {
							g.setColor(Color.cyan);
						} else if (a.type.equals("Task2Search")) {
							g.setColor(Color.pink);
						}
						g.fillRect(x * S, y * S, S, S);
					}
				}
			}
		}
		
	}
	
	private class GUI {
		private JFrame frame;
		private GUIPanel p;
		
		public GUI() {
			frame = new JFrame();
			frame.setSize(X * S + 20, Y * S + 20);
			p = new GUIPanel();
			p.setSize(X * S, Y * S);
			frame.setVisible(true);
			frame.add(p);
		}
		
		public void render() {
			p.revalidate();
			p.repaint();
		}
	}

}
